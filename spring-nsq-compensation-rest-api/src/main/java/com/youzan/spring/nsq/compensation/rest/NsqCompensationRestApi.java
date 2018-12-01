package com.youzan.spring.nsq.compensation.rest;

import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;
import com.youzan.spring.nsq.transaction.service.TransactionMessageService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-12-01
 */
@RestController
@RequestMapping("/nsq/compensation/api")
@Slf4j
public class NsqCompensationRestApi {

  @Resource
  private TransactionMessageService transactionMessageService;

  @GetMapping("/delete")
  public String delete(
      @RequestParam(name = "remainDays", required = false, defaultValue = "30") int remainDays,
      @RequestParam(name = "size", required = false, defaultValue = "200") int size,
      @RequestParam(name = "shardingId", required = false, defaultValue = "0") int shardingId) {

    Date untilTo = toDate(ZonedDateTime.now().minusDays(remainDays));
    TransactionMessageDao dao = transactionMessageService.getTransactionMessageDao();
    int rows = dao.batchDelete(untilTo, size, shardingId);
    log.info("[事务性消息清理任务]清理{}天前数据, 本次清理共删除 {} 条记录", remainDays, rows);
    return "ok";
  }


  @GetMapping("publishAgain")
  public String publishAgain(
      @RequestParam(name = "daysAgo", required = false, defaultValue = "7") int daysAgo,
      @RequestParam(name = "size", required = false, defaultValue = "200") int size,
      @RequestParam(name = "shardingId", required = false, defaultValue = "0") int shardingId) {
    Date from = toDate(ZonedDateTime.now().minusDays(daysAgo));
    TransactionMessageDao dao = transactionMessageService.getTransactionMessageDao();
    List<TransactionMessage> messages = dao.queryPublishFailedMessages(from, size, shardingId);
    if (messages == null || messages.isEmpty()) {
      log.info("[事务性消息补偿任务]此次调度没有查询到{}天前需要重发的消息", daysAgo);
    } else {
      transactionMessageService.publishAgain(messages);
    }
    return "ok";
  }


  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }


}
