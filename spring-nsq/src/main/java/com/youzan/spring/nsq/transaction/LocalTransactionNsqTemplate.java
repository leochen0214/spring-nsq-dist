package com.youzan.spring.nsq.transaction;

import com.youzan.spring.nsq.support.EnvironmentComponent;

import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author: clong
 * @date: 2018-09-11
 */
public class LocalTransactionNsqTemplate {

  private final TransactionTemplate transactionTemplate;
  private final EnvironmentComponent environmentComponent;

  public LocalTransactionNsqTemplate(TransactionTemplate transactionTemplate,
                                     EnvironmentComponent environmentComponent) {
    this.transactionTemplate = transactionTemplate;
    this.environmentComponent = environmentComponent;
  }
}
