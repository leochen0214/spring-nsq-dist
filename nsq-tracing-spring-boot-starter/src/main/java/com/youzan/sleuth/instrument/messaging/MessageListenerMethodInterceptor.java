package com.youzan.sleuth.instrument.messaging;

import com.youzan.brave.nsq.client.NsqTracing;
import com.youzan.nsq.client.entity.NSQMessage;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import brave.Span;
import brave.Tracer;

/**
 * @author: clong
 * @date: 2018-09-03
 */
public class MessageListenerMethodInterceptor implements MethodInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(MessageListenerMethodInterceptor.class);

  private static final String METHOD_NAME = "onMessage";

  private final NsqTracing nsqTracing;
  private final Tracer tracer;

  public MessageListenerMethodInterceptor(NsqTracing nsqTracing, Tracer tracer) {
    this.nsqTracing = nsqTracing;
    this.tracer = tracer;
  }


  /**
   * intercept {@link com.youzan.spring.nsq.listener.MessageListener} MessageListener.onMessage(..)
   * method
   */
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (!METHOD_NAME.equals(invocation.getMethod().getName())) {
      return invocation.proceed();
    }

    Object[] arguments = invocation.getArguments();
    Optional<Object> message = Arrays.stream(arguments)
        .filter(o -> o instanceof NSQMessage)
        .findFirst();

    if (!message.isPresent()) {
      return invocation.proceed();
    }

    NSQMessage nsqMessage = (NSQMessage) message.get();
    logger.info("on-messaging nsqMessage={}, headers={}", nsqMessage, nsqMessage.getJsonExtHeader());
    Span span = this.nsqTracing.nextSpan(nsqMessage).name(METHOD_NAME).start();
    try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(span)) {
      return invocation.proceed();
    } catch (RuntimeException | Error e) {
      String msg = e.getMessage();
      if (msg == null) {
        msg = e.getClass().getSimpleName();
      }
      span.tag("error", msg);
      throw e;
    } finally {
      span.finish();
    }
  }
}
