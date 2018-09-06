package com.youzan.spring.nsq.config;

import com.youzan.spring.nsq.core.MessageListenerContainerFactory;
import com.youzan.spring.nsq.core.RequeuePolicy;
import com.youzan.spring.nsq.listener.MessageListenerContainer;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import java.util.Collection;

/**
 * Model for a nsq listener endpoint. Can be used against a {@link com.youzan.spring.nsq.annotation.NsqListenerConfigurer
 * NsqListenerConfigurer} to register endpoints programmatically.
 *
 * @author: clong
 * @date: 2018-08-30
 */
public interface NsqListenerEndpoint {

  /**
   * Return the id of this endpoint.
   *
   * @return the id of this endpoint. The id can be further qualified when the endpoint is resolved
   * against its actual listener container.
   * @see MessageListenerContainerFactory#createListenerContainer
   */
  String getId();

  /**
   * Return the topics for this endpoint.
   *
   * @return the topics for this endpoint.
   */
  Collection<String> getTopics();

  /**
   * Return the channel for this endpoint.
   */
  String getChannel();


  /**
   * Return the topic partitionID
   */
  Integer getPartitionID();

  /**
   * The set of messages is ordered in one specified partition
   */
  boolean ordered();

  /**
   * Weather consumer auto finish
   */
  boolean autoFinish();

  /**
   * The bean name of RequenePolicy
   */
  RequeuePolicy getRequeuePolicy();

  /**
   * Return the group of this endpoint or null if not in a group.
   *
   * @return the group of this endpoint or null if not in a group.
   */
  String getGroup();

  /**
   * Return the autoStartup for this endpoint's container.
   *
   * @return the autoStartup.
   */
  Boolean getAutoStartup();

  /**
   * Setup the specified message listener container with the model defined by this endpoint.
   * <p>This endpoint must provide the requested missing option(s) of
   * the specified container to make it usable. Usually, this is about setting the {@code queues}
   * and the {@code messageListener} to use but an implementation may override any default setting
   * that was already set.
   *
   * @param listenerContainer the listener container to configure
   * @param messageConverter  the message converter - can be null
   */
  void setupListenerContainer(MessageListenerContainer listenerContainer,
                              NSQMessageConverter messageConverter);


  /**
   * Weather unpack transactional message, extract message really body.
   */
  boolean isUnpackMessage();
}
