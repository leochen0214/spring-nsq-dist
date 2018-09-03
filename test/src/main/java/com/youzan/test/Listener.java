package com.youzan.test;

import com.youzan.spring.nsq.annotation.NsqListener;

import com.alibaba.fastjson.JSON;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Component
public class Listener {

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener1(String person) {
    System.out.println("listener1 personStr=" + person);
  }


  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener2(Person person) {
    System.out.println("listener2 person=" + person);
  }


  @NsqListener(topics = "JavaTesting-Finish", channel = "default")
  public void listener3(List<Person> person) {
    System.out.println("listener3 persons=" + person);
  }


}
