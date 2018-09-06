package com.youzan.spring.nsq.autoconfigure;

import com.youzan.spring.nsq.annotation.EnableNsq;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.core.impl.DefaultNsqConsumerFactory;
import com.youzan.spring.nsq.core.impl.DefaultNsqProducerFactory;
import com.youzan.spring.nsq.core.MessageListenerContainerFactory;
import com.youzan.spring.nsq.core.impl.NsqMessageListenerContainerFactory;
import com.youzan.spring.nsq.core.ProducerFactory;
import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.listener.MessageListenerContainer;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;
import com.youzan.spring.nsq.properties.ProducerConfigProperties;
import com.youzan.spring.nsq.support.converter.JsonMessageConverter;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: clong
 * @date: 2018-09-05
 */
@Configuration
@ConditionalOnClass(NsqTemplate.class)
@EnableConfigurationProperties(NsqProperties.class)
@EnableNsq
public class SpringNsqAutoConfiguration {

  private final NsqProperties properties;

  public SpringNsqAutoConfiguration(NsqProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsumerConfigProperties consumerProperties() {
    ConsumerConfigProperties consumerProperties = new ConsumerConfigProperties();
    consumerProperties.setLookupAddress(this.properties.getLookupAddress());
    consumerProperties.setConsumerWorkerPoolSize(this.properties.getConsumerWorkerPoolSize());
    consumerProperties.setMessageInteractiveTimeout(this.properties.getMessageInteractiveTimeout());
    consumerProperties.setConnectTimeout(this.properties.getConnectTimeout());
    consumerProperties.setQueryTimeout(this.properties.getQueryTimeout());
    consumerProperties.setNextConsumingInSecond(this.properties.getNextConsumingInSecond());
    consumerProperties.setAttemptWarningThreshold(this.properties.getAttemptWarningThreshold());
    consumerProperties.setAttemptErrorThreshold(this.properties.getAttemptErrorThreshold());

    return consumerProperties;
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsumerFactory consumerFactory() {
    return new DefaultNsqConsumerFactory(consumerProperties());
  }

  @Bean
  @ConditionalOnMissingBean
  public ProducerConfigProperties producerProperties() {
    ProducerConfigProperties producerProperties = new ProducerConfigProperties();
    producerProperties.setLookupAddress(this.properties.getLookupAddress());
    producerProperties.setConnectionPoolSize(this.properties.getConnectionPoolSize());
    producerProperties.setMaxWaitTimeout(this.properties.getMaxWaitTimeout());
    producerProperties.setMinIdle(this.properties.getMinIdle());
    producerProperties.setPublishRetryTimes(this.properties.getPublishRetryTimes());
    producerProperties.setHeartbeatInterval(this.properties.getHeartbeatInterval());
    producerProperties.setConnectionEvictInterval(this.properties.getConnectionEvictInterval());

    return producerProperties;
  }

  @Bean
  @ConditionalOnMissingBean
  public ProducerFactory producerFactory() {
    return new DefaultNsqProducerFactory(producerProperties());
  }

  @Bean
  @ConditionalOnMissingBean
  public MessageListenerContainerFactory<? extends MessageListenerContainer> nsqMessageListenerContainerFactory() {
    NsqMessageListenerContainerFactory factory = new NsqMessageListenerContainerFactory();
    factory.setConsumerFactory(consumerFactory());
    factory.setAutoStartup(true);
//    factory.setErrorHandler(
//        (e, data) -> System.out.println("occur exception, data content="+ data.getReadableContent() + e));

    factory.setMessageConverter(messageConverter());

    return factory;
  }

  @Bean
  @ConditionalOnMissingBean
  public NSQMessageConverter messageConverter() {
    return new JsonMessageConverter();
  }


  @Bean
  @ConditionalOnMissingBean
  public NsqTemplate nsqTemplate(){
    return new NsqTemplate(producerFactory(), messageConverter());
  }


}
