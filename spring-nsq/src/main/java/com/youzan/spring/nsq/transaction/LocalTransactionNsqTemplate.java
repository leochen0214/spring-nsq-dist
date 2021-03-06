package com.youzan.spring.nsq.transaction;

import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.support.converter.JsonMessageConverter;
import com.youzan.spring.nsq.transaction.builder.TransactionMessageBuilder;
import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.MessageContext;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

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
public class LocalTransactionNsqTemplate implements TransactionNsqTemplate {

  private final CurrentEnvironment currentEnvironment;
  private final NsqTemplate nsqTemplate;
  private final TransactionMessageDao transactionMessageDao;
  private TransactionTemplate template;

  public LocalTransactionNsqTemplate(PlatformTransactionManager transactionManager,
                                     CurrentEnvironment currentEnvironment,
                                     NsqTemplate nsqTemplate,
                                     TransactionMessageDao transactionMessageDao) {
    this.currentEnvironment = currentEnvironment;
    this.nsqTemplate = nsqTemplate;
    this.transactionMessageDao = transactionMessageDao;
    this.template = createTransactionTemplate(transactionManager);
  }


  @Override
  public void send(String topic, MessageContext context) {
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      throw new IllegalStateException("当前线程上无绑定事务，请确保在开启了事务中的方法中调用此方法");
    }

    String payload = getPayload(context);
    TransactionMessage message = TransactionMessageBuilder.builder()
        .businessKey(context.getBusinessKey())
        .eventType(context.getEventType())
        .shardingId(context.getShardingId())
        .env(currentEnvironment.currentEnv())
        .payload(payload)
        .topic(topic)
        .state(MessageStateEnum.CREATED.getCode())
        .build();
    boolean success = insert(message);
    if (success) {
      TransactionSynchronizationManager
          .registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
              doSend(context.getEventType(), topic, payload, message);
            }
          });
    }

  }

  private Boolean insert(TransactionMessage message) {
    return template.execute(status -> {
      int rows = 0;
      try {
        rows = transactionMessageDao.insert(message);
      } catch (DuplicateKeyException e) {
        log.warn("重复消息，不入库", e);
      }
      return rows == 1;
    });
  }

  private TransactionTemplate createTransactionTemplate(PlatformTransactionManager txManager) {
    TransactionTemplate template = new TransactionTemplate(txManager);
    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

    return template;
  }

  private void doSend(String eventType, String topic, String payload, TransactionMessage message) {
    log.info("开始发送{}消息, message={}", eventType, payload);
    try {
      Message nsqMessage = Message.create(new Topic(topic), payload);
      nsqTemplate.execute(producer -> producer.publish(nsqMessage));

      template.execute(status -> {
        int rows = transactionMessageDao.updateState(message.getId(), message.getShardingId(),
                                                     MessageStateEnum.PUBLISHED.getCode());
        return rows == 1;
      });
      log.info("结束发送{}消息, message={}", eventType, payload);
    } catch (NSQException e) {
      log.error("发送{}消息失败, message={}", eventType, payload, e);
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
