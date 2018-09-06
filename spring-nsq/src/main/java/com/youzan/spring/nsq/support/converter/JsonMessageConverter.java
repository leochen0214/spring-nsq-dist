package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.entity.NSQMessage;

import com.alibaba.fastjson.JSON;

import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-03
 */
@Slf4j
public class JsonMessageConverter extends MessagingMessageConverter {


  @Override
  public Object extractAndConvertValue(NSQMessage message, Type type, boolean unpackMessage) {
    String content = message.getReadableContent();
    if (!StringUtils.hasText(content)) {
      return null;
    }

    String json = content;
    if (unpackMessage) {
      TransactionalMessage data = JSON.parseObject(content, TransactionalMessage.class);
      json = data.getBizBody();
    }

    try {
      Object result = JSON.parseObject(json, type);
      if (log.isDebugEnabled()) {
        log.debug("extractAndConvertValue return value={}, type class={}",
                  result, result.getClass().getName());
      }
      return result;
    } catch (Exception e) {
      log.warn("parse json occur exception, content={}", content, e);
      return content;
    }

  }

  @Override
  public String convertPayload(Object payload) {
    if (payload == null) {
      return "";
    }

    return JSON.toJSONString(payload);
  }

  @Data
  static class TransactionalMessage {

    private Headers headers;
    private String bizBody;
  }

  @Data
  static class Headers {

    private String actionId;
    private String txId;
  }

}
