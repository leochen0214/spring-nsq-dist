package com.youzan.spring.nsq.annotation;


import com.youzan.spring.nsq.core.MessageListenerContainerFactory;

import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method to be the target of a nsq message listener on the specified
 * topics.
 *
 * The {@link #containerFactory()} identifies the {@link MessageListenerContainerFactory
 * NsqListenerContainerFactory} to use to build the Nsq listener container. If not set, a
 * <em>default</em> container factory is assumed to be available with a bean name of {@code
 * NsqListenerContainerFactory} unless an explicit default has been provided through configuration.
 *
 * <p>
 * Processing of {@code @NsqListener} annotations is performed by registering a {@link
 * NsqListenerAnnotationBeanPostProcessor}. This can be done manually or, more conveniently, through
 * {@link EnableNsq} annotation.
 *
 * <p>
 * Annotated methods are allowed to have flexible signatures similar to what {@link MessageMapping}
 * provides, that is
 * <ul>
 * <li>{@link com.youzan.nsq.client.entity.NSQMessage} to access to the raw nsq message</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Payload @Payload}-annotated
 * method arguments including the support of validation</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Header @Header}-annotated
 * method arguments to extract a specific header value</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Headers @Headers}-annotated
 * argument that must also be assignable to {@link java.util.Map} for getting access to all
 * headers.</li>
 * <li>{@link org.springframework.messaging.MessageHeaders MessageHeaders} arguments for
 * getting access to all headers.</li>
 * <li>{@link org.springframework.messaging.support.MessageHeaderAccessor
 * MessageHeaderAccessor} for convenient access to all method arguments.</li>
 * </ul>
 *
 * <p>When defined at the method level, a listener container is created for each method.
 * The {@link com.youzan.spring.nsq.listener.MessageListener} is a {@link
 * com.youzan.spring.nsq.listener.adapter.MessagingMessageListenerAdapter}, configured with a {@link
 * com.youzan.spring.nsq.config.MethodNsqListenerEndpoint}.
 *
 * @author: clong
 * @date: 2018-08-29
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
@Repeatable(NsqListeners.class)
public @interface NsqListener {

  /**
   * The unique identifier of the container managing for this endpoint.
   * <p>If none is specified an auto-generated one is provided.
   *
   * @return the {@code id} for the container managing for this endpoint.
   * @see com.youzan.spring.nsq.config.NsqListenerEndpointRegistry#getListenerContainer(String)
   */
  String id() default "";

  /**
   * The bean name of the {@link MessageListenerContainerFactory} to use to create the message
   * listener container responsible to serve this endpoint.
   * <p>If not specified, the default container factory is used, if any.
   *
   * @return the container factory bean name.
   */
  String containerFactory() default "";

  /**
   * The topics for this listener. The entries can be 'topic name', 'property-placeholder keys' or
   * 'expressions'. Expression must be resolved to the topic name.
   *
   * @return the topic names or expressions (SpEL) to listen to.
   */
  String[] topics() default {};

  /**
   * the getChannel name for this listener. The entries can be 'getChannel name',
   * 'property-placeholder keys' or 'expressions'. Expression must be resolved to the getChannel
   * name.
   *
   * @return the getChannel name or expressions (SpEL) to listen to.
   */
  String channel() default "default";

  /**
   * The set of messages is ordered in one specified partition
   */
  boolean ordered() default false;


  boolean autoFinish() default true;


  /**
   * If provided, the listener container for this listener will be added to a bean with this value
   * as its name, of type {@code Collection<MessageListenerContainer>}. This allows, for example,
   * iteration over the collection to start/stop a subset of containers.
   *
   * @return the bean name for the group.
   */
  String containerGroup() default "";

  /**
   * Set an {@link com.youzan.spring.nsq.handler.NsqListenerErrorHandler} to invoke if the listener
   * method throws an exception.
   *
   * @return the error handler.
   * @since 1.3
   */
  String errorHandler() default "";

  /**
   * Override the container factory's {@code concurrency} setting for this listener. May be a
   * property placeholder or SpEL expression that evaluates to a {@link Number}, in which case
   * {@link Number#intValue()} is used to obtain the value.
   *
   * @return the concurrency.
   * @since 2.2
   */
  String concurrency() default "";

  /**
   * Set to true or false, to override the default setting in the container factory. May be a
   * property placeholder or SpEL expression that evaluates to a {@link Boolean} or a {@link
   * String}, in which case the {@link Boolean#parseBoolean(String)} is used to obtain the value.
   *
   * @return true to auto start, false to not auto start.
   * @since 2.2
   */
  String autoStartup() default "";


}
