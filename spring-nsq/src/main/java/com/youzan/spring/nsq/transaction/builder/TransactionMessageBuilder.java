package com.youzan.spring.nsq.transaction.builder;

import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import java.util.Date;

/**
 * @author: clong
 * @date: 2018-09-12
 */
public class TransactionMessageBuilder {

  private Long id;
  private Date createdAt;
  private String businessKey;
  private String eventType;
  private int shardingId;
  private Integer state;
  private String env;
  private String payload;
  private String topic;


  private TransactionMessageBuilder() {

  }

  public static TransactionMessageBuilder builder() {
    return new TransactionMessageBuilder();
  }

  public TransactionMessage build() {
    TransactionMessage result = new TransactionMessage();
    result.setId(id);
    result.setCreatedAt(createdAt);
    result.setShardingId(shardingId);
    result.setBusinessKey(businessKey);
    result.setEventType(eventType);
    result.setEnv(env);
    result.setState(state);
    result.setPayload(payload);
    result.setTopic(topic);

    return result;
  }

  public TransactionMessageBuilder topic(String topic) {
    this.topic = topic;
    return this;
  }

  public TransactionMessageBuilder payload(String payload) {
    this.payload = payload;
    return this;
  }


  public TransactionMessageBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public TransactionMessageBuilder eventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public TransactionMessageBuilder env(String env) {
    this.env = env;
    return this;
  }

  public TransactionMessageBuilder shardingId(int shardingId) {
    this.shardingId = shardingId;
    return this;
  }

  public TransactionMessageBuilder state(Integer state) {
    this.state = state;
    return this;
  }

  public TransactionMessageBuilder id(Long id) {
    this.id = id;
    return this;
  }

  public TransactionMessageBuilder createdAt(Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }


}
