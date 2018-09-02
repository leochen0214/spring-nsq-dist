package com.youzan.spring.nsq.listener;

import org.springframework.util.Assert;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class ListenerUtils {

  public static ListenerType determineListenerType(Object listener) {
    Assert.notNull(listener, "Listener cannot be null");
    ListenerType listenerType;

    if (listener instanceof ConsumerAwareMessageListener) {
      listenerType = ListenerType.CONSUMER_AWARE;
    } else if (listener instanceof GenericMessageListener) {
      listenerType = ListenerType.SIMPLE;
    } else {
      throw new IllegalArgumentException(
          "Unsupported listener type: " + listener.getClass().getName());
    }
    return listenerType;
  }


}
