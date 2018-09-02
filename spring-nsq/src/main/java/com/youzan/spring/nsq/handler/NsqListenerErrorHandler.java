package com.youzan.spring.nsq.handler;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.exception.ListenerExecutionFailedException;

/**
 * @author: clong
 * @date: 2018-08-31
 */
public interface NsqListenerErrorHandler {

  /**
   * Handle the error.
   *
   * @param message   the nsq message.
   * @param exception the exception the listener threw, wrapped in a {@link
   *                  ListenerExecutionFailedException}.
   * @return the return value is ignored unless the annotated method has a
   * @throws Exception an exception which may be the original or different.
   */
  Object handleError(NSQMessage message, ListenerExecutionFailedException exception)
      throws Exception;

  /**
   * Handle the error.
   *
   * @param message   the nsq message.
   * @param exception the exception the listener threw, wrapped in a {@link
   *                  ListenerExecutionFailedException}.
   * @param consumer  the consumer.
   * @throws Exception an exception which may be the original or different.
   */
  default Object handleError(NSQMessage message, ListenerExecutionFailedException exception,
                             Consumer consumer) throws Exception {

    return handleError(message, exception);
  }


}
