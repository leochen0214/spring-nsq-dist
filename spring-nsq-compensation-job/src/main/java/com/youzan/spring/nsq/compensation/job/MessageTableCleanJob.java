package com.youzan.spring.nsq.compensation.job;

import com.youzan.spring.nsq.transaction.dao.TransactionalMessageDao;

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

  @Resource
  private TransactionalMessageDao transactionalMessageDao;


  @Value("${message.remain.days: 30}")
  private int remainDays;

  @Value("${message.delete.limit.size: 200}")
  private int fetchSize;


  @Override
  public void execute(ShardingContext shardingContext) {
    Date date = toDate(ZonedDateTime.now().minusDays(remainDays));

    int rows = transactionalMessageDao.batchDeleteOfNonSharding(date, fetchSize);

    log.info("delete {} rows messages", rows);
  }


  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }
}
