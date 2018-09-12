package com.youzan.spring.nsq.compensation.job;

import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.dao.TransactionalMessageDao;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import java.util.List;

import javax.annotation.Resource;

/**
 * search publish failed messages, and publish again
 *
 * @author: clong
 * @date: 2018-09-12
 */
public class MessagePublishCompensationJob implements DataflowJob<TransactionMessage> {

  @Resource
  private CurrentEnvironment currentEnvironment;

  @Resource
  private TransactionalMessageDao transactionalMessageDao;


  @Override
  public List<TransactionMessage> fetchData(ShardingContext shardingContext) {


    return null;
  }

  @Override
  public void processData(ShardingContext shardingContext, List<TransactionMessage> list) {

  }
}
