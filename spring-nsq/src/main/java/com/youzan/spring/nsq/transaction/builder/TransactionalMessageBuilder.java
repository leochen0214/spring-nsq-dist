package com.youzan.spring.nsq.transaction.builder;

import com.youzan.spring.nsq.transaction.domain.TransactionalMessage;

/**
 * @author: clong
 * @date: 2018-09-12
 */
public class TransactionalMessageBuilder {

  private String businessKey;
  private String eventType;
  private int shardingId;
  private Integer state;
  private String env;
  private String payload;


  private TransactionalMessageBuilder() {

  }

  public static TransactionalMessageBuilder builder() {
    return new TransactionalMessageBuilder();
  }

  public TransactionalMessage build(){
    TransactionalMessage result = new TransactionalMessage();
    result.setShardingId(shardingId);
    result.setBusinessKey(businessKey);
    result.setEventType(eventType);
    result.setEnv(env);
    result.setState(state);
    result.setPayload(payload);

    return result;
  }

  public TransactionalMessageBuilder payload(String payload) {
    this.payload = payload;
    return this;
  }


  public TransactionalMessageBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public TransactionalMessageBuilder eventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public TransactionalMessageBuilder env(String env) {
    this.env = env;
    return this;
  }

  public TransactionalMessageBuilder shardingId(int shardingId) {
    this.shardingId = shardingId;
    return this;
  }

  public TransactionalMessageBuilder state(Integer state) {
    this.state = state;
    return this;
  }


}
