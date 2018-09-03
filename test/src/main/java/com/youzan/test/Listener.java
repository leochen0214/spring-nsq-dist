package com.youzan.test;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.annotation.NsqListener;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Component
public class Listener {

//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener1(String person) {
    System.out.println("listener1 personStr=" + person);
  }


  @NsqListener(topics = "${topic}", channel = "default")
  public void listener2(Person person) {
    System.out.println("listener2 person=" + person);
  }


  @NsqListener(topics = "JavaTesting-Finish", channel = "default")
  public void listener3(List<Person> person) {
    System.out.println("listener3 persons=" + person);
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener4(NSQMessage message) {
    System.out.println(
        "listener4, message content=" + message.getReadableContent() + ", headers=" + message
            .getJsonExtHeader());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener5(NSQMessage message, Consumer consumer) {
    System.out.println(
        "listener5, message content=" + message.getReadableContent() + ", headers=" + message
            .getJsonExtHeader());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener6(Message<?> message) {
    System.out.println(
        "listener6, message paylod=" + message.getPayload() + ", headers=" + message.getHeaders());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener7(Message<Person> message) {
    System.out.println(
        "listener7, message paylod=" + message.getPayload() + ", headers=" + message.getHeaders());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener8(@Payload Person message) {
    System.out.println("listener8, message paylod=" + message);
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener9(@Payload Person message, @Headers MessageHeaders headers) {
    System.out.println("listener9, message paylod=" + message + ", headers=" + headers);
  }

  @NsqListener(topics = "${topic}", channel = "default")
  public void listener10(@Payload Person message, @Headers Map<String, Object> headers) {
    System.out.println("listener10, message paylod=" + message + ", headers=" + headers);
  }


}
