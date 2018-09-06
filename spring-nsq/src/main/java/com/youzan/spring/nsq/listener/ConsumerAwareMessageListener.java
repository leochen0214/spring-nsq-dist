package com.youzan.spring.nsq.listener;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@FunctionalInterface
public interface ConsumerAwareMessageListener extends MessageListener {

  /**
   * Invoked with data from kafka. Containers should never call this since it they will detect we
   * are a consumer aware acknowledging listener.
   *
   * @param data the data to be processed.
   */
  @Override
  default void onMessage(NSQMessage data, boolean unpackMessage) {
    throw new UnsupportedOperationException("Container should never call this");
  }

  @Override
  void onMessage(NSQMessage data, Consumer consumer, boolean unpackMessage);


}
