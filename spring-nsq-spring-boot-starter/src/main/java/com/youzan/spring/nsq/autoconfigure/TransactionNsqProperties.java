package com.youzan.spring.nsq.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

  /**
   * 事务性消息补偿任务参数属性配置
   */
  @NestedConfigurationProperty
  private JobProperties compensationJob;

  /**
   * 事务性消息清理任务参数属性配置
   */
  @NestedConfigurationProperty
  private JobProperties cleanJob;


  @Getter
  @Setter
  private static class JobProperties {

    /**
     * cron表达式, 格式为:  Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (Year是可选项)
     */
    private String cron;

    /**
     * job总分片数
     */
    private int shardingTotalCount = 1;

    /**
     * job parameters
     */
    private String jobParameter;

    /**
     * job工作线程池core-pool-size
     */
    private int corePoolSize = 1;

    /**
     * job工作线程池大小max-pool-size
     */
    private int maxPoolSize = 10;

    /**
     * Keep-alive time in seconds
     */
    private int keepAliveSeconds = 60;

    /**
     * Queue capacity for the ThreadPoolTaskExecutor. If not specified, the default will be
     * Integer.MAX_VALUE (i.e. unbounded).
     */
    private int queueCapacity = 100;

  }

}
