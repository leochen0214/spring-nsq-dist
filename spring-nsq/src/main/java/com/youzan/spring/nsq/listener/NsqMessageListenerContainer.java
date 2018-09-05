package com.youzan.spring.nsq.listener;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.core.RequeuePolicy;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-01
 */
@Slf4j
public class NsqMessageListenerContainer extends AbstractMessageListenerContainer {

  private volatile Consumer consumer;

  /**
   * Construct an instance with the provided factory and properties.
   *
   * @param consumerFactory     the factory.
   * @param containerProperties the properties.
   */
  public NsqMessageListenerContainer(ConsumerFactory consumerFactory,
                                     ConsumerConfigProperties containerProperties) {
    super(consumerFactory, containerProperties);
  }

  @Override
  protected void doStart() {
    if (isRunning()) {
      return;
    }

    ConsumerConfigProperties properties = getContainerProperties();
    MessageListener listener = getMessageListener(properties);
    ListenerType listenerType = ListenerUtils.determineListenerType(listener);
    RequeuePolicy requeuePolicy = properties.getRequeuePolicy();

    consumer = consumerFactory.createConsumer(properties);
    consumer.setAutoFinish(properties.isAutoFinish());
    consumer.setMessageHandler(
        message -> doInvokeMessageListener(message, listener, listenerType, requeuePolicy));
    consumer.subscribe(toArray(properties));
    startConsumer();
  }

  private void startConsumer() {
    try {
      this.consumer.start();
      setRunning(true);
      log.info("started consumer={}", this.consumer);
    } catch (NSQException e) {
      log.error("consumer start failed", e);
    }
  }


  @Override
  protected void doStop(Runnable callback) {
    if (isRunning()) {
      consumer.close();
      callback.run();
      setRunning(false);
    }
  }

  private MessageListener getMessageListener(ConsumerConfigProperties properties) {
    Object messageListener = properties.getMessageListener();
    Assert.state(messageListener != null, "A MessageListener is required");
    Assert.state(messageListener instanceof MessageListener, "Listener must be a MessageListener");
    return (MessageListener) messageListener;
  }

  private void doInvokeMessageListener(NSQMessage message, MessageListener listener,
                                       ListenerType listenerType, RequeuePolicy requeuePolicy) {
    try {
      switch (listenerType) {
        case CONSUMER_AWARE:
          listener.onMessage(message, this.consumer);
          break;
        case SIMPLE:
          listener.onMessage(message);
          break;
      }
    } catch (Exception e) {
      if (errorHandler != null) {
        errorHandler.handle(e, message, this.consumer);
      }
      if (requeuePolicy != null) {
        requeuePolicy.requeue(message);
      }
      throw e;
    }
  }

  private Topic[] toArray(ConsumerConfigProperties properties) {
    Topic[] arr = new Topic[properties.getTopics().size()];

    int i = 0;
    for (String topic : properties.getTopics()) {
      Topic t = new Topic(topic);
      Integer partitionID = properties.getPartitionID();
      if (partitionID != null && partitionID != -1) {
        t.setPartitionID(partitionID);
      }
      arr[i] = t;
      i++;
    }

    return arr;
  }


}
