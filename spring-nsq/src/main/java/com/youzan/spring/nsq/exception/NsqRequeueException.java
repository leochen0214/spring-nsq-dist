package com.youzan.spring.nsq.exception;

/**
 * when throws this exception, nsq message will requeue, and republish it later.
 *
 * @author: clong
 * @date: 2018-12-01
 */
public class NsqRequeueException extends NsqException {

  public NsqRequeueException(String msg) {
    super(msg);
  }
}
