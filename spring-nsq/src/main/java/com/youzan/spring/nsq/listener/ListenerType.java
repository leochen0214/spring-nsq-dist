package com.youzan.spring.nsq.listener;

/**
 * @author: clong
 * @date: 2018-09-01
 */
public enum ListenerType {
  /**
   * Acknowledging and consumer aware.
   */
  ACKNOWLEDGING_CONSUMER_AWARE,

  /**
   * Consumer aware.
   */
  CONSUMER_AWARE,

  /**
   * Acknowledging.
   */
  ACKNOWLEDGING,

  /**
   * Simple.
   */
  SIMPLE

}
