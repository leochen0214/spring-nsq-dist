package com.youzan.spring.nsq.transaction;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author: clong
 * @date: 2018-09-11
 */
public class CurrentEnvironment implements EnvironmentAware {

  /**
   * sc环境的变量名称
   */
  private static final String SERVICE_CHAIN_PROPERTY = "APPLICATION_SERVICE_CHAIN";


  private Environment environment;

  private String serviceChainName = SERVICE_CHAIN_PROPERTY;


  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }


  public void setServiceChainName(String serviceChainName) {
    this.serviceChainName = serviceChainName;
  }


  /**
   * 获取当前环境变量
   */
  public String currentEnv() {
    String[] activeProfiles = environment.getActiveProfiles();

    if (activeProfiles == null || activeProfiles.length == 0) {
      return "";
    }

    // 如果是sc环境优先选择sc的标记
    String scEnv = System.getenv(serviceChainName);
    String activeProfile = activeProfiles[0];

    if (StringUtils.hasText(scEnv)) {
      return activeProfile + "-" + scEnv;
    }

    return activeProfile;
  }


}
