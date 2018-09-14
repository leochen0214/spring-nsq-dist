package com.youzan.spring.nsq.transaction.util;

import java.util.Collection;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * @author: clong
 * @date: 2017-10-09
 */
public abstract class SqlUtils {

  private SqlUtils() {

  }

  public static String inCondition(int size) {
    StringJoiner joiner = new StringJoiner(",", "(", ")");
    for (int i = 0; i < size; i++) {
      joiner.add("?");
    }
    return joiner.toString();
  }


  public static String inCondition(Collection<String> orderNos) {
    return orderNos.stream().map(s -> "'" + s + "'")
        .collect(joining(",", "(", ")"));
  }


  public static int getAffectedRows(int[] rows) {
    if (rows == null) {
      return 0;
    }

    int count = 0;
    for (int row : rows) {
      count += row;
    }
    return count;
  }


}
