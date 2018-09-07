package com.youzan.spring.nsq.support.converter;

import com.youzan.nsq.client.entity.NSQMessage;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: clong
 * @date: 2018-09-07
 */
@Slf4j
public class JsonMessageConverterTest {

  private JsonMessageConverter messageConverter = new JsonMessageConverter();

  @Test
  public void test_parsePaySuccessMessage() {
    String json =
        "{\"headers\":{\"actionId\":\"a\",\"txId\":\"tx-1\"},\"bizBody\":{\"age\":18,\"name\":\"jack\"}}";

    NSQMessage message = new NSQMessage();
    Whitebox.setInternalState(message, "readableContent", json);

    Person result = (Person) messageConverter.extractAndConvertValue(message, Person.class, true);
    log.info("result={}", result);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("jack");
    assertThat(result.getAge()).isEqualTo(18);
  }


  @Test
  public void test_parseListString_jsonIsEmpty() {
    NSQMessage message = new NSQMessage();
    Whitebox.setInternalState(message, "readableContent", "");

    List<String>
        result = (List<String>) messageConverter.extractAndConvertValue(message, List.class, false);

    assertThat(result).isNull();
  }


  @Test
  public void test_parseListStringMessage() {
    String json = "[\"a\",\"b\"]";
    NSQMessage message = new NSQMessage();
    Whitebox.setInternalState(message, "readableContent", json);

    List<String> result =
        (List<String>) messageConverter.extractAndConvertValue(message, List.class, false);

    assertThat(result).isNotEmpty();
    assertThat(result).containsSequence("a", "b");
  }

  @Data
  private static class Person {

    String name;
    int age;
  }

}
