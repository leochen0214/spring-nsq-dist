package com.youzan.spring.nsq.transaction.domain;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-12
 */
@Data
public class MessageContext {

  private Object payload;

  private String businessKey;

  private String eventType;

  private int shardingId;

  private boolean wrapMessage;

}
