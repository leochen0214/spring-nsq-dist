package com.youzan.spring.nsq.compensation.config;

import com.youzan.spring.nsq.compensation.rest.NsqCompensationRestApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: clong
 * @date: 2018-12-02
 */
@Configuration
public class NsqCompensationRestApiConfiguration {


  @Bean
  public NsqCompensationRestApi nsqCompensationRestApi(){
    return new NsqCompensationRestApi();
  }
}
