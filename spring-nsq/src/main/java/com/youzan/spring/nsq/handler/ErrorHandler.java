package com.youzan.spring.nsq.handler;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.listener.MessageListenerContainer;

/**
 * @author: clong
 * @date: 2018-09-01
 */
public interface ErrorHandler extends GenericErrorHandler<NSQMessage> {

  /**
   * Handle the exception.
   *
   * @param thrownException the exception.
   * @param message         the remaining message including the one that failed.
   * @param consumer        the consumer.
   * @param container       the container.
   */
  default void handle(Exception thrownException, NSQMessage message, Consumer consumer,
                      MessageListenerContainer container) {
    handle(thrownException, null);
  }


}
