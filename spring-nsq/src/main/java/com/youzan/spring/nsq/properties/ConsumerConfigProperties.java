package com.youzan.spring.nsq.properties;

import com.youzan.spring.nsq.core.RequeuePolicy;

import java.util.Collection;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-01
 */
@Data
public class ConsumerConfigProperties extends ConfigProperties {

  /**
   * The default {@link #setShutdownTimeout(long) shutDownTimeout} (ms).
   */
  public static final long DEFAULT_SHUTDOWN_TIMEOUT = 10_000L;


  /**
   * The message listener; must be a {@link com.youzan.spring.nsq.listener.MessageListener}
   */
  private Object messageListener;

  /**
   * The timeout for shutting down the container. This is the maximum amount of time that the
   * invocation to {@code #stop(Runnable)} will block for, before returning.
   */
  private long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

  /**
   * The re-queue policy
   */
  private RequeuePolicy requeuePolicy;

  private boolean unpackMessage;


  private Collection<String> topics;

  private String channel;

  /**
   * The set of messages is ordered in one specified partition
   */
  private Boolean ordered;

  /**
   * The message partition id
   */
  private Integer partitionID;

  private boolean autoFinish;

  /**
   * The consumer work thread pool size
   */
  private Integer consumerWorkerPoolSize;

  /**
   * The message interactive timeout(ms) between client and server
   */
  private Integer messageInteractiveTimeout;

  /**
   * The tcp connection timeout(ms)
   */
  private Integer connectTimeout;

  /**
   * The connection heart beat validation timeout(ms) for consumer
   */
  private Integer queryTimeout;

  /**
   * specify time(unit: s) elapse before a requeued message sent from NSQd
   */
  private Integer nextConsumingInSecond;

  private Integer attemptWarningThreshold;

  private Integer attemptErrorThreshold;


}
