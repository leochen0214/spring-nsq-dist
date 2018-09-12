package com.youzan.spring.nsq.transaction.builder;

import com.youzan.spring.nsq.transaction.domain.MessageContext;

/**
 * @author: clong
 * @date: 2018-09-12
 */
public class MessageContextBuilder {

  private Object payload;

  private String businessKey;

  private String eventType;

  private int shardingId;

  private boolean wrapMessage;


  private MessageContextBuilder() {

  }


  public static MessageContextBuilder builder() {
    return new MessageContextBuilder();
  }

  public MessageContext build() {
    MessageContext context = new MessageContext();
    context.setPayload(payload);
    context.setBusinessKey(businessKey);
    context.setEventType(eventType);
    context.setShardingId(shardingId);
    context.setWrapMessage(wrapMessage);

    return context;
  }


  public MessageContextBuilder payload(Object payload) {
    this.payload = payload;
    return this;
  }

  public MessageContextBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public MessageContextBuilder eventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public MessageContextBuilder shardingId(int shardingId) {
    this.shardingId = shardingId;
    return this;
  }

  public MessageContextBuilder wrapMessage(boolean wrapMessage) {
    this.wrapMessage = wrapMessage;
    return this;
  }

}
