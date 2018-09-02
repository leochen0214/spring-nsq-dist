package com.youzan.spring.nsq.listener;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

import org.springframework.util.Assert;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-01
 */
@Slf4j
public class NsqMessageListenerContainer extends AbstractMessageListenerContainer {

  private volatile Consumer consumer;
  private ListenerType listenerType;
  private MessageListener listener;


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

    this.consumer = consumerFactory.createConsumer(properties);
    this.consumer.setAutoFinish(properties.isAutoFinish());
    Object messageListener = properties.getMessageListener();
    Assert.state(messageListener != null, "A MessageListener is required");
    Assert.state(messageListener instanceof MessageListener, "Listener must be a MessageListener");
    this.listener = (MessageListener) messageListener;

    this.listenerType = ListenerUtils.determineListenerType(this.listener);

    this.consumer.setMessageHandler(this::doInvokeMessageListener);
    this.consumer.subscribe(toArray(properties));
    try {
      consumer.start();
    } catch (NSQException e) {
      log.error("consumer start failed", e);
    }

    setRunning(true);
  }


  @Override
  protected void doStop(Runnable callback) {
    if (isRunning()) {
      consumer.close();
      callback.run();
      setRunning(false);
    }
  }

  private void doInvokeMessageListener(final NSQMessage message) {
    try {
      switch (this.listenerType) {
        case CONSUMER_AWARE:
          this.listener.onMessage(message, this.consumer);
          break;

        case SIMPLE:
          this.listener.onMessage(message);
          break;
      }
    } catch (Exception e) {
      if (errorHandler != null) {
        errorHandler.handle(e, message, this.consumer);
      }
      throw e;
    }
  }

  private Topic[] toArray(ConsumerConfigProperties properties){
    Topic[] arr = new Topic[properties.getTopics().size()];

    int i = 0;
    for (String topic : properties.getTopics()) {
      Topic t = new Topic(topic);
      if (properties.getPartitionID() != null) {
        t.setPartitionID(properties.getPartitionID());
      }
      arr[i] = t;
      i++;
    }

    return arr;
  }


}
