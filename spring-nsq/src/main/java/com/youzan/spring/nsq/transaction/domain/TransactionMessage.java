package com.youzan.spring.nsq.transaction.domain;

import java.util.Date;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-11
 */
@Data
public class TransactionMessage {

  /**
   * 主键id
   */
  protected Long id;

  /**
   * 创建时间
   */
  private Date createdAt;

  /**
   * 修改时间
   */
  private Date updatedAt;

  /**
   * 分片id
   */
  private Integer shardingId;

  /**
   * 业务key，比如tradeNo
   */
  private String businessKey;

  /**
   * 事件类型
   */
  private String eventType;

  /**
   * 消息投递状态, 0-CREATED, 1-PUBLISHED
   */
  private Integer state;

  /**
   * 当前环境
   */
  private String env;

  /**
   * 消息体
   */
  private String payload;

  /**
   * 消息topic
   */
  private String topic;


}
