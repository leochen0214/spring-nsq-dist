package com.youzan.sleuth.instrument.messaging;

import com.youzan.brave.nsq.client.NsqTracing;
import com.youzan.nsq.client.Consumer;
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

  @Pointcut("execution(public * com.youzan.spring.nsq.core.ConsumerFactory.createConsumer(..))")
  private void anyConsumerFactory() {
  } // NOSONAR

  @Pointcut("execution(public * com.youzan.spring.nsq.core.MessageListenerContainerFactory.createListenerContainer(..))")
  private void anyCreateListenerContainer() {
  } // NOSONAR

  @Around("anyProducerFactory()")
  public Object wrapProducerFactory(ProceedingJoinPoint pjp) throws Throwable {
    Producer producer = (Producer) pjp.proceed();
    return this.nsqTracing.producer(producer);
  }

  @Around("anyConsumerFactory()")
  public Object wrapConsumerFactory(ProceedingJoinPoint pjp) throws Throwable {
    Consumer consumer = (Consumer) pjp.proceed();
    return this.nsqTracing.consumer(consumer);
  }

  @Around("anyCreateListenerContainer()")
  public Object wrapListenerContainerCreation(ProceedingJoinPoint pjp) throws Throwable {
    MessageListenerContainer listener = (MessageListenerContainer) pjp.proceed();
    if (listener instanceof AbstractMessageListenerContainer) {
      AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) listener;
      Object someMessageListener = container.getContainerProperties().getMessageListener();
      if (someMessageListener == null) {
        logger.debug("No message listener to wrap. Proceeding");
      } else if (someMessageListener instanceof MessageListener) {
        container.setupMessageListener(createProxy(someMessageListener));
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("ATM we don't support Batch message listeners");
        }
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Can't wrap this listener. Proceeding");
      }
    }
    return listener;
  }


  Object createProxy(Object bean) {
    ProxyFactoryBean factory = new ProxyFactoryBean();
    factory.setProxyTargetClass(true);
    factory.addAdvice(new MessageListenerMethodInterceptor(this.nsqTracing, this.tracer));
    factory.setTarget(bean);
    return factory.getObject();
  }


}
