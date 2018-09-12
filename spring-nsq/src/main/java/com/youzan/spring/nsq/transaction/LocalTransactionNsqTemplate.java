package com.youzan.spring.nsq.transaction;

import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.support.converter.JsonMessageConverter;
import com.youzan.spring.nsq.transaction.builder.TransactionalMessageBuilder;
import com.youzan.spring.nsq.transaction.dao.TransactionalMessageDao;
import com.youzan.spring.nsq.transaction.domain.MessageContext;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionalMessage;

import com.alibaba.fastjson.JSON;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-11
 */
@Slf4j
public class LocalTransactionNsqTemplate {

  private final CurrentEnvironment currentEnvironment;
  private final NsqTemplate nsqTemplate;
  private final TransactionalMessageDao transactionalMessageDao;
  private final TransactionTemplate template;

  public LocalTransactionNsqTemplate(PlatformTransactionManager transactionManager,
                                     CurrentEnvironment currentEnvironment,
                                     NsqTemplate nsqTemplate,
                                     TransactionalMessageDao transactionalMessageDao) {
    this.currentEnvironment = currentEnvironment;
    this.nsqTemplate = nsqTemplate;
    this.transactionalMessageDao = transactionalMessageDao;
    this.template = createTransactionTemplate(transactionManager);
  }


  public void send(String topic, MessageContext context) {
    String payload = getPayload(context);
    TransactionalMessage messageDO = TransactionalMessageBuilder.builder()
        .businessKey(context.getBusinessKey())
        .eventType(context.getEventType())
        .shardingId(context.getShardingId())
        .env(currentEnvironment.currentEnv())
        .payload(payload)
        .state(MessageStateEnum.CREATED.getCode())
        .build();

    boolean success = false;
    try {
      success = template.execute(status -> {
        int rows = transactionalMessageDao.insert(messageDO);
        return rows == 1;
      });
    } catch (DuplicateKeyException e) {
      log.warn("重复消息，不入库", e);
    }

    if (success) {
      TransactionSynchronizationManager
          .registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
              doSend(context.getEventType(), topic, payload, messageDO);
            }
          });
    }

  }

  private TransactionTemplate createTransactionTemplate(PlatformTransactionManager txManager) {
    TransactionTemplate template = new TransactionTemplate(txManager);
    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

    return template;
  }

  private void doSend(String eventType, String topic, String msg, TransactionalMessage messageDO) {
    log.info("开始发送{}消息, message={}", eventType, msg);
    try {
      Message message = Message.create(new Topic(topic), msg);

      nsqTemplate.execute(producer -> producer.publish(message));

      template.execute(status -> {
        int rows = transactionalMessageDao.updateState(messageDO.getId(), messageDO.getShardingId(),
                                                       MessageStateEnum.PUBLISHED.getCode());
        return rows == 1;
      });
      log.info("结束发送{}消息, message={}", eventType, msg);
    } catch (NSQException e) {
      log.error("发送{}消息失败, message={}", eventType, msg, e);
    }
  }

  private String getPayload(MessageContext context) {
    Object payload = context.getPayload();

    if (context.isWrapMessage()) {
      JsonMessageConverter.Headers headers = new JsonMessageConverter.Headers();
      headers.setTxId(context.getEventType() + "-" + context.getBusinessKey());

      Map<String, Object> messageContainer = new HashMap<>();
      messageContainer.put("bizBody", context.getPayload());
      messageContainer.put("headers", headers);

      payload = messageContainer;
    }

    return JSON.toJSONString(payload);
  }

}
