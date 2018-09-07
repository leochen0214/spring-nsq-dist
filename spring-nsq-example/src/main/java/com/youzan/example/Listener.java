package com.youzan.example;

import com.youzan.spring.nsq.annotation.NsqListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Component
public class Listener {
  private static final Logger logger = LoggerFactory.getLogger(Listener.class);

//  @NsqListener(topics = "JavaTesting-Ext", channel = "default", unpackMessage = true)
//  public void listener1(UniformPayEvent payEvent) {
//    logger.info("listener1 payEvent={}", payEvent);
//  }


//  @NsqListener(topics = "${topic}", channel = "default", errorHandler ="personListenerErrorHandler")
  public void listener2(Person person) {
    logger.info("listener2 person={}", person);
  }


  @NsqListener(topics = "JavaTesting-Finish", channel = "default")
  public void listener3(List<Person> personList) {
    logger.info("listener3 persons={}", personList);
  }

//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener4(NSQMessage message) {
//    System.out.println(
//        "listener4, message content=" + message.getReadableContent() + ", headers=" + message
//            .getJsonExtHeader());
//  }
//
//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener5(NSQMessage message, Consumer consumer) {
//    System.out.println(
//        "listener5, message content=" + message.getReadableContent() + ", headers=" + message
//            .getJsonExtHeader());
//  }
//
//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener6(Message<?> message) {
//    System.out.println(
//        "listener6, message paylod=" + message.getPayload() + ", headers=" + message.getHeaders());
//  }
//
//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener7(Message<Person> message) {
//    System.out.println(
//        "listener7, message paylod=" + message.getPayload() + ", headers=" + message.getHeaders());
//  }
//
//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener8(@Payload Person message) {
//    System.out.println("listener8, message paylod=" + message);
//  }

//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener9(@Payload Person message, @Headers MessageHeaders headers) {
    logger.info("listener9, message paylod={}, headers={}", message, headers);
  }

//  @NsqListener(topics = "${topic}", channel = "default")
//  public void listener10(@Payload Person message, @Headers Map<String, Object> headers) {
//    System.out.println("listener10, message paylod=" + message + ", headers=" + headers);
//  }


}
