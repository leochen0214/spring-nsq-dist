package com.youzan.spring.nsq.annotation;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.bootstrap.NsqBootstrapConfiguration;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.core.ProducerFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class EnableNsqIntegrationTest {

  private String lookupAddress = "sqs-qa.s.qima-inc.com:4161";
  private String topic = "JavaTesting-Ext";
  private String channel = "default";


  private ConfigurableApplicationContext ctx;

  @BeforeEach
  public void setup() {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(NsqBootstrapConfiguration.class);
    ctx.register(AppConfig.class);
    ctx.refresh();

    this.ctx = ctx;
  }

  @AfterEach
  public void teardown() {
    if (ctx != null) {
      ctx.close();
    }
  }


}
