package com.youzan.spring.nsq.config;

import com.youzan.spring.nsq.core.MessageListenerContainerFactory;

/**
 * @author: clong
 * @date: 2018-08-30
 */
public class NsqConfigConstants {

  /**
   * The bean name of the default {@link MessageListenerContainerFactory}.
   */
  public static final String DEFAULT_NSQ_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "nsqMessageListenerContainerFactory";


  /**
   * The bean name of the internally managed nsq listener annotation processor.
   */
  public static final String NSQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME = "defaultNsqListenerAnnotationBeanPostProcessor";

  /**
   * The bean name of the internally managed nsq listener endpoint registry.
   */
  public static final String NSQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME = "defaultNsqListenerEndpointRegistry";


  public static final int DEFAULT_PHASE = Integer.MAX_VALUE - 100; // late phase
}
