package com.youzan.test;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.spring.nsq.core.ProducerFactory;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

@SpringBootApplication
public class TestNsq2Application {
  private static final Logger logger = LoggerFactory.getLogger(TestNsq2Application.class);

  public static void main(String[] args) {
    SpringApplication.run(TestNsq2Application.class, args);
  }


  @Resource
  ProducerFactory producerFactory;

  @Bean
  public CommandLineRunner runner(){
    return args -> {

      String topic1 = "JavaTesting-Ext";
      String topic2 = "JavaTesting-Finish";

      Person p1 = getPerson("jack", 18);
      Person p2 = getPerson("大王", 20);

      List<Person> list = new ArrayList<>();
      list.add(p1);
      list.add(p2);


      Producer producer = producerFactory.createProducer();

      Topic t1 = new Topic(topic1);
      Topic t2 = new Topic(topic2);

      producer.publish(Message.create(t1, JSON.toJSONString(p1)));
      producer.publish(Message.create(t2, JSON.toJSONString(list)));

      logger.info("publish finish");
      System.out.println("publish finish");

    };
  }

  private Person getPerson(String name, int age) {
    Person p = new Person();
    p.setName(name);
    p.setAge(age);
    p.setSex("male");
    return p;
  }
}
