package com.youzan.spring.nsq.compensation.job;

import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.spring.nsq.core.NsqOperations;
import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

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

  @Resource
  private CurrentEnvironment currentEnvironment;

  @Resource
  private TransactionMessageDao transactionMessageDao;

  @Resource
  private NsqOperations nsqTemplate;

  @Value("${message.fetch.days: 7}")
  private int agoDays;

  @Value("${message.fetch.size: 200}")
  private int fetchSize;


  @Override
  public List<TransactionMessage> fetchData(ShardingContext ctx) {
    String env = currentEnvironment.currentEnv();
    Date from = toDate(ZonedDateTime.now().minusDays(agoDays));

    return transactionMessageDao.queryPublishFailedMessagesOfNonSharding(from, fetchSize, env);
  }

  @Override
  public void processData(ShardingContext shardingContext, List<TransactionMessage> messages) {
    if (messages.isEmpty()) {
      log.info("此次调度没有查询到需要重发的消息, agoDays={}", agoDays);
      return;
    }

    log.info("此次调度查询到需要重发的消息共有{}条", messages.size());

    int successCount = 0;
    for (TransactionMessage message : messages) {
      if (isEmptyTopic(message)) {
        log.error("message topic is empty, message={}", message);
      } else {
        boolean success = doSend(message);
        if (success) {
          successCount++;
        }
      }
    }
    log.info("此次调度查询到需要重发的{}条消息处理完毕, 消息重发成功并且更新消息状态成功的共{}条",
             messages.size(), successCount);
  }

  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }

  private boolean isEmptyTopic(TransactionMessage message) {
    return !StringUtils.hasText(message.getTopic());
  }

  private boolean doSend(TransactionMessage message) {
    boolean sendSuccess = doSendNsqMessage(message);
    boolean updateSuccess = false;
    if (sendSuccess) {
      log.info("补偿重发消息成功, message={}", message);
      updateSuccess = doUpdateMessageState(message);
    }

    if (updateSuccess) {
      log.info("此次更新消息表状态为已发送成功, message={}", message);
    }

    return sendSuccess && updateSuccess;
  }


  private boolean doSendNsqMessage(TransactionMessage message) {
    try {
      Message nsqMessage = Message.create(new Topic(message.getTopic()), message.getPayload());
      nsqTemplate.send(nsqMessage);
      return true;
    } catch (Exception e) {
      log.warn("此次补偿消息重发失败, message={}", message, e);
    }
    return false;
  }


  private boolean doUpdateMessageState(TransactionMessage message) {
    try {
      transactionMessageDao.updateState(message.getId(), message.getShardingId(),
                                        MessageStateEnum.PUBLISHED.getCode());
      return true;
    } catch (Exception e) {
      log.warn("此次更新消息表状态为已发送失败, message={}", message, e);
    }
    return false;
  }


}
