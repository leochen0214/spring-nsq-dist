package com.youzan.spring.nsq.core;

import com.youzan.spring.nsq.config.NsqListenerEndpoint;
import com.youzan.spring.nsq.handler.ErrorHandler;
import com.youzan.spring.nsq.listener.NsqMessageListenerContainer;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;
import com.youzan.spring.nsq.support.converter.NSQMessageConverter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

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


  private final ConsumerConfigProperties containerProperties = new ConsumerConfigProperties();

  private ErrorHandler errorHandler;

  private ConsumerFactory consumerFactory;

  private Boolean autoStartup;

  private Integer phase;

  private NSQMessageConverter messageConverter;
  private ApplicationEventPublisher applicationEventPublisher;


  @Override
  public NsqMessageListenerContainer createListenerContainer(NsqListenerEndpoint endpoint) {

    containerProperties.setTopics(endpoint.getTopics());
    containerProperties.setChannel(endpoint.getChannel());
    containerProperties.setOrdered(endpoint.ordered());
    containerProperties.setAutoFinish(endpoint.autoFinish());


    NsqMessageListenerContainer instance = createContainerInstance(endpoint);

    if (endpoint.getId() != null) {
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
    return new NsqMessageListenerContainer(consumerFactory, containerProperties);
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
