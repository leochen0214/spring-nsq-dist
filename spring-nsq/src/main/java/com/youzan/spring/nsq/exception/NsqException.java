package com.youzan.spring.nsq.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * The Spring nsq specific {@link NestedRuntimeException} implementation.
 *
 * @author: clong
 * @date: 2018-08-31
 */
public class NsqException extends NestedRuntimeException {

  public NsqException(String msg) {
    super(msg);
  }

  public NsqException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
