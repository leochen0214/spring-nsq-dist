package com.youzan.spring.nsq.listener;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.Assert;

import java.util.Arrays;

import lombok.Data;

/**
 * Contains runtime properties for a listener container.
 *
 * @author: clong
 * @date: 2018-08-29
 */
@Data
public class ContainerProperties {

  /**
   * The default {@link #setShutdownTimeout(long) shutDownTimeout} (ms).
   */
  public static final long DEFAULT_SHUTDOWN_TIMEOUT = 10_000L;


  /**
   * Topic names.
   */
  private final String[] topics;




  /**
   * The message listener; must be a {@link com.youzan.spring.nsq.listener.MessageListener}
   */
  private Object messageListener;


  /**
   * The timeout for shutting down the container. This is the maximum amount of time that the
   * invocation to {@code #stop(Runnable)} will block for, before returning.
   */
  private long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;


  private TaskScheduler scheduler;


  private boolean logContainerConfig;


  private boolean missingTopicsFatal = true;

  public ContainerProperties(String... topics) {
    Assert.notEmpty(topics, "An array of topicPartitions must be provided");
    this.topics = Arrays.asList(topics).toArray(new String[topics.length]);
  }


}
