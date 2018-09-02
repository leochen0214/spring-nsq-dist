package com.youzan.test;

import com.youzan.spring.nsq.annotation.NsqListener;

import com.alibaba.fastjson.JSON;

import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Component
public class Listener {

  @NsqListener(topics = "JavaTesting-Ext", channel = "default")
  public void listener1(Person person) {
    System.out.println("person=" + person);
  }


  public static void main(String[] args) {
    Person person = new Person();
    person.setName("jack");
    person.setAge(18);
    person.setSex("male");

    String s = JSON.toJSONString(person);
    System.out.println(s);

    Type javaType = Person.class;
    Object o = JSON.parseObject(s, javaType);

    System.out.println(o);

  }
}
