package com.youzan.spring.nsq.handler;

import com.youzan.nsq.client.Consumer;

/**
 * @author: clong
 * @date: 2018-09-01
 */
public interface GenericErrorHandler<T> {

  /**
   * Handle the exception.
   * @param thrownException The exception.
   * @param data the data.
   */
  void handle(Exception thrownException, T data);

  /**
   * Handle the exception.
   * @param thrownException The exception.
   * @param data the data.
   * @param consumer the consumer.
   */
  default void handle(Exception thrownException, T data, Consumer consumer) {
    handle(thrownException, data);
  }



}
