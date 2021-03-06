package com.youzan.spring.nsq.transaction;

import com.youzan.spring.nsq.transaction.domain.MessageContext;

/**
 * @author: clong
 * @date: 2018-09-12
 * @see com.youzan.spring.nsq.transaction.builder.MessageContextBuilder
 */
public interface TransactionNsqTemplate {

  /**
   * send transactional message, if message send failed, then we can send it later.
   *
   * @param topic   message topic
   * @param context message context, use @{link MessageContextBuilder to build it}
   */
  void send(String topic, MessageContext context);
}
