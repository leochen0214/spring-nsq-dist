package com.youzan.spring.nsq.annotation;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.messaging.converter.GenericMessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: clong
 * @date: 2018-09-03
 */
public class NsqMessageHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory,
                                                              BeanFactoryAware {

  private final DefaultFormattingConversionService
      defaultFormattingConversionService = new DefaultFormattingConversionService();

  private BeanFactory beanFactory;
  private MessageHandlerMethodFactory messageHandlerMethodFactory;

  public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory factory) {
    this.messageHandlerMethodFactory = factory;
  }


  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
    return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
  }

  private MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
    if (this.messageHandlerMethodFactory == null) {
      this.messageHandlerMethodFactory = createDefaultMessageHandlerMethodFactory();
    }

    return this.messageHandlerMethodFactory;
  }

  private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
    DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
    defaultFactory.setBeanFactory(this.beanFactory);
    defaultFactory.setConversionService(this.defaultFormattingConversionService);

    List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();

    ConfigurableBeanFactory cbf = (this.beanFactory instanceof ConfigurableBeanFactory ?
                                   (ConfigurableBeanFactory) this.beanFactory : null);

    // Annotation-based argument resolution
    argumentResolvers
        .add(new HeaderMethodArgumentResolver(defaultFormattingConversionService, cbf));
    argumentResolvers.add(new HeadersMethodArgumentResolver());

    // Type-based argument resolution
    GenericMessageConverter messageConverter =
        new GenericMessageConverter(defaultFormattingConversionService);
    argumentResolvers.add(new MessageMethodArgumentResolver(messageConverter));
    argumentResolvers.add(new PayloadArgumentResolver(messageConverter) {

      @Override
      protected boolean isEmptyPayload(Object payload) {
        return payload == null;
      }

    });
    defaultFactory.setArgumentResolvers(argumentResolvers);
    defaultFactory.afterPropertiesSet();
    return defaultFactory;
  }

}
