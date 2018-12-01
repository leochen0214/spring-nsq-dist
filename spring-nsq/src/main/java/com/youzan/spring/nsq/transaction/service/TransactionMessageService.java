package com.youzan.spring.nsq.transaction.service;

import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import java.util.List;

/**
 * @author: clong
 * @date: 2018-12-01
 */
public interface TransactionMessageService {

  /**
   * send publishing failed message again.
   *
   * @param messages publish failed messages
   */
  void publishAgain(List<TransactionMessage> messages);

  /**
   * @return the TransactionMessageDao
   */
  TransactionMessageDao getTransactionMessageDao();
}
