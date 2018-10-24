package com.youzan.spring.nsq.autoconfigure;

import com.youzan.spring.nsq.transaction.CurrentEnvironment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: clong
 * @date: 2018-10-24
 */
@Configuration
@ConditionalOnClass(CurrentEnvironment.class)
public class CurrentEnvironmentAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CurrentEnvironment currentEnvironment() {
    return new CurrentEnvironment();
  }


}
