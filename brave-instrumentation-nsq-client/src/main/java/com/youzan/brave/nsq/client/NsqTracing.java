package com.youzan.brave.nsq.client;


import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.NSQMessage;

import java.util.Map;

import brave.Span;
import brave.SpanCustomizer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;

/**
 * @author: clong
 * @date: 2018-08-28
 */
public class NsqTracing {

  public static NsqTracing create(Tracing tracing) {
    return new NsqTracing.Builder(tracing).build();
  }

  public static NsqTracing.Builder newBuilder(Tracing tracing) {
    return new NsqTracing.Builder(tracing);
  }

  public static final class Builder {
    final Tracing tracing;
    String remoteServiceName = "nsq";

    Builder(Tracing tracing) {
      if (tracing == null) throw new NullPointerException("tracing == null");
      this.tracing = tracing;
    }

    /**
     * The remote service name that describes the broker in the dependency graph. Defaults to
     * "kafka"
     */
    public NsqTracing.Builder remoteServiceName(String remoteServiceName) {
      this.remoteServiceName = remoteServiceName;
      return this;
    }

    public NsqTracing build() {
      return new NsqTracing(this);
    }
  }

  private final Tracing tracing;
  private final TraceContext.Extractor<Map<String,Object>> extractor;
  private final String remoteServiceName;

  NsqTracing(NsqTracing.Builder builder) { // intentionally hidden constructor
    this.tracing = builder.tracing;
    this.extractor = tracing.propagation().extractor(NsqPropagation.HEADER_GETTER);
    this.remoteServiceName = builder.remoteServiceName;
  }

  /**
   * Extracts or creates a {@link Span.Kind#CONSUMER} span for each message received. This span is
   * injected onto each message so it becomes the parent when a processor later calls
   */
  public Consumer consumer(Consumer consumer) {
    return new TracingConsumer(tracing, consumer, remoteServiceName);
  }

  /** Starts and propagates {@link Span.Kind#PRODUCER} span for each message sent. */
  public Producer producer(Producer producer) {
    return new TracingProducer(tracing, producer, remoteServiceName);
  }

  /**
   * Use this to create a span for processing the given message. Note: the result has no name and is
   * not started.
   *
   * <p>This creates a child from identifiers extracted from the message headers, or a new span if
   * one couldn't be extracted.
   */
  public Span nextSpan(NSQMessage message) {
    TraceContextOrSamplingFlags extracted = extractAndClearHeaders(message);
    Span span = tracing.tracer().nextSpan(extracted);
    if (extracted.context() == null && !span.isNoop()) {
      addTags(message, span);
    }
    return span;
  }

  TraceContextOrSamplingFlags extractAndClearHeaders(NSQMessage record) {
    TraceContextOrSamplingFlags extracted = extractor.extract(record.getJsonExtHeader());
    // clear propagation headers if we were able to extract a span
    if (!extracted.equals(TraceContextOrSamplingFlags.EMPTY)) {
      tracing.propagation().keys().forEach(key -> record.getJsonExtHeader().remove(key));
    }
    return extracted;
  }

  /** When an upstream context was not present, lookup keys are unlikely added */
  static void addTags(NSQMessage record, SpanCustomizer result) {
    result.tag(NsqTags.NSQ_TOPIC_TAG, record.getTopicInfo().getTopicName());
  }


}
