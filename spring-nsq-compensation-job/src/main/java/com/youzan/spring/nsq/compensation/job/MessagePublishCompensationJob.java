package com.youzan.spring.nsq.compensation.job;

import com.youzan.spring.nsq.transaction.domain.TransactionMessage;
import com.youzan.spring.nsq.transaction.service.TransactionMessageService;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * search sending failed messages, and send it again.
 *
 * @author: clong
 * @date: 2018-09-12
 */
@Slf4j
public class MessagePublishCompensationJob implements DataflowJob<TransactionMessage> {

  private static final String PREFIX = "[事务性消息补偿任务]";

  @Resource
  private TransactionMessageService transactionMessageService;

  @Value("${spring.nsq.transaction.message.fetch-days: 7}")
  private int daysAgo;

  @Value("${spring.nsq.transaction.message.fetch-size: 200}")
  private int fetchSize;


  @Override
  public List<TransactionMessage> fetchData(ShardingContext ctx) {
    log.debug("{}消息重发补偿job开始工作, ctx={}", PREFIX, ctx);
    Date from = toDate(ZonedDateTime.now().minusDays(daysAgo));

    List<TransactionMessage> messages =
        transactionMessageService.queryPublishFailedMessages(from, 0, fetchSize);
    if (messages.isEmpty()) {
      log.info("{}此次调度没有查询到{}天前需要重发的消息", PREFIX, daysAgo);
    }
    return messages;
  }

  @Override
  public void processData(ShardingContext shardingContext, List<TransactionMessage> messages) {
    transactionMessageService.publishAgain(messages);
  }

  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }

}
