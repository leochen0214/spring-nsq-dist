package com.youzan.spring.nsq.core;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.Message;
import com.youzan.spring.nsq.exception.NsqMessagePublishFailedException;

/**
 * The basic NSQ operations api
 *
 * @author: clong
 * @date: 2018-09-04
 */
public interface NsqOperations {


  /**
   * send with payload data
   *
   * @param topic the topic name
   * @param data  the payload data
   * @param <T>   the payload type
   * @throws NsqMessagePublishFailedException throws exception when message send failed
   */
  <T> void send(String topic, T data) throws NsqMessagePublishFailedException;

  /**
   * send with nsq client origin Message
   */
  void send(Message nsqMessage) throws NsqMessagePublishFailedException;

  /**
   * send with Spring-Messaging message
   *
   * @param topic         the topic name
   * @param springMessage the Spring-Messaging message
   */
  void send(String topic, org.springframework.messaging.Message<?> springMessage)
      throws NsqMessagePublishFailedException;


  /**
   * send with ProducerCallback
   *
   * @param callback the ProducerCallback
   * @throws NsqMessagePublishFailedException throws exception when message send failed
   */
  void execute(ProducerCallback callback) throws NsqMessagePublishFailedException;


  interface ProducerCallback {

    void doExecute(Producer producer);
  }

}
