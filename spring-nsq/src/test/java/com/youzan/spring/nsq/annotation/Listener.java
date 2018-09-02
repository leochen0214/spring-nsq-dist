package com.youzan.spring.nsq.annotation;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;

import org.springframework.stereotype.Component;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class Listener {

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener1(NSQMessage message, Consumer consumer) {
    System.out.println("listener message=" + message.getReadableContent());
  }


}
