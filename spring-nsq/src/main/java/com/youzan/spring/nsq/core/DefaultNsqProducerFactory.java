package com.youzan.spring.nsq.core;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.properties.ProducerConfigProperties;

import org.springframework.beans.factory.DisposableBean;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-08-29
 */
@Slf4j
public class DefaultNsqProducerFactory implements ProducerFactory, DisposableBean {

  private final ProducerConfigProperties properties;
  private final NSQConfig nsqConfig;
  private volatile boolean running;
  private volatile Producer producer;


  public DefaultNsqProducerFactory(ProducerConfigProperties properties) {
    this.properties = properties;
    this.nsqConfig = buildProducerConfig(properties);
  }

  @Override
  public Producer createProducer() {
    if (this.producer == null) {
      synchronized (this) {
        if (this.producer == null) {
          Producer producer = createNsqProducer(nsqConfig);
          doStart(producer);
          this.producer = producer;
          running = true;
        }
      }
    }
    return this.producer;
  }

  @Override
  public void destroy() {
    // because ProducerImplV2.close() is thread safe, so we just invoke it
    producer.close();
    running = false;
  }

  @Override
  public ProducerConfigProperties getConfigurationProperties() {
    return properties;
  }

  protected Producer createNsqProducer(NSQConfig nsqConfig) {
    return new ProducerImplV2(nsqConfig);
  }

  public boolean isRunning() {
    return running;
  }


  private void doStart(Producer producer) {
    try {
      producer.start();
    } catch (NSQException e) {
      log.error("start nsq producer failed, nsqConfig={}", nsqConfig, e);
      throw new RuntimeException("start nsq producer failed", e);
    }
  }

  protected NSQConfig buildProducerConfig(ProducerConfigProperties properties) {
    NSQConfig config = new NSQConfig();

    if (properties.getLookupAddress() != null) {
      config.setLookupAddresses(properties.getLookupAddress());
    }

    if (properties.getConnectionPoolSize() != null){
      config.setConnectionPoolSize(properties.getConnectionPoolSize());
    }

    if (properties.getMaxWaitTimeout() != null) {
      config.setConnWaitTimeoutForProducerInMilliSec(properties.getMaxWaitTimeout());
    }

    if (properties.getMinIdle() != null) {
      config.setMinIdleConnectionForProducer(properties.getMinIdle());
    }

    if (properties.getPublishRetryTimes() != null) {
      config.setPublishRetry(properties.getPublishRetryTimes());
    }

    if (properties.getHeartbeatInterval() != null) {
      config.setHeartbeatIntervalInMillisecond(properties.getHeartbeatInterval());
    }

    if (properties.getConnectionEvictInterval() != null) {
      config.setProducerConnectionEvictIntervalInMillSec(properties.getConnectionEvictInterval());
    }

    return config;
  }




}
