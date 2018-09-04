package com.youzan.brave.nsq.client;

import com.youzan.nsq.client.MessageReceipt;
import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.core.NSQConnection;
import com.youzan.nsq.client.entity.Address;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.nsq.client.network.frame.NSQFrame;
import com.youzan.util.IOUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.internal.Nullable;
import brave.propagation.TraceContext;

/**
 * 装饰者模式， tracing producer
 *
 * @author: clong
 * @date: 2018-08-28
 */
public class TracingProducer implements Producer {

  private final Tracing tracing;
  private final TraceContext.Injector<Map<String, Object>> injector;
  private final Producer delegate;
  @Nullable
  private final String remoteServiceName;


  public TracingProducer(Tracing tracing, Producer delegate, @Nullable String remoteServiceName) {
    this.delegate = delegate;
    this.tracing = tracing;
    this.injector = tracing.propagation().injector(NsqPropagation.HEADER_SETTER);
    this.remoteServiceName = remoteServiceName;
  }


  @Override
  public void start() throws NSQException {
    delegate.start();
  }

  @Override
  public void start(String... topics) throws NSQException {
    delegate.start(topics);
  }

  @Override
  public void publish(String message, String topic) throws NSQException {
    this.publish(message.getBytes(IOUtil.DEFAULT_CHARSET), topic);
  }

  @Override
  public void publish(String message, Topic topic, Object shardingID) throws NSQException {
    Message msg = Message.create(topic, message);
    msg.setTopicShardingIDObject(shardingID);

    this.publish(msg);
  }

  @Override
  public MessageReceipt publishAndGetReceipt(Message message) throws NSQException {
    return delegate.publishAndGetReceipt(message);
  }

  @Override
  public List<byte[]> publishMulti(List<byte[]> messages, Topic topic, int batchSize)
      throws NSQException {
    return delegate.publishMulti(messages, topic, batchSize);
  }

  /**
   * wrap publish method to add tracing
   */
  @Override
  public void publish(Message message) throws NSQException {
    Map<String, Object> headers = getHeaders(message);
    //set back if origin headers is null
    message.setJsonHeaderExt(headers);
    //trace here
    Span span = tracing.tracer().nextSpan();
    tracing.propagation().keys().forEach(headers::remove);

    if (!span.isNoop()) {
      if (remoteServiceName != null) {
        span.remoteServiceName(remoteServiceName);
      }
      span.tag(NsqTags.NSQ_TOPIC_TAG, message.getTopic().getTopicText())
          .name("send")
          .kind(Span.Kind.PRODUCER)
          .start();
    }
    injector.inject(span.context(), headers);
    try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
      delegate.publish(message);
    } catch (RuntimeException | Error e) {
      span.error(e); // finish as an exception means the callback won't finish the span
      throw e;
    }finally {
      span.finish();
    }
  }

  @Override
  public void publish(byte[] message, String topic) throws NSQException {
    this.publish(message, new Topic(topic));
  }

  @Override
  public void publish(byte[] message, Topic topic) throws NSQException {
    Message msg = Message.create(topic, message);
    this.publish(msg);
  }

  @Override
  public void publishMulti(List<byte[]> messages, String topic) throws NSQException {
    delegate.publishMulti(messages, topic);
  }

  @Override
  public void publishMulti(List<byte[]> messages, Topic topic) throws NSQException {
    delegate.publishMulti(messages, topic);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public NSQConfig getConfig() {
    return delegate.getConfig();
  }

  @Override
  public void incoming(NSQFrame frame, NSQConnection conn) throws NSQException {
    delegate.incoming(frame, conn);
  }

  @Override
  public void backoff(NSQConnection conn) {
    delegate.backoff(conn);
  }

  @Override
  public boolean validateHeartbeat(NSQConnection conn) {
    return delegate.validateHeartbeat(conn);
  }

  @Override
  public Set<NSQConnection> clearDataNode(Address address) {
    return delegate.clearDataNode(address);
  }

  @Override
  public void close(NSQConnection conn) {
    delegate.close(conn);
  }


  private static Map<String, Object> getHeaders(Message message) {
    Object jsonHeaderExt = message.getJsonHeaderExt();
    if (jsonHeaderExt == null) {
      return new HashMap<>();
    }

    return (Map<String, Object>) jsonHeaderExt;
  }
}
