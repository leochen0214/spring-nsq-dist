package com.youzan.example;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.spring.nsq.core.impl.NsqTemplate;
import com.youzan.spring.nsq.core.ProducerFactory;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import brave.Tracing;

@SpringBootApplication
public class SpringNsqExampleApplication {

  private static final Logger logger = LoggerFactory.getLogger(SpringNsqExampleApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(SpringNsqExampleApplication.class, args);
  }


  @Resource
  ProducerFactory producerFactory;

  @Resource
  NsqTemplate nsqTemplate;

  @Resource
  Tracing tracing;

  @Value("${spring.application.name}")
  String applicationName;

  @Bean
  public CommandLineRunner runner() {
    return args -> {

      String topic1 = "JavaTesting-Ext";
      String topic2 = "JavaTesting-Finish";

      Person p1 = getPerson("jack", 18);
      Person p2 = getPerson("大王", 20);

//      nsqTemplate.send("JavaTesting-Ext", p1);

      List<Person> list = new ArrayList<>();
      list.add(p1);
      list.add(p2);

      Producer producer = producerFactory.createProducer();

      Topic t1 = new Topic(topic1);
      Topic t2 = new Topic(topic2);

      logger.info("publish start");

//      for (int i = 0; i < 2; i++) {
//        Person p = getPerson("jack" + i, 18);
//        logger.info("will publish Person p={}", p);
//        producer.publish(Message.create(t1, JSON.toJSONString(p)));
//      }

      String json = "{\n"
                    + "  \"headers\": {\n"
                    + "    \"actionId\": \"af50c5d58cf94b999d8ea13613d8d65a\",\n"
                    + "    \"txId\": \"assetcenter-180530113558000001-SUCCESS\"\n"
                    + "  },\n"
                    + "  \"bizBody\": {\n"
                    + "    \"payerId\": \"5182812\",\n"
                    + "    \"assetDetailNo\": \"180530113558000001\",\n"
                    + "    \"remark\": \"定金预售测试\",\n"
                    + "    \"tradeDesc\": \"定金预售测试\",\n"
                    + "    \"payFinishedTime\": 1527651358253,\n"
                    + "    \"realAmount\": {\n"
                    + "      \"cent\": 100,\n"
                    + "      \"currency\": \"CNY\",\n"
                    + "      \"currencyNum\": 156,\n"
                    + "      \"currentCode\": \"CNY\",\n"
                    + "      \"yuan\": 1\n"
                    + "    },\n"
                    + "    \"bizSubType\": \"ECARD\",\n"
                    + "    \"payAmount\": {\n"
                    + "      \"cent\": 100,\n"
                    + "      \"currency\": \"CNY\",\n"
                    + "      \"currencyNum\": 156,\n"
                    + "      \"currentCode\": \"CNY\",\n"
                    + "      \"yuan\": 1\n"
                    + "    },\n"
                    + "    \"extra\": {\n"
                    + "      \"ignoreMessage\": true,\n"
                    + "      \"tradeNo\": \"1805301135480000010371\",\n"
                    + "      \"srcEnv\": \"qa\",\n"
                    + "      \"market_channel\": \"1\"\n"
                    + "    },\n"
                    + "    \"yzGurantee\": false,\n"
                    + "    \"payCreateTime\": 1527651358000,\n"
                    + "    \"orderNo\": \"E20180530113548034800001\",\n"
                    + "    \"mchId\": 160922194321381340,\n"
                    + "    \"channelSettleNo\": \"180530113558000000\",\n"
                    + "    \"bizProd\": 1,\n"
                    + "    \"outBizNo\": \"1805301135480000010371\",\n"
                    + "    \"payTool\": \"ECARD\",\n"
                    + "    \"acquireNo\": \"180530113548000000\",\n"
                    + "    \"payerNickName\": \"\",\n"
                    + "    \"bizAction\": \"PAY\",\n"
                    + "    \"bizExt\": {\n"
                    + "      \"forceConsignmentMode\": \"1\",\n"
                    + "      \"acquireOrder\": \"C1805301135480000000371\",\n"
                    + "      \"PAY_TYPE\": \"PHASE_PAY\",\n"
                    + "      \"subAcquireOrder\": \"1805301135480000010371\",\n"
                    + "      \"MERCHANDISE_CODE\": \"0\",\n"
                    + "      \"market_channel\": \"1\",\n"
                    + "      \"payTools\": \"NOW_PAY\",\n"
                    + "      \"YZ_GUARANTEE\": \"false\"\n"
                    + "    },\n"
                    + "    \"payChannel\": \"65\",\n"
                    + "    \"partnerId\": \"820000000003\",\n"
                    + "    \"bizMode\": 1,\n"
                    + "    \"status\": \"BUYER_PAIED\"\n"
                    + "  }\n"
                    + "}";


        producer.publish(Message.create(t1, json));
//      producer.publish(Message.create(t2, JSON.toJSONString(list)));
      logger.info("publish finish");

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
