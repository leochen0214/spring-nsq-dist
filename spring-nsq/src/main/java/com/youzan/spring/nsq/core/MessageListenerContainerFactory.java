package com.youzan.spring.nsq.core;

import com.youzan.spring.nsq.config.NsqListenerEndpoint;
import com.youzan.spring.nsq.listener.MessageListenerContainer;

/**
 * Factory for {@link MessageListenerContainer}s.
 *
 * @author: clong
 * @date: 2018-08-30
 */
public interface MessageListenerContainerFactory<C extends MessageListenerContainer> {

  /**
   * Create a {@link MessageListenerContainer} for the given {@link NsqListenerEndpoint}.
   * Containers created using this method are added to the listener endpoint registry.
   * @param endpoint the endpoint to configure
   * @return the created container
   */
  C createListenerContainer(NsqListenerEndpoint endpoint);


  /**
   * Create and configure a container without a listener; used to create containers that
   * are not used for KafkaListener annotations. Containers created using this method
   * are not added to the listener endpoint registry.
   * @param topics the topics.
   * @return the container.
   * @since 2.2
   */
  C createContainer(String... topics);



}
