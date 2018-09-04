package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.entity.NSQMessage;

import com.alibaba.fastjson.JSON;

import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-03
 */
@Slf4j
public class JsonMessageConverter extends MessagingMessageConverter {


  @Override
  protected Object extractAndConvertValue(NSQMessage message, Type type) {
    String content = message.getReadableContent();
    if (!StringUtils.hasText(content)) {
      return null;
    }

    try {
      Object result = JSON.parseObject(content, type);
      if (log.isDebugEnabled()){
        log.debug("extractAndConvertValue return value={}, type class={}",
                  result, result.getClass().getName());
      }
      return result;
    } catch (Exception e) {
      log.error("", e);
      return content;
    }

  }

  @Override
  protected String convertPayload(Message<?> message) {
    Object payload = message.getPayload();

    if (payload == null) {
      return "";
    }

    return JSON.toJSONString(payload);
  }
}
