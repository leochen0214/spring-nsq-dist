package com.youzan.spring.nsq.transaction.dao;

import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import java.util.Date;
import java.util.List;


/**
 * @author: clong
 * @date: 2018-09-11
 */
public interface TransactionMessageDao {

  /**
   * insert into message table
   *
   * @param message message entity
   * @return weather insert success
   */
  int insert(TransactionMessage message);

  /**
   * update message state
   *
   * @param id         message id
   * @param shardingId sharding id
   * @param toState    to state
   * @return weather update success
   */
  int updateState(Long id, int shardingId, int toState);


  /**
   * query single message from non-sharding database
   *
   * @param businessKey the business key
   * @param eventType   the event type
   * @return the single message record
   */
  TransactionMessage querySingleMessageOfNonSharding(String businessKey, String eventType);

  /**
   * query single message record
   *
   * @param businessKey the business key
   * @param eventType   the event type
   * @param shardingId  the sharding id
   * @return the single message record
   */
  TransactionMessage querySingleMessage(String businessKey, String eventType, int shardingId);

  /**
   * query send failed messages from non-sharding database
   *
   * @param from      the create time which is great or equal than (>=) from
   * @param fetchSize the limit size
   * @param env       the current environment
   * @return send failed messages.
   */
  List<TransactionMessage> queryPublishFailedMessagesOfNonSharding(Date from, int fetchSize,
                                                                   String env);

  /**
   * query the messages which can be deleted safely from non-sharding database
   *
   * @param untilTo   the create time which is less or equal than (<=) untilTo
   * @param fetchSize the limit size
   * @param env       the current environment
   * @return send success messages.
   */
  List<TransactionMessage> queryCanDeleteMessagesOfNonSharding(Date untilTo, int fetchSize,
                                                               String env);


  /**
   * batch delete messages (which are publishing success)
   *
   * @param untilTo   the create time which is less or equal than (<=) untilTo
   * @param fetchSize the limit size
   * @return the deleted rows count
   */
  int batchDeleteOfNonSharding(Date untilTo, int fetchSize);
}
