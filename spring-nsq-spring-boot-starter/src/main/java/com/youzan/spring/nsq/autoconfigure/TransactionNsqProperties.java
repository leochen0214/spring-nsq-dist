package com.youzan.spring.nsq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-18
 */
@ConfigurationProperties(prefix = "spring.nsq.transaction")
@Data
public class TransactionNsqProperties {

  /**
   * 本地事务性消息表名称, 默认表名: nsq_transaction_message
   */
  private String tableName;

}
