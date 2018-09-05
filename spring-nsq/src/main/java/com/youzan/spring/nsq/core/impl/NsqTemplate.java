package com.youzan.spring.nsq.core.impl;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.NsqOperations;
import com.youzan.spring.nsq.core.ProducerFactory;
import com.youzan.spring.nsq.exception.NsqMessagePublishFailedException;
import com.youzan.spring.nsq.support.converter.JsonMessageConverter;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author: clong
 * @date: 2018-09-04
 */
public class NsqTemplate implements NsqOperations {

  private static final Logger logger = LoggerFactory.getLogger(NsqTemplate.class);

  private final NSQMessageConverter messageConverter;
  private final ProducerFactory producerFactory;

  public NsqTemplate(ProducerFactory producerFactory) {
    this(producerFactory, new JsonMessageConverter());
  }

  public NsqTemplate(ProducerFactory producerFactory, NSQMessageConverter messageConverter) {
    this.messageConverter = messageConverter;
    this.producerFactory = producerFactory;
  }


  @Override
  public <T> void send(String topic, T data) {
    if (data == null) {
      return;
    }
    doSend(messageConverter.fromPayload(data, topic));
  }

  @Override
  public void send(Message nsqMessage) throws NsqMessagePublishFailedException {
    doSend(nsqMessage);
  }

  @Override
  public void send(String topic, org.springframework.messaging.Message<?> springMessage) {
    doSend(messageConverter.fromSpringMessage(springMessage, topic));
  }

  @Override
  public void execute(ProducerCallback callback) {
    Assert.notNull(callback, "ProducerCallback can't be null");
    callback.doExecute(getProducer());
  }


  protected void doSend(Message nsqMessage) {
    try {
      getProducer().publish(nsqMessage);
    } catch (NSQException e) {
      logger.warn("nsq message send failed, topic={}, message={}",
                  nsqMessage.getTopic().getTopicText(), nsqMessage, e);
      throw new NsqMessagePublishFailedException("nsq message send failed", e);
    }
  }

  protected Producer getProducer(){
    return producerFactory.createProducer();
  }


}
