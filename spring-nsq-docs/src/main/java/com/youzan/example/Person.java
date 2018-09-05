package com.youzan.example;

import lombok.Data;

/**
 * @author: clong
 * @date: 2018-09-02
 */
// tag::configuration[]
@Data
public class Person {

  private String name;
  private int age;
  private String sex;

  @Override
  public String toString() {
    return "name=" + name + ", age=" + age + ", sex=" + sex;
  }

}
// end::configuration[]
