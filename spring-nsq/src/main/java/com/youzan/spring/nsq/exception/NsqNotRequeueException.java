package com.youzan.spring.nsq.exception;

/**
 * Not requeue exception
 *
 * @author: clong
 * @date: 2018-10-22
 */
public class NsqNotRequeueException extends NsqException {

  public NsqNotRequeueException(String msg) {
    super(msg);
  }

  public NsqNotRequeueException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
