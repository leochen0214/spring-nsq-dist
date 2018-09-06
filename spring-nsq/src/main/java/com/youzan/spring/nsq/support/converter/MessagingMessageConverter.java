package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class MessagingMessageConverter implements NSQMessageConverter {

  private static final String PARTITION_ID = "partitionId";


  @Override
  public Message<?> toSpringMessage(NSQMessage message, Consumer consumer,
                                    Type payloadType, boolean unpackMessage) {
    Map<String, Object> rawHeaders = message.getJsonExtHeader();
    if (rawHeaders == null) {
      rawHeaders = new HashMap<>();
    }
    rawHeaders.putIfAbsent(PARTITION_ID, message.getTopicInfo().getTopicPartition());
    MessageHeaders headers = new MessageHeaders(rawHeaders);
    return MessageBuilder.createMessage(extractAndConvertValue(message, payloadType, unpackMessage), headers);
  }


  @Override
  public com.youzan.nsq.client.entity.Message fromSpringMessage(Message<?> message,
                                                                String defaultTopic) {
    Topic topic = new Topic(defaultTopic);
    MessageHeaders headers = message.getHeaders();
    if (headers != null) {
      Integer partitionId = headers.get(PARTITION_ID, Integer.class);
      if (partitionId != null) {
        topic.setPartitionID(partitionId);
      }
    }

    com.youzan.nsq.client.entity.Message result =
        com.youzan.nsq.client.entity.Message.create(topic, convertPayload(message.getPayload()));

    if (headers != null) {
      result.setJsonHeaderExt(headers);
    }
    return result;
  }

  @Override
  public <T> com.youzan.nsq.client.entity.Message fromPayload(T payload, String topic) {
    if (payload == null) {
      return null;
    }

    return com.youzan.nsq.client.entity.Message.create(new Topic(topic), convertPayload(payload));
  }

  /**
   * Subclasses can convert the value; by default, it's returned as provided by nsq.
   *
   * @param message the nsq message.
   * @param type    the required type.
   * @return the value.
   */
  protected Object extractAndConvertValue(NSQMessage message, Type type, boolean unpackMessage) {
    String content = message.getReadableContent();
    if (!StringUtils.hasText(content)) {
      return null;
    }

    return content;
  }

  /**
   * Subclasses can convert the payload; by default, it's sent unchanged to Kafka.
   *
   * @param payload the message.
   * @return the payload.
   */
  protected String convertPayload(Object payload) {
    if (payload == null) {
      return "";
    }

    return payload.toString();
  }


}
