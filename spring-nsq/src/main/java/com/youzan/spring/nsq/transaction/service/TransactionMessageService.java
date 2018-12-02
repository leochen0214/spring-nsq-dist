package com.youzan.spring.nsq.transaction.service;

import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import java.util.Date;
import java.util.List;

/**
 * @author: clong
 * @date: 2018-12-01
 */
public interface TransactionMessageService {

  /**
   * @param from       the begin date
   * @param shardingId the sharding id
   * @param size       the limit size
   * @return the messages of publishing failed.
   */
  List<TransactionMessage> queryPublishFailedMessages(Date from, int shardingId, int size);

  /**
   * send publishing failed message again.
   *
   * @param messages publish failed messages
   * @return the messages count of publish success.
   */
  long publishAgain(List<TransactionMessage> messages);

  /**
   * @return the TransactionMessageDao
   */
  TransactionMessageDao getTransactionMessageDao();
}
