package com.youzan.spring.nsq.core.impl;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * The {@link ConsumerFactory} implementation to produce a new {@link Consumer} instance for
 * provided {@link NSQConfig} {@code nsqConfig} and {@link MessageHandler} {@code messageHandler}
 *
 * @author: clong
 * @date: 2018-08-29
 */
@Slf4j
public class DefaultNsqConsumerFactory implements ConsumerFactory {

  private final ConsumerConfigProperties properties;

  public DefaultNsqConsumerFactory(ConsumerConfigProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isAutoCommit() {
    return properties.isAutoFinish();
  }



  @Override
  public Consumer createConsumer(ConsumerConfigProperties properties2) {
    properties.setTopics(properties2.getTopics());
    properties.setChannel(properties2.getChannel());
    properties.setOrdered(properties2.getOrdered());
    properties.setAutoFinish(properties2.isAutoFinish());

    NSQConfig nsqConfig = buildConsumerConfig(properties);

    Consumer consumer = createNsqConsumer(nsqConfig);
    if (log.isDebugEnabled()) {
      log.debug("create a new Consumer instance, nsqConfig={}", nsqConfig);
    }

    return consumer;
  }

  protected Consumer createNsqConsumer(NSQConfig nsqConfig) {
    return new ConsumerImplV2(nsqConfig);
  }

  @Override
  public ConsumerConfigProperties getConfigurationProperties() {
    return properties;
  }

  private NSQConfig buildConsumerConfig(ConsumerConfigProperties properties) {
    NSQConfig config = new NSQConfig();

    if (properties.getLookupAddress() != null) {
      config.setLookupAddresses(properties.getLookupAddress());
    }

    if (properties.getChannel() != null) {
      config.setConsumerName(properties.getChannel());
    }

    if (properties.getOrdered() != null) {
      config.setOrdered(properties.getOrdered());
    }

    if (properties.getConsumerWorkerPoolSize() != null) {
      config.setConsumerWorkerPoolSize(properties.getConsumerWorkerPoolSize());
    }

    if (properties.getMessageInteractiveTimeout() != null) {
      config.setMsgTimeoutInMillisecond(properties.getMessageInteractiveTimeout());
    }

    if (properties.getConnectTimeout() != null) {
      config.setConnectTimeoutInMillisecond(properties.getConnectTimeout());
    }

    if (properties.getQueryTimeout() != null) {
      config.setQueryTimeoutInMillisecond(properties.getQueryTimeout());
    }

    if (properties.getNextConsumingInSecond() != null) {
      config.setNextConsumingInSeconds(properties.getNextConsumingInSecond());
    }

    if (properties.getAttemptWarningThreshold() != null) {
      config.setAttemptWarningThresdhold(properties.getAttemptWarningThreshold());
    }

    if (properties.getAttemptErrorThreshold() != null) {
      config.setAttemptErrorThresdhold(properties.getAttemptErrorThreshold());
    }

    return config;
  }
}
