package com.youzan.brave.nsq.client;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.core.ConnectionManager;
import com.youzan.nsq.client.core.NSQConnection;
import com.youzan.nsq.client.entity.Address;
import com.youzan.nsq.client.entity.ExtVer;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.nsq.client.exception.NSQInvalidMessageException;
import com.youzan.nsq.client.network.frame.MessageFrame;
import com.youzan.nsq.client.network.frame.NSQFrame;
import com.youzan.nsq.client.network.frame.OrderedMessageFrame;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import brave.Span;
import brave.SpanCustomizer;
import brave.Tracer;
import brave.Tracing;
import brave.internal.Nullable;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;

import static com.youzan.nsq.client.network.frame.NSQFrame.FrameType.MESSAGE_FRAME;

/**
 * @author: clong
 * @date: 2018-08-28
 */
public class TracingConsumer implements Consumer {

  private final Tracing tracing;
  private final TraceContext.Extractor<Map<String, Object>> extractor;
  private final Consumer delegate;
  @Nullable
  private final String remoteServiceName;

  public TracingConsumer(Tracing tracing, Consumer delegate, @Nullable String remoteServiceName) {
    this.delegate = delegate;
    this.tracing = tracing;
    this.extractor = tracing.propagation().extractor(NsqPropagation.HEADER_GETTER);
    this.remoteServiceName = remoteServiceName;
  }


  @Override
  public void subscribe(String... topics) {
    delegate.subscribe(topics);
  }

  @Override
  public void subscribe(Topic... topics) {
    delegate.subscribe(topics);
  }

  @Override
  public void start() throws NSQException {
    delegate.start();
  }

  @Override
  public void finish(NSQMessage message) throws NSQException {
    delegate.finish(message);
  }

  @Override
  public void touch(NSQMessage message) throws NSQException {
    delegate.touch(message);
  }

  @Override
  public void requeue(NSQMessage message) throws NSQException {
    delegate.requeue(message);
  }

  @Override
  public void setAutoFinish(boolean autoFinish) {
    delegate.setAutoFinish(autoFinish);
  }

  @Override
  public void setMessageHandler(MessageHandler handler) {
    delegate.setMessageHandler(handler);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void backoff(Topic topic) {
    delegate.backoff(topic);
  }

  @Override
  public void backoff(Topic topic, CountDownLatch latch) {
    delegate.backoff(topic, latch);
  }

  @Override
  public void resume(Topic topic) {
    delegate.resume(topic);
  }

  @Override
  public void resume(Topic topic, CountDownLatch latch) {
    delegate.resume(topic, latch);
  }

  @Override
  public NSQConfig getConfig() {
    return delegate.getConfig();
  }

  @Override
  public ConnectionManager getConnectionManager() {
    return delegate.getConnectionManager();
  }

  @Override
  public void incoming(NSQFrame frame, NSQConnection conn) throws NSQException {
    if (!isMessageFrame(frame)) {
      delegate.incoming(frame, conn);
    } else {
      final MessageFrame msg = (MessageFrame) frame;
      final NSQMessage message = createNSQMessage(msg, conn);

      // Grab any span from the record. The topic and key are automatically tagged
      Span span = NsqTracing.create(tracing).nextSpan(message);

      if (!span.isNoop()) {
        span.name("incoming").kind(Span.Kind.CONSUMER)
            .tag(NsqTags.NSQ_TOPIC_TAG, message.getTopicInfo().getTopicName());
        if (remoteServiceName != null) {
          span.remoteServiceName(remoteServiceName);
        }
        span.start();// span won't be shared by other records
      }

      // Below is the same setup as any synchronous tracing
      try (Tracer.SpanInScope ws = tracing.tracer()
          .withSpanInScope(span)) { // so logging can see trace ID
        logger.info("delete to origin Producer to process, message={}", message);
        delegate.incoming(frame, conn); // do the actual work
      } catch (RuntimeException | Error e) {
        span.error(e); // make sure any error gets into the span before it is finished
        throw e;
      } finally {
        span.finish(); // ensure the span representing this processing completes.
      }

    }
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


  @Override
  public void requeue(NSQMessage nsqMessage, int i) throws NSQException {
    delegate.requeue(nsqMessage, i);
  }

  @Override
  public void backoff(long l) {
    delegate.backoff(l);
  }

  @Override
  public void resume() {
    delegate.resume();
  }

  private boolean isMessageFrame(NSQFrame frame) {
    return MESSAGE_FRAME == frame.getType();
  }


  private NSQMessage createNSQMessage(final MessageFrame msgFrame, final NSQConnection conn)
      throws NSQInvalidMessageException {
    NSQMessage msg;
    if (getConfig().isOrdered() && msgFrame instanceof OrderedMessageFrame) {
      OrderedMessageFrame orderedMsgFrame = (OrderedMessageFrame) msgFrame;
      msg =
          new NSQMessage(orderedMsgFrame.getTimestamp(), orderedMsgFrame.getAttempts(),
                         orderedMsgFrame.getMessageID(),
                         orderedMsgFrame.getInternalID(), orderedMsgFrame.getTractID(),
                         orderedMsgFrame.getDiskQueueOffset(),
                         orderedMsgFrame.getDiskQueueDataSize(),
                         msgFrame.getMessageBody(), conn.getAddress(), conn.getId(),
                         getConfig().getNextConsumingInSecond(), conn.getTopic(), conn.isExtend());
    } else {
      msg = new NSQMessage(msgFrame.getTimestamp(), msgFrame.getAttempts(), msgFrame.getMessageID(),
                           msgFrame.getInternalID(), msgFrame.getTractID(),
                           msgFrame.getMessageBody(), conn.getAddress(), conn.getId(),
                           getConfig().getNextConsumingInSecond(), conn.getTopic(),
                           conn.isExtend());
    }
    if (conn.isExtend()) {
      ExtVer extVer = ExtVer.getExtVersion(msgFrame.getExtVerBytes());
      try {
        msg.parseExtContent(extVer, msgFrame.getExtBytes());
      } catch (IOException e) {
        throw new NSQInvalidMessageException("Fail to parse ext content for incoming message.", e);
      }
    }
    return msg;
  }


  private Span nextSpan(NSQMessage message) {
    TraceContextOrSamplingFlags extracted = extractAndClearHeaders(message);
    Span result = tracing.tracer().nextSpan(extracted);
    if (extracted.context() == null && !result.isNoop()) {
      addTags(message, result);
    }
    return result;
  }

  private TraceContextOrSamplingFlags extractAndClearHeaders(NSQMessage message) {
    TraceContextOrSamplingFlags extracted = extractor.extract(message.getJsonExtHeader());
    // clear propagation headers if we were able to extract a span
    if (!extracted.equals(TraceContextOrSamplingFlags.EMPTY)) {
      tracing.propagation().keys().forEach(key -> message.getJsonExtHeader().remove(key));
    }
    return extracted;
  }

  /**
   * When an upstream context was not present, lookup keys are unlikely added
   */
  private static void addTags(NSQMessage record, SpanCustomizer result) {
    result.tag(NsqTags.NSQ_TOPIC_TAG, record.getTopicInfo().getTopicName());
  }


}
