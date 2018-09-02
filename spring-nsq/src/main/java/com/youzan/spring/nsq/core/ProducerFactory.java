package com.youzan.spring.nsq.core;

import com.youzan.nsq.client.Producer;
import com.youzan.spring.nsq.properties.ProducerConfigProperties;

/**
 * The strategy to produce a {@link Producer} instance(s).
 *
 * @author: clong
 * @date: 2018-08-29
 */
public interface ProducerFactory {

  /**
   * create a Producer
   */
  Producer createProducer();

  ProducerConfigProperties getConfigurationProperties();

}
