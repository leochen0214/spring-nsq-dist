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

  private static final Logger logger =
      LoggerFactory.getLogger(MessageListenerMethodInterceptor.class);

  private final NsqTracing nsqTracing;
  private final Tracer tracer;

  public MessageListenerMethodInterceptor(NsqTracing nsqTracing, Tracer tracer) {
    this.nsqTracing = nsqTracing;
    this.tracer = tracer;
  }


  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (!"onMessage".equals(invocation.getMethod().getName())) {
      return invocation.proceed();
    }

    Object[] arguments = invocation.getArguments();
    Optional<Object> message =
        Arrays.stream(arguments).filter(o -> o instanceof NSQMessage).findFirst();

    if (!message.isPresent()) {
      return invocation.proceed();
    }

    logger.info("Wrapping onMessage call");

    Span span = this.nsqTracing.nextSpan((NSQMessage) message.get()).name("on-message").start();
    try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(span)) {
      logger.info("on-message ... ");
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
      logger.info("wrapping onMessage call finish");
    }
  }
}
