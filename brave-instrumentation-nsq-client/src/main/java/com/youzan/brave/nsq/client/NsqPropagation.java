package com.youzan.brave.nsq.client;

import java.util.Map;

import brave.propagation.Propagation;

/**
 * @author: clong
 * @date: 2018-08-28
 */
public class NsqPropagation {

  static final Propagation.Setter<Map<String, Object>, String> HEADER_SETTER = (carrier, key, value) -> {
    carrier.remove(key);
    carrier.put(key, value);
  };

  static final Propagation.Getter<Map<String, Object>, String> HEADER_GETTER = (carrier, key) -> (String) carrier.get(key);


}
