package com.youzan.spring.nsq.autoconfigure;

import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.LocalTransactionNsqTemplate;
import com.youzan.spring.nsq.transaction.dao.DefaultTransactionalMessageDao;
import com.youzan.spring.nsq.transaction.dao.TransactionalMessageDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author: clong
 * @date: 2018-09-12
 */
@Configuration
@ConditionalOnClass(LocalTransactionNsqTemplate.class)
@AutoConfigureAfter({SpringNsqAutoConfiguration.class,
                     DataSourceTransactionManagerAutoConfiguration.class,
                     JdbcTemplateAutoConfiguration.class})
public class LocalTransactionNsqTemplateAutoConfiguration {


  @Bean
  @ConditionalOnMissingBean
  public CurrentEnvironment environmentComponent() {
    return new CurrentEnvironment();
  }


  @Bean
  @ConditionalOnMissingBean
  public TransactionalMessageDao transactionalMessageDao(@Autowired JdbcTemplate jdbcTemplate,
                                                         @Value("${spring.nsq.transaction.table-name}") String tableName) {
    DefaultTransactionalMessageDao transactionalMessageDao =
        new DefaultTransactionalMessageDao(jdbcTemplate);
    transactionalMessageDao.setTableName(tableName);

    return transactionalMessageDao;
  }

  @Bean
  @ConditionalOnMissingBean
  public LocalTransactionNsqTemplate localTransactionNsqTemplate(
      @Autowired PlatformTransactionManager transactionManager,
      @Autowired CurrentEnvironment currentEnvironment,
      @Autowired TransactionalMessageDao transactionalMessageDao,
      @Autowired NsqTemplate nsqTemplate) {
    return new LocalTransactionNsqTemplate(transactionManager, currentEnvironment,
                                           nsqTemplate, transactionalMessageDao);
  }


}
