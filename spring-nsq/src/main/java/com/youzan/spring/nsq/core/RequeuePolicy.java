package com.youzan.spring.nsq.core;

import com.youzan.nsq.client.entity.NSQMessage;

/**
 * NSQ message re-queue policy
 *
 * @author: clong
 * @date: 2018-09-05
 */
public interface RequeuePolicy {

  void requeue(NSQMessage message);
}
