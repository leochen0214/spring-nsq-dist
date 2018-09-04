package com.youzan.sleuth.instrument.messaging;

import com.youzan.brave.nsq.client.NsqTracing;
import com.youzan.nsq.client.Producer;
import com.youzan.spring.nsq.listener.AbstractMessageListenerContainer;
import com.youzan.spring.nsq.listener.MessageListener;
import com.youzan.spring.nsq.listener.MessageListenerContainer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;

import brave.Tracer;

/**
 * @author: clong
 * @date: 2018-09-03
 */
@Aspect
public class SleuthSpringNsqAspect {

  private static final Logger logger = LoggerFactory.getLogger(SleuthSpringNsqAspect.class);

  private final NsqTracing nsqTracing;
  private final Tracer tracer;


  public SleuthSpringNsqAspect(NsqTracing nsqTracing, Tracer tracer) {
    this.nsqTracing = nsqTracing;
    this.tracer = tracer;
  }


  @Pointcut("execution(public * com.youzan.spring.nsq.core.ProducerFactory.createProducer(..))")
  private void anyProducerFactory() {
  } // NOSONAR


  @Pointcut("execution(public * com.youzan.spring.nsq.core.MessageListenerContainerFactory.createListenerContainer(..))")
  private void anyCreateListenerContainer() {
  } // NOSONAR

  @Around("anyProducerFactory()")
  public Object wrapProducerFactory(ProceedingJoinPoint pjp) throws Throwable {
    Producer producer = (Producer) pjp.proceed();
    return this.nsqTracing.producer(producer);
  }


  @Around("anyCreateListenerContainer()")
  public Object wrapListenerContainerCreation(ProceedingJoinPoint pjp) throws Throwable {
    MessageListenerContainer container = (MessageListenerContainer) pjp.proceed();
    if (!(container instanceof AbstractMessageListenerContainer)) {
      logger.debug("Can't wrap this message listener. Proceeding");
      return container;
    }

    AbstractMessageListenerContainer instance = (AbstractMessageListenerContainer) container;
    Object someMessageListener = instance.getContainerProperties().getMessageListener();
    if (someMessageListener == null) {
      logger.debug("No message listener to wrap. Proceeding");
    } else if (someMessageListener instanceof MessageListener) {
      instance.setupMessageListener(createProxy(someMessageListener));
    } else {
      logger.debug("ATM we don't support other message listeners");
    }

    return container;
  }


  private Object createProxy(Object bean) {
    ProxyFactoryBean factory = new ProxyFactoryBean();
    factory.setProxyTargetClass(true);
    factory.addAdvice(new MessageListenerMethodInterceptor(this.nsqTracing, this.tracer));
    factory.setTarget(bean);
    return factory.getObject();
  }


}
