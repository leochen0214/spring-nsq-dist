package com.youzan.example;

// tag::configuration[]
import com.youzan.spring.nsq.annotation.EnableNsq;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.core.impl.DefaultNsqConsumerFactory;
import com.youzan.spring.nsq.core.impl.DefaultNsqProducerFactory;
import com.youzan.spring.nsq.core.MessageListenerContainerFactory;
import com.youzan.spring.nsq.core.impl.NsqMessageListenerContainerFactory;
import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.core.ProducerFactory;
import com.youzan.spring.nsq.listener.MessageListenerContainer;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;
import com.youzan.spring.nsq.properties.ProducerConfigProperties;
import com.youzan.spring.nsq.support.converter.JsonMessageConverter;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author: clong
 * @date: 2018-09-02
 */
@Configuration
@EnableNsq
public class AppConfig {

  private String lookupAddress = "sqs-qa.s.qima-inc.com:4161";
  private String topic = "JavaTesting-Ext";
  private String channel = "default";


  @Bean
  public ConsumerFactory consumerFactory() {
    return new DefaultNsqConsumerFactory(consumerProperties());
  }

  @Bean
  public ConsumerConfigProperties consumerProperties() {
    ConsumerConfigProperties properties = new ConsumerConfigProperties();
    properties.setLookupAddress(lookupAddress);

    return properties;
  }

  @Bean
  public ProducerFactory producerFactory() {
    return new DefaultNsqProducerFactory(producerProperties());
  }

  @Bean
  public ProducerConfigProperties producerProperties() {
    ProducerConfigProperties properties = new ProducerConfigProperties();
    properties.setLookupAddress(lookupAddress);
    properties.setConnectionPoolSize(5);

    return properties;
  }

  @Bean
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
  public NSQMessageConverter messageConverter() {
    return new JsonMessageConverter();
  }

  @Bean
  public NsqTemplate nsqTemplate(){
    return new NsqTemplate(producerFactory(), messageConverter());
  }

}
// end::configuration[]