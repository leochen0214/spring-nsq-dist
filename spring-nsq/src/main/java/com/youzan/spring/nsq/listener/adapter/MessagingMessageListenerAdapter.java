package com.youzan.spring.nsq.listener.adapter;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.exception.ListenerExecutionFailedException;
import com.youzan.spring.nsq.listener.MessageListener;
import com.youzan.spring.nsq.support.converter.MessagingMessageConverter;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;
import com.youzan.spring.nsq.support.util.NsqUtils;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * An abstract {@link MessageListener} adapter providing the necessary infrastructure to extract the
 * payload of a {@link org.springframework.messaging.Message}.
 *
 * @author: clong
 * @date: 2018-09-02
 */
@Slf4j
public class MessagingMessageListenerAdapter {


  private final Object bean;


  private final Type inferredType;

  private InvocableHandlerMethod handlerMethod;

  private boolean isConsumerRecordList;

  private boolean isConsumerRecords;

  private boolean isMessageList;

  private NSQMessageConverter messageConverter = new MessagingMessageConverter();

  private Type fallbackType = Object.class;


  private boolean messageReturnType;

  public MessagingMessageListenerAdapter(Object bean, Method method) {
    this.bean = bean;
    this.inferredType = determineInferredType(method);
  }

  /**
   * Set the MessageConverter.
   *
   * @param messageConverter the converter.
   */
  public void setMessageConverter(NSQMessageConverter messageConverter) {
    this.messageConverter = messageConverter;
  }

  /**
   * Return the {@link MessagingMessageConverter} for this listener, being able to convert {@link
   * org.springframework.messaging.Message}.
   *
   * @return the {@link MessagingMessageConverter} for this listener, being able to convert {@link
   * org.springframework.messaging.Message}.
   */
  protected final NSQMessageConverter getMessageConverter() {
    return this.messageConverter;
  }

  /**
   * Returns the inferred type for conversion or, if null, the {@link #setFallbackType(Class)
   * fallbackType}.
   *
   * @return the type.
   */
  protected Type getType() {
    return this.inferredType == null ? this.fallbackType : this.inferredType;
  }

  /**
   * Set a fallback type to use when using a type-aware message converter and this adapter cannot
   * determine the inferred type from the method. An example of a type-aware message converter is
   * the {@code StringJsonMessageConverter}. Defaults to {@link Object}.
   *
   * @param fallbackType the type.
   */
  public void setFallbackType(Class<?> fallbackType) {
    this.fallbackType = fallbackType;
  }

  /**
   * Set the {@link InvocableHandlerMethod} to use to invoke the method processing an incoming
   * {@link com.youzan.nsq.client.entity.NSQMessage}.
   *
   * @param handlerMethod {@link InvocableHandlerMethod} instance.
   */
  public void setHandlerMethod(InvocableHandlerMethod handlerMethod) {
    this.handlerMethod = handlerMethod;
  }

  protected boolean isConsumerRecordList() {
    return this.isConsumerRecordList;
  }

  public boolean isConsumerRecords() {
    return this.isConsumerRecords;
  }


  protected boolean isMessageList() {
    return this.isMessageList;
  }


  protected Message<?> toMessagingMessage(NSQMessage record, Consumer consumer) {
    return getMessageConverter().toSpringMessage(record, consumer, getType());
  }

  /**
   * Invoke the handler, wrapping any exception to a {@link ListenerExecutionFailedException} with a
   * dedicated error message.
   *
   * @param data     the data to process during invocation.
   * @param message  the message to process.
   * @param consumer the consumer.
   * @return the result of invocation.
   */
  protected final Object invokeHandler(Object data, Message<?> message, Consumer consumer) {
    try {
      if (data instanceof List && !this.isConsumerRecordList) {
        return this.handlerMethod.invoke(message, consumer);
      } else {
        return this.handlerMethod.invoke(message, data, consumer);
      }
    } catch (org.springframework.messaging.converter.MessageConversionException ex) {

      throw new ListenerExecutionFailedException(
          createMessagingErrorMessage("Listener method could not " +
                                      "be invoked with the incoming message", message.getPayload()),
          new MessageConversionException("Cannot handle message", ex));
    } catch (MessagingException ex) {
      throw new ListenerExecutionFailedException(
          createMessagingErrorMessage("Listener method could not " +
                                      "be invoked with the incoming message", message.getPayload()), ex);
    } catch (Exception ex) {
      throw new ListenerExecutionFailedException("Listener method '" +
                                                 this.handlerMethod.getMethod().toGenericString()
                                                 + "' threw exception", ex);
    }
  }


  protected final String createMessagingErrorMessage(String description, Object payload) {
    return description + "\n"
           + "Endpoint handler details:\n"
           + "Method [" + this.handlerMethod.getMethod().toGenericString() + "]\n"
           + "Bean [" + this.handlerMethod.getBean() + "]";
  }

  /**
   * Subclasses can override this method to use a different mechanism to determine the target type
   * of the payload conversion.
   *
   * @param method the method.
   * @return the type.
   */
  protected Type determineInferredType(Method method) {
    if (method == null) {
      return null;
    }

    Type genericParameterType = null;
    boolean hasConsumerParameter = false;

    for (int i = 0; i < method.getParameterCount(); i++) {
      MethodParameter methodParameter = new MethodParameter(method, i);
      /*
       * We're looking for a single non-annotated parameter, or one annotated with @Payload.
       * We ignore parameters with type Message because they are not involved with conversion.
       */
      if (eligibleParameter(methodParameter)
          && (methodParameter.getParameterAnnotations().length == 0
              || methodParameter.hasParameterAnnotation(Payload.class))) {
        if (genericParameterType == null) {
          genericParameterType = methodParameter.getGenericParameterType();
          if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            if (parameterizedType.getRawType().equals(Message.class)) {
              genericParameterType = ((ParameterizedType) genericParameterType)
                  .getActualTypeArguments()[0];
            } else if (parameterizedType.getRawType().equals(List.class)
                       && parameterizedType.getActualTypeArguments().length == 1) {
              Type paramType = parameterizedType.getActualTypeArguments()[0];
              this.isConsumerRecordList = paramType.equals(NSQMessage.class)
                                          || (paramType instanceof ParameterizedType
                                              && ((ParameterizedType) paramType).getRawType()
                                                  .equals(NSQMessage.class)
                                              || (paramType instanceof WildcardType
                                                  && ((WildcardType) paramType).getUpperBounds()
                                                     != null
                                                  &&
                                                  ((WildcardType) paramType).getUpperBounds().length
                                                  > 0
                                                  && ((WildcardType) paramType)
                                                      .getUpperBounds()[0] instanceof ParameterizedType
                                                  && ((ParameterizedType) ((WildcardType)
                                                                               paramType)
                  .getUpperBounds()[0]).getRawType().equals(NSQMessage.class))
                                          );
              boolean messageHasGeneric = paramType instanceof ParameterizedType
                                          && ((ParameterizedType) paramType).getRawType()
                                              .equals(Message.class);
              this.isMessageList = paramType.equals(Message.class) || messageHasGeneric;
              if (messageHasGeneric) {
                genericParameterType = ((ParameterizedType) paramType).getActualTypeArguments()[0];
              }
            } else {
              this.isConsumerRecords = parameterizedType.getRawType().equals(NSQMessage.class);
            }
          }
        } else {
          if (log.isDebugEnabled()) {
            log.debug("Ambiguous parameters for target payload for method " + method
                      + "; no inferred type available");
          }
          break;
        }
      } else {
        if (methodParameter.getGenericParameterType().equals(Consumer.class)) {
          hasConsumerParameter = true;
        } else {
          Type parameterType = methodParameter.getGenericParameterType();
          hasConsumerParameter = parameterType instanceof ParameterizedType
                                 && ((ParameterizedType) parameterType).getRawType()
                                     .equals(Consumer.class);
        }
      }
    }
    boolean
        validParametersForBatch =
        validParametersForBatch(method.getGenericParameterTypes().length, hasConsumerParameter);
    if (!validParametersForBatch) {
      String stateMessage = "A parameter of type '%s' must be the only parameter "
                            + "(except for an optional 'Acknowledgment' and/or 'Consumer')";
      Assert.state(!this.isConsumerRecords,
                   () -> String.format(stateMessage, "ConsumerRecords"));
      Assert.state(!this.isConsumerRecordList,
                   () -> String.format(stateMessage, "List<ConsumerRecord>"));
      Assert.state(!this.isMessageList,
                   () -> String.format(stateMessage, "List<Message<?>>"));
    }
    this.messageReturnType = NsqUtils.returnTypeMessageOrCollectionOf(method);
    return genericParameterType;
  }

  private boolean validParametersForBatch(int parameterCount, boolean hasConsumer) {
    if (hasConsumer) {
      return parameterCount == 2;
    } else {
      return parameterCount == 1;
    }
  }

  /*
   * Don't consider parameter types that are available after conversion.
   * NSQMessage, Consumer, and Message<?>.
   */
  private boolean eligibleParameter(MethodParameter methodParameter) {
    Type parameterType = methodParameter.getGenericParameterType();
    if (parameterType.equals(NSQMessage.class) || parameterType.equals(Consumer.class)) {
      return false;
    }
    if (parameterType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) parameterType;
      Type rawType = parameterizedType.getRawType();
      if (rawType.equals(NSQMessage.class) || rawType.equals(Consumer.class)) {
        return false;
      } else if (rawType.equals(Message.class)) {
        return !(parameterizedType.getActualTypeArguments()[0] instanceof WildcardType);
      }
    }
    return !parameterType.equals(Message.class); // could be Message without a generic type
  }


}
