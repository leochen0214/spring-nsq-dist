package com.youzan.spring.nsq.properties;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-01
 */
@Data
public class ProducerConfigProperties extends ConfigProperties {

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
