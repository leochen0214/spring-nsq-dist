package com.youzan.spring.nsq.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * NSQ message send failed exception
 *
 * @author: clong
 * @date: 2018-09-04
 */
public class NsqMessagePublishFailedException extends NestedRuntimeException {

  private static final long serialVersionUID = 6517029038927387578L;


  public NsqMessagePublishFailedException(String msg) {
    super(msg);
  }


  public NsqMessagePublishFailedException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
