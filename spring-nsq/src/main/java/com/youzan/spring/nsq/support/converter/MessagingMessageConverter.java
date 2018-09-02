package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;

import com.alibaba.fastjson.JSON;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class MessagingMessageConverter implements NSQMessageConverter {

  private static final String PARTITION_ID = "partitionId";

  @Override
  public Message<?> toSpringMessage(NSQMessage message, Consumer consumer, Type payloadType) {
    Map<String, Object> rawHeaders = message.getJsonExtHeader();
    if (rawHeaders == null) {
      rawHeaders = new HashMap<>();
    }
    rawHeaders.putIfAbsent(PARTITION_ID, message.getTopicInfo().getTopicPartition());
    MessageHeaders headers = new MessageHeaders(rawHeaders);
    return MessageBuilder.createMessage(extractAndConvertValue(message, payloadType), headers);
  }


  @Override
  public com.youzan.nsq.client.entity.Message fromSpringMessage(Message<?> message,
                                                                String defaultTopic) {
    MessageHeaders headers = message.getHeaders();

    Topic topic = new Topic(defaultTopic);
    Integer partitionId = headers.get(PARTITION_ID, Integer.class);
    if (partitionId != null) {
      topic.setPartitionID(partitionId);
    }

    Object payload = message.getPayload();
    String content = JSON.toJSONString(payload);

    com.youzan.nsq.client.entity.Message result =
        com.youzan.nsq.client.entity.Message.create(topic, content);
    result.setJsonHeaderExt(headers);
    return result;
  }

  /**
   * Subclasses can convert the value; by default, it's returned as provided by nsq.
   *
   * @param message the nsq message.
   * @param type    the required type.
   * @return the value.
   */
  protected Object extractAndConvertValue(NSQMessage message, Type type) {
    String content = message.getReadableContent();
    if (!StringUtils.hasText(content)) {
      return null;
    }


    if (type instanceof List){

    }

    try {
      return JSON.parseObject(content, type);
    } catch (Exception e) {
      return content;
    }

  }




}
