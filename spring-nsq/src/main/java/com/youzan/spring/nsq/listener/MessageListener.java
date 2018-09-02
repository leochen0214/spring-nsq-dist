package com.youzan.spring.nsq.listener;

import com.youzan.nsq.client.entity.NSQMessage;

/**
 * Listener for handling individual incoming nsq messages.
 *
 * @author: clong
 * @date: 2018-08-29
 */
@FunctionalInterface
public interface MessageListener extends GenericMessageListener<NSQMessage> {

}
