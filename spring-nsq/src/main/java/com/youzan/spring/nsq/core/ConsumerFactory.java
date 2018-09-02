package com.youzan.spring.nsq.core;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

/**
 * The strategy to produce a {@link Consumer} instance(s).
 *
 * @author: clong
 * @date: 2018-08-29
 */
public interface ConsumerFactory {

  /**
   * Create a consumer.
   */
  Consumer createConsumer(ConsumerConfigProperties properties);


  /**
   * Return true if consumers created by this factory use auto commit.
   *
   * @return true if auto commit.
   */
  boolean isAutoCommit();


  /**
   * Return the container properties
   */
  ConsumerConfigProperties getConfigurationProperties();


}
