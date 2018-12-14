package com.youzan.spring.nsq.transaction.service;

import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.spring.nsq.core.NsqOperations;
import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-12-01
 */
@Slf4j
public class DefaultTransactionMessageService implements TransactionMessageService {

  private static final String PREFIX = "[事务性消息补偿任务]";

  private static final int DEFAULT_SECONDS = 60;

  private final TransactionMessageDao transactionMessageDao;
  private final NsqOperations nsqTemplate;
  private final CurrentEnvironment currentEnvironment;
  private int seconds = DEFAULT_SECONDS;


  public DefaultTransactionMessageService(TransactionMessageDao transactionMessageDao,
                                          NsqOperations nsqTemplate,
                                          CurrentEnvironment currentEnvironment) {
    this.transactionMessageDao = transactionMessageDao;
    this.nsqTemplate = nsqTemplate;
    this.currentEnvironment = currentEnvironment;
  }

  public void setSeconds(int seconds) {
    this.seconds = seconds;
  }

  public int getSeconds() {
    return seconds;
  }

  @Override
  public List<TransactionMessage> queryPublishFailedMessages(Date from, int shardingId, int size) {
    Date to = toDate(ZonedDateTime.now().minusSeconds(seconds));
    String env = currentEnvironment.currentEnv();
    return transactionMessageDao.queryPublishFailedMessages(from, to, size, env, shardingId);
  }

  @Override
  public long publishAgain(List<TransactionMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return 0;
    }

    log.info("{}此次需要重发的消息共有{}条", PREFIX, messages.size());

    long successCount = messages.stream().filter(this::isNotEmptyTopic)
        .map(this::doSend).filter(Boolean::booleanValue).count();

    log.info("{}此次需要重发的{}条消息处理完毕, 消息重发成功并且更新消息状态成功的共{}条",
             PREFIX, messages.size(), successCount);

    return successCount;
  }

  @Override
  public TransactionMessageDao getTransactionMessageDao() {
    return transactionMessageDao;
  }

  private boolean isNotEmptyTopic(TransactionMessage message) {
    return StringUtils.hasText(message.getTopic());
  }

  private boolean doSend(TransactionMessage message) {
    if (sendNsqMessageSuccess(message)) {
      log.info("{}补偿重发消息成功, message={}", PREFIX, message);
      if (updateMessageStateSuccess(message)) {
        log.info("{}此次更新消息表状态为已发送成功, message={}", PREFIX, message);
        return true;
      }
    }
    return false;
  }

  private boolean sendNsqMessageSuccess(TransactionMessage message) {
    try {
      Message nsqMessage = Message.create(new Topic(message.getTopic()), message.getPayload());
      nsqTemplate.send(nsqMessage);
      return true;
    } catch (Exception e) {
      log.warn("{}此次补偿消息重发失败, message={}", PREFIX, message, e);
    }
    return false;
  }

  private boolean updateMessageStateSuccess(TransactionMessage message) {
    try {
      transactionMessageDao.updateState(message.getId(), message.getShardingId(),
                                        MessageStateEnum.PUBLISHED.getCode());
      return true;
    } catch (Exception e) {
      log.warn("{}此次更新消息表状态为已发送失败, message={}", PREFIX, message, e);
    }
    return false;
  }

  private static Date toDate(ZonedDateTime zdt) {
    return Date.from(zdt.toInstant());
  }

}
