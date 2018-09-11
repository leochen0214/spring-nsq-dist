package com.youzan.spring.nsq.transaction.dao;

import com.youzan.spring.nsq.transaction.TransactionalMessage;


/**
 * @author: clong
 * @date: 2018-09-11
 */
public interface TransactionalMessageDao {

  /**
   * insert into message table
   *
   * @param message message entity
   * @return weather insert success
   */
  int insert(TransactionalMessage message);

  /**
   * update message state
   *
   * @param id         message id
   * @param shardingId sharding id
   * @param toState    to state
   * @return weather update success
   */
  int updateState(Long id, int shardingId, int toState);


}
