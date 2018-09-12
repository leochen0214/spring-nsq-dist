package com.youzan.spring.nsq.compensation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author: clong
 * @date: 2018-09-12
 */
@Configuration
@ImportResource("classpath*:META-INF/nsq-transaction-message-compensation-jobs.xml")
public class NsqCompensationJobConfiguration {

}
