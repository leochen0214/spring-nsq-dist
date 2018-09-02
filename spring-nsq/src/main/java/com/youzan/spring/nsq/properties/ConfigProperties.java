package com.youzan.spring.nsq.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: clong
 * @date: 2018-09-01
 */
@Getter
@Setter
public abstract class ConfigProperties {

  protected String lookupAddress;

}
