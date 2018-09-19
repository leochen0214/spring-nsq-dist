package com.youzan.spring.nsq.compensation.job;

import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.Date;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * deletes has already published success message from message table.
 *
 * @author: clong
 * @date: 2018-09-12
 */
@Slf4j
public class MessageTableCleanJob implements SimpleJob {
  private static final String PREFIX = "[事务性消息清理任务]";

  @Resource
  private TransactionMessageDao transactionMessageDao;


  @Value("${spring.nsq.transaction.message.remain-days: 30}")
  private int remainDays;

  @Value("${spring.nsq.transaction.message.delete-limit-size: 200}")
  private int fetchSize;


  @Override
  public void execute(ShardingContext shardingContext) {
    Date date = toDate(ZonedDateTime.now().minusDays(remainDays));

    int rows = transactionMessageDao.batchDeleteOfNonSharding(date, fetchSize);

    log.info("{} 清理{}天前数据, 本次清理共删除 {} 条记录", PREFIX, remainDays, rows);
  }


  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }
}
