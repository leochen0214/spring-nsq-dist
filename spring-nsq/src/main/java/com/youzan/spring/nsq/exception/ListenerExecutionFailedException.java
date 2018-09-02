package com.youzan.spring.nsq.exception;

/**
 * The listener specific {@link NsqException} extension.
 *
 * @author: clong
 * @date: 2018-08-31
 */
public class ListenerExecutionFailedException extends NsqException {

  public ListenerExecutionFailedException(String msg) {
    super(msg);
  }

  public ListenerExecutionFailedException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
