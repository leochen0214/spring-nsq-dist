package com.youzan.spring.nsq.support.util;

import org.springframework.messaging.Message;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author: clong
 * @date: 2018-09-02
 */
public class NsqUtils {

  /**
   * Return true if the method return type is {@link Message} or
   * {@code Collection<Message<?>>}.
   * @param method the method.
   * @return true if it returns message(s).
   */
  public static boolean returnTypeMessageOrCollectionOf(Method method) {
    Type returnType = method.getGenericReturnType();
    if (returnType.equals(Message.class)) {
      return true;
    }
    if (returnType instanceof ParameterizedType) {
      ParameterizedType prt = (ParameterizedType) returnType;
      Type rawType = prt.getRawType();
      if (rawType.equals(Message.class)) {
        return true;
      }
      if (rawType.equals(Collection.class)) {
        Type collectionType = prt.getActualTypeArguments()[0];
        if (collectionType.equals(Message.class)) {
          return true;
        }
        return collectionType instanceof ParameterizedType
               && ((ParameterizedType) collectionType).getRawType().equals(Message.class);
      }
    }
    return false;

  }


}
