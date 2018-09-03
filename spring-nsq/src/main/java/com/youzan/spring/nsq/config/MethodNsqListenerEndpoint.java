package com.youzan.spring.nsq.config;

import com.youzan.spring.nsq.handler.NsqListenerErrorHandler;
import com.youzan.spring.nsq.listener.MessageListenerContainer;
import com.youzan.spring.nsq.listener.adapter.MessagingMessageListenerAdapter;
import com.youzan.spring.nsq.listener.adapter.RecordMessagingMessageListenerAdapter;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author: clong
 * @date: 2018-08-31
 */
public class MethodNsqListenerEndpoint implements NsqListenerEndpoint, BeanFactoryAware {

  private String id;
  private final Collection<String> topics = new ArrayList<>();
  private String channel;
  private Boolean autoStartup;
  private Boolean ordered;
  private Boolean autoFinish;
  private String group;

  private Object bean;
  private Method method;
  private MessageHandlerMethodFactory messageHandlerMethodFactory;
  private NsqListenerErrorHandler errorHandler;


  private BeanFactory beanFactory;
  private BeanExpressionResolver resolver;
  private BeanExpressionContext expressionContext;
  private BeanResolver beanResolver;


  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Collection<String> getTopics() {
    return Collections.unmodifiableCollection(topics);
  }

  public void setTopics(String... topics) {
    Assert.notNull(topics, "'topics' must not be null");
    this.topics.clear();
    this.topics.addAll(Arrays.asList(topics));
  }

  @Override
  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  @Override
  public boolean ordered() {
    return ordered;
  }

  public void setOrdered(boolean ordered) {
    this.ordered = ordered;
  }

  @Override
  public boolean autoFinish() {
    return autoFinish;
  }

  public void setAutoFinish(boolean autoFinish) {
    this.autoFinish = autoFinish;
  }

  @Override
  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  @Override
  public Boolean getAutoStartup() {
    return autoStartup;
  }

  public void setAutoStartup(Boolean autoStartup) {
    this.autoStartup = autoStartup;
  }


  /**
   * Set the object instance that should manage this endpoint.
   *
   * @param bean the target bean instance.
   */
  public void setBean(Object bean) {
    this.bean = bean;
  }

  public Object getBean() {
    return this.bean;
  }

  /**
   * Set the method to invoke to process a message managed by this endpoint.
   *
   * @param method the target method for the {@link #bean}.
   */
  public void setMethod(Method method) {
    this.method = method;
  }

  public Method getMethod() {
    return this.method;
  }

  /**
   * Set the {@link MessageHandlerMethodFactory} to use to build the {@link InvocableHandlerMethod}
   * responsible to manage the invocation of this endpoint.
   *
   * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory} instance.
   */
  public void setMessageHandlerMethodFactory(
      MessageHandlerMethodFactory messageHandlerMethodFactory) {
    this.messageHandlerMethodFactory = messageHandlerMethodFactory;
  }

  public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
    return messageHandlerMethodFactory;
  }

  /**
   * Set the {@link NsqListenerErrorHandler} to invoke if the listener method throws an exception.
   *
   * @param errorHandler the error handler.
   */
  public void setErrorHandler(NsqListenerErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public NsqListenerErrorHandler getErrorHandler() {
    return errorHandler;
  }


  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    if (beanFactory instanceof ConfigurableListableBeanFactory) {
      ConfigurableListableBeanFactory cf = (ConfigurableListableBeanFactory) beanFactory;
      this.resolver = cf.getBeanExpressionResolver();
      this.expressionContext = new BeanExpressionContext(cf, null);
    }
    this.beanResolver = new BeanFactoryResolver(beanFactory);
  }

  public BeanFactory getBeanFactory() {
    return this.beanFactory;
  }

  public BeanExpressionResolver getResolver() {
    return this.resolver;
  }

  public BeanExpressionContext getBeanExpressionContext() {
    return this.expressionContext;
  }

  public BeanResolver getBeanResolver() {
    return this.beanResolver;
  }

  @Override
  public void setupListenerContainer(MessageListenerContainer listenerContainer,
                                     NSQMessageConverter messageConverter) {
    setupMessageListener(listenerContainer, messageConverter);
  }


  @SuppressWarnings("unchecked")
  private void setupMessageListener(MessageListenerContainer container,
                                    NSQMessageConverter messageConverter) {
    Object messageListener = createMessageListener(container, messageConverter);
    Assert.state(messageListener != null,
                 "Endpoint [" + this + "] must provide a non null message listener");

    container.setupMessageListener(messageListener);
  }


  protected MessagingMessageListenerAdapter createMessageListener(MessageListenerContainer container,
                                                                  NSQMessageConverter messageConverter) {
    Assert.state(this.messageHandlerMethodFactory != null,
                 "Could not create message listener - MessageHandlerMethodFactory not set");

    MessagingMessageListenerAdapter messageListener =
        createMessageListenerInstance(messageConverter);
    messageListener.setHandlerMethod(configureListenerAdapter(messageListener));


    return messageListener;
  }


  /**
   * Create an empty {@link MessagingMessageListenerAdapter} instance.
   *
   * @param converter the converter (may be null).
   * @return the {@link MessagingMessageListenerAdapter} instance.
   */
  protected MessagingMessageListenerAdapter createMessageListenerInstance(
      NSQMessageConverter converter) {

    RecordMessagingMessageListenerAdapter listener =
        new RecordMessagingMessageListenerAdapter(bean, method, errorHandler);

    listener.setMessageConverter(converter);

    return listener;
  }


  /**
   * Create a {@link InvocableHandlerMethod} for this listener adapter.
   * @param messageListener the listener adapter.
   * @return the handler adapter.
   */
  protected InvocableHandlerMethod configureListenerAdapter(MessagingMessageListenerAdapter messageListener) {
    return this.messageHandlerMethodFactory.createInvocableHandlerMethod(getBean(), getMethod());
  }



}
