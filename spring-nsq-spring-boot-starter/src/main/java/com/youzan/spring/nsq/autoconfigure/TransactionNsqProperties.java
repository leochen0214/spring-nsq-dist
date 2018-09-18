package com.youzan.spring.nsq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-18
 */
@ConfigurationProperties(prefix = "spring.nsq.transaction.message")
@Data
public class TransactionNsqProperties {

  private static final int DEFAULT_FETCH_DAYS = 7;
  private static final int DEFAULT_FETCH_SIZE = 200;
  private static final int DEFAULT_REMAIN_DAYS = 30;
  private static final int DEFAULT_DELETE_LIMIT_SIZE = 200;

  /**
   * 本地事务性消息表名称, 默认表名: nsq_transaction_message
   */
  private String tableName;

  /**
   * 事务性消息补偿任务每次查询多少天前的数据, 默认7天前
   */
  private int fetchDays = DEFAULT_FETCH_DAYS;

  /**
   * 事务性消息补偿任务每次查询数据量limit大小, 默认200条
   */
  private int fetchSize = DEFAULT_FETCH_SIZE;

  /**
   * 事务性消息清理任务每次清理多少天前的数据, 默认30天
   */
  private int remainDays = DEFAULT_REMAIN_DAYS;

  /**
   * 事务性消息清理任务每次删除多少条数据, 默认200条
   */
  private int deleteLimitSize = DEFAULT_DELETE_LIMIT_SIZE;

}
