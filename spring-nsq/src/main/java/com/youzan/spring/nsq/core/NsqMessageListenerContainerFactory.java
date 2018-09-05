package com.youzan.spring.nsq.core;

import com.youzan.spring.nsq.config.NsqListenerEndpoint;
import com.youzan.spring.nsq.handler.ErrorHandler;
import com.youzan.spring.nsq.listener.NsqMessageListenerContainer;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-02
 */
@Slf4j
@Getter
@Setter
public class NsqMessageListenerContainerFactory
    implements MessageListenerContainerFactory<NsqMessageListenerContainer>,
               ApplicationEventPublisherAware {

  private ErrorHandler errorHandler;

  private ConsumerFactory consumerFactory;

  private Boolean autoStartup;

  private Integer phase;

  private NSQMessageConverter messageConverter;
  private ApplicationEventPublisher applicationEventPublisher;


  @Override
  public NsqMessageListenerContainer createListenerContainer(NsqListenerEndpoint endpoint) {
    NsqMessageListenerContainer instance = createContainerInstance(endpoint);
    if (StringUtils.hasText(endpoint.getId())) {
      instance.setBeanName(endpoint.getId());
    }
    endpoint.setupListenerContainer(instance, this.messageConverter);
    initializeContainer(instance, endpoint);

    return instance;
  }


  @Override
  public NsqMessageListenerContainer createContainer(String... topics) {
    //todo
    return null;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  protected NsqMessageListenerContainer createContainerInstance(NsqListenerEndpoint endpoint) {
    ConsumerConfigProperties properties = new ConsumerConfigProperties();
    properties.setTopics(endpoint.getTopics());
    properties.setChannel(endpoint.getChannel());
    properties.setOrdered(endpoint.ordered());
    properties.setAutoFinish(endpoint.autoFinish());
    properties.setPartitionID(endpoint.getPartitionID());
    properties.setRequeuePolicy(endpoint.getRequeuePolicy());

    return new NsqMessageListenerContainer(consumerFactory, properties);
  }


  /**
   * Further initialize the specified container.
   * <p>Subclasses can inherit from this method to apply extra
   * configuration if necessary.
   *
   * @param instance the container instance to configure.
   * @param endpoint the endpoint.
   */
  protected void initializeContainer(NsqMessageListenerContainer instance,
                                     NsqListenerEndpoint endpoint) {
    if (this.errorHandler != null) {
      instance.setErrorHandler(this.errorHandler);
    }

    if (endpoint.getAutoStartup() != null) {
      instance.setAutoStartup(endpoint.getAutoStartup());
    } else if (this.autoStartup != null) {
      instance.setAutoStartup(this.autoStartup);
    }

    if (this.phase != null) {
      instance.setPhase(this.phase);
    }

    if (this.applicationEventPublisher != null) {
      instance.setApplicationEventPublisher(this.applicationEventPublisher);
    }
  }


}
