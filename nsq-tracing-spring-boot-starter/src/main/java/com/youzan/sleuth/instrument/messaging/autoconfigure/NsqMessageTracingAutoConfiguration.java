package com.youzan.sleuth.instrument.messaging.autoconfigure;

import com.youzan.brave.nsq.client.NsqTracing;
import com.youzan.sleuth.instrument.messaging.SleuthSpringNsqAspect;
import com.youzan.spring.nsq.core.ProducerFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracer;
import brave.Tracing;

/**
 * @author: clong
 * @date: 2018-09-03
 */
@Configuration
@ConditionalOnBean(Tracing.class)
@AutoConfigureAfter({TraceAutoConfiguration.class})
@ConditionalOnProperty(value = "spring.sleuth.messaging.enabled", matchIfMissing = true)
public class NsqMessageTracingAutoConfiguration {


  @Configuration
  @ConditionalOnProperty(value = "spring.sleuth.messaging.nsq.enabled", matchIfMissing = true)
  @ConditionalOnClass(ProducerFactory.class)
  protected static class SleuthSpringNsqConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NsqTracing nsqTracing(Tracing tracing) {
      return NsqTracing.newBuilder(tracing).build();
    }

    @Bean
    // for tests
    @ConditionalOnMissingBean
    public SleuthSpringNsqAspect sleuthSpringNsqAspect(NsqTracing nsqTracing, Tracer tracer) {
      return new SleuthSpringNsqAspect(nsqTracing, tracer);
    }
  }


}
