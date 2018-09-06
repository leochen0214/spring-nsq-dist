package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;

import org.springframework.messaging.Message;

import java.lang.reflect.Type;

/**
 * A nsq-specific {@link Message} converter strategy.
 *
 * @author: clong
 * @date: 2018-09-02
 */
public interface NSQMessageConverter extends MessageConverter {

  /**
   * Convert a {@link NSQMessage} to a {@link Message}.
   *
   * @param message     the nsq message.
   * @param consumer    the consumer
   * @param payloadType the required payload type.
   * @return the message.
   */
  Message<?> toSpringMessage(NSQMessage message, Consumer consumer, Type payloadType, boolean unpackMessage);

  /**
   * Convert a message to a producer message.
   *
   * @param message      the message.
   * @param defaultTopic the default topic to use if no header found.
   * @return the producer record.
   */
  com.youzan.nsq.client.entity.Message fromSpringMessage(Message<?> message, String defaultTopic);


  /**
   * Convert message payload to a producer message
   *
   * @param payload  the message payload
   * @param topic the topic
   * @param <T>   payload type
   * @return the Producer Message
   */
  <T> com.youzan.nsq.client.entity.Message fromPayload(T payload, String topic);
}
