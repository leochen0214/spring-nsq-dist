package com.youzan.spring.nsq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-05
 */
@ConfigurationProperties(prefix = "spring.nsq")
@Data
public class NsqProperties {

  /**
   * nsq server lookup address
   */
  private String lookupAddress;

  //=================================//
  // consumer config properties
  //=================================//

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


  //=================================//
  // producer config properties
  //=================================//

  /**
   * Specify connection pool size for producer per nsqd partition, or default value(30) applies.
   */
  private Integer connectionPoolSize;

  /**
   * The timeout for waiting for connection for producer, when it waits for connection. Default is 200ms.
   */
  private Integer maxWaitTimeout;

  /**
   * The min number of idle connection number per topic in producer's connection pool
   */
  private Integer minIdle;

  /**
   * The publish message retry times, default value is 3, max value is 6.
   */
  private Integer publishRetryTimes;

  /**
   * The server's heartbeat time(ms), max-interval is 60 seconds.
   */
  private Integer heartbeatInterval;

  /**
   * number of milliseconds to sleep between runs of the idle object evictor thread
   */
  private Integer connectionEvictInterval;

}
