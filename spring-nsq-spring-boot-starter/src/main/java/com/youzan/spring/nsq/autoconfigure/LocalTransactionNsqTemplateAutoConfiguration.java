package com.youzan.spring.nsq.autoconfigure;

import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.LocalTransactionNsqTemplate;
import com.youzan.spring.nsq.transaction.TransactionNsqTemplate;
import com.youzan.spring.nsq.transaction.dao.DefaultTransactionMessageDao;
import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.service.DefaultTransactionMessageService;
import com.youzan.spring.nsq.transaction.service.TransactionMessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author: clong
 * @date: 2018-09-12
 */
@Configuration
@ConditionalOnClass(LocalTransactionNsqTemplate.class)
@AutoConfigureAfter({SpringNsqAutoConfiguration.class,
                     CurrentEnvironmentAutoConfiguration.class,
                     DataSourceAutoConfiguration.class,
                     DataSourceTransactionManagerAutoConfiguration.class})
@EnableConfigurationProperties(TransactionNsqProperties.class)
@ConditionalOnProperty(value = "spring.nsq.transaction-message.enabled", matchIfMissing = true)
public class LocalTransactionNsqTemplateAutoConfiguration {

  private final TransactionNsqProperties properties;

  public LocalTransactionNsqTemplateAutoConfiguration(TransactionNsqProperties properties) {
    this.properties = properties;
  }


  @Bean
  @ConditionalOnMissingBean
  public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @ConditionalOnMissingBean
  public TransactionMessageDao transactionMessageDao(@Autowired JdbcTemplate jdbcTemplate) {
    DefaultTransactionMessageDao transactionalMessageDao =
        new DefaultTransactionMessageDao(jdbcTemplate);
    transactionalMessageDao.setTableName(properties.getTableName());

    return transactionalMessageDao;
  }

  @Bean
  @ConditionalOnMissingBean
  public TransactionMessageService transactionMessageService(
      @Autowired TransactionMessageDao transactionMessageDao,
      @Autowired NsqTemplate nsqTemplate) {
    return new DefaultTransactionMessageService(transactionMessageDao, nsqTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public TransactionNsqTemplate transactionNsqTemplate(
      @Autowired PlatformTransactionManager transactionManager,
      @Autowired CurrentEnvironment currentEnvironment,
      @Autowired TransactionMessageDao transactionMessageDao,
      @Autowired NsqTemplate nsqTemplate) {
    return new LocalTransactionNsqTemplate(transactionManager, currentEnvironment,
                                           nsqTemplate, transactionMessageDao);
  }


}
