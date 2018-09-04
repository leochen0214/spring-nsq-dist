package com.youzan.spring.nsq.listener.adapter;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.exception.ListenerExecutionFailedException;
import com.youzan.spring.nsq.handler.NsqListenerErrorHandler;
import com.youzan.spring.nsq.listener.ConsumerAwareMessageListener;

import org.springframework.messaging.Message;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Slf4j
public class NsqMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter
    implements ConsumerAwareMessageListener {

  private NsqListenerErrorHandler errorHandler;

  public NsqMessagingMessageListenerAdapter(Object bean, Method method) {
    this(bean, method, null);
  }

  public NsqMessagingMessageListenerAdapter(Object bean, Method method,
                                            NsqListenerErrorHandler errorHandler) {
    super(bean, method);
    this.errorHandler = errorHandler;
  }


  @Override
  public void onMessage(NSQMessage data, Consumer consumer) {
    Message<?> message = toMessagingMessage(data, consumer);
    if (log.isDebugEnabled()) {
      log.debug("Processing [" + message + "]");
    }

    try {
      invokeHandler(data, message, consumer);
    } catch (ListenerExecutionFailedException e) {
      if (this.errorHandler == null) {
        throw e;
      }

      try {
        this.errorHandler.handleError(data, e, consumer);
        if (log.isDebugEnabled()) {
          log.debug("NsqListenerErrorHandler handle error");
        }
      } catch (Exception ex) {
        throw new ListenerExecutionFailedException(createMessagingErrorMessage(
            "Listener error handler threw an exception for the incoming message",
            message.getPayload()), ex);
      }
    }

  }
}
