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

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener1(String json) {
    logger.info("listener1 personJson={}", json);
  }


  @NsqListener(topics = "${topic}", channel = "default", errorHandler ="personListenerErrorHandler")
  public void listener2(Person person) {
    logger.info("listener2 person={}", person);
  }


//  @NsqListener(topics = "JavaTesting-Finish", channel = "default")
//  public void listener3(List<Person> personList) {
//    logger.info("listener3 persons={}", personList);
//  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener4(NSQMessage message) {
    logger.info("listener4 message content={}, , headers={}", message.getReadableContent(), message.getJsonExtHeader());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener5(NSQMessage message, Consumer consumer) {
    logger.info("listener5 message content={}, , headers={}", message.getReadableContent(), message.getJsonExtHeader());
  }

//  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
//  public void listener6(Message<?> message) {
//    logger.info("listener6 person={}", person);
//    System.out.println(
//        "listener6, message paylod=" + message.getPayload() + ", headers=" + message.getHeaders());
//  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener6(Message<Person> message) {
    logger.info("listener6 person={}, headers=", message.getPayload(), message.getHeaders());
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener7(@Payload Person message) {
    System.out.println("listener7, message paylod=" + message);
  }

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener8(@Payload Person message, @Headers MessageHeaders headers) {
    logger.info("listener8, message paylod={}, headers={}", message, headers);
  }

  @NsqListener(topics = "${topic}", channel = "default")
  public void listener9(@Payload Person message, @Headers Map<String, Object> headers) {
    System.out.println("listener9, message paylod=" + message + ", headers=" + headers);
  }


}
