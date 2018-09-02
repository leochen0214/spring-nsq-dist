package com.youzan.spring.nsq.listener;

import com.youzan.nsq.client.Consumer;

/**
 * Top level interface for listeners.
 *
 * @param <T> the type received by the listener.
 *
 * @author: clong
 * @date: 2018-08-29
 */
@FunctionalInterface
public interface GenericMessageListener<T> {

  /**
   * Invoked with data from nsq.
   *
   * @param data the data to be processed.
   */
  void onMessage(T data);


  /**
   * Invoked with data from nsq and provides access to the {@link Consumer}. The default
   * implementation throws {@link UnsupportedOperationException}.
   *
   * @param data     the data to be processed.
   * @param consumer the consumer.
   */
  default void onMessage(T data, Consumer consumer) {
    throw new UnsupportedOperationException("Container should never call this");
  }


}
