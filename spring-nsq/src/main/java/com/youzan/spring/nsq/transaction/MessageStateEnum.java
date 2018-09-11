package com.youzan.spring.nsq.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: clong
 * @date: 2018-09-11
 */
@Getter
@AllArgsConstructor
public enum MessageStateEnum {


  CREATED(0),

  PUBLISHED(1);


  private final int code;


}
