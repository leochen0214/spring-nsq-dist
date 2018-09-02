package com.youzan.spring.nsq.config;

import com.youzan.spring.nsq.core.MessageListenerContainerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper bean for registering {@link NsqListenerEndpoint} with a {@link
 * NsqListenerEndpointRegistry}.
 *
 * @author: clong
 * @date: 2018-08-30
 */
public class NsqListenerEndpointRegistrar implements BeanFactoryAware, InitializingBean {

  private final List<NsqListenerEndpointDescriptor> endpointDescriptors = new ArrayList<>();
  private NsqListenerEndpointRegistry endpointRegistry;
  private MessageHandlerMethodFactory messageHandlerMethodFactory;
  private MessageListenerContainerFactory<?> containerFactory;
  private String containerFactoryBeanName;
  private BeanFactory beanFactory;
  private boolean startImmediately;


  /**
   * Set the {@link NsqListenerEndpointRegistry} instance to use.
   *
   * @param endpointRegistry the {@link NsqListenerEndpointRegistry} instance to use.
   */
  public void setEndpointRegistry(NsqListenerEndpointRegistry endpointRegistry) {
    this.endpointRegistry = endpointRegistry;
  }

  /**
   * Return the {@link NsqListenerEndpointRegistry} instance for this registrar, may be {@code
   * null}.
   *
   * @return the {@link NsqListenerEndpointRegistry} instance for this registrar, may be {@code
   * null}.
   */
  public NsqListenerEndpointRegistry getEndpointRegistry() {
    return this.endpointRegistry;
  }

  /**
   * Set the {@link MessageHandlerMethodFactory} to use to configure the message listener
   * responsible to serve an endpoint detected by this processor.
   * <p>By default,
   * {@link org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory}
   * is used and it can be configured further to support additional method arguments or to customize
   * conversion and validation support. See {@link org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory}
   * javadoc for more details.
   *
   * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory} instance.
   */
  public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
    this.messageHandlerMethodFactory = messageHandlerMethodFactory;
  }

  /**
   * Return the custom {@link MessageHandlerMethodFactory} to use, if any.
   *
   * @return the custom {@link MessageHandlerMethodFactory} to use, if any.
   */
  public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
    return this.messageHandlerMethodFactory;
  }

  /**
   * Set the {@link MessageListenerContainerFactory} to use in case a {@link NsqListenerEndpoint} is
   * registered with a {@code null} container factory.
   * <p>Alternatively, the bean name of the {@link MessageListenerContainerFactory} to use
   * can be specified for a lazy lookup, see {@link #setContainerFactoryBeanName}.
   *
   * @param containerFactory the {@link MessageListenerContainerFactory} instance.
   */
  public void setContainerFactory(MessageListenerContainerFactory<?> containerFactory) {
    this.containerFactory = containerFactory;
  }

  /**
   * Set the bean name of the {@link MessageListenerContainerFactory} to use in case a {@link
   * NsqListenerEndpoint} is registered with a {@code null} container factory. Alternatively, the
   * container factory instance can be registered directly: see {@link
   * #setContainerFactory(MessageListenerContainerFactory)}.
   *
   * @param containerFactoryBeanName the {@link MessageListenerContainerFactory} bean name.
   * @see #setBeanFactory
   */
  public void setContainerFactoryBeanName(String containerFactoryBeanName) {
    this.containerFactoryBeanName = containerFactoryBeanName;
  }


  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public void afterPropertiesSet() {
    registerAllEndpoints();
  }

  public void registerAllEndpoints() {
    synchronized (endpointDescriptors) {
      endpointDescriptors.forEach(descriptor -> endpointRegistry
          .registerListenerContainer(descriptor.endpoint, resolveContainerFactory(descriptor)));
      this.startImmediately = true;  // trigger immediate startup
    }
  }

  private MessageListenerContainerFactory<?> resolveContainerFactory(NsqListenerEndpointDescriptor descriptor) {
    if (descriptor.containerFactory != null) {
      return descriptor.containerFactory;
    }

    if (this.containerFactory != null) {
      return this.containerFactory;
    }

    if (this.containerFactoryBeanName != null) {
      Assert.state(this.beanFactory != null,
                   "BeanFactory must be set to obtain container factory by bean name");
      this.containerFactory = this.beanFactory.getBean(this.containerFactoryBeanName, MessageListenerContainerFactory.class);
      return this.containerFactory;  // Consider changing this if live change of the factory is required
    }

    throw new IllegalStateException("Could not resolve the " +
                                    MessageListenerContainerFactory.class.getSimpleName()
                                    + " to use for [" +
                                    descriptor.endpoint
                                    + "] no factory was given and no default is set.");

  }

  /**
   * Register a new {@link NsqListenerEndpoint} alongside the
   * {@link MessageListenerContainerFactory} to use to create the underlying container.
   * <p>The {@code factory} may be {@code null} if the default factory has to be
   * used for that endpoint.
   * @param endpoint the {@link NsqListenerEndpoint} instance to register.
   * @param factory the {@link MessageListenerContainerFactory} to use.
   */
  public void registerEndpoint(NsqListenerEndpoint endpoint, MessageListenerContainerFactory<?> factory) {
    Assert.notNull(endpoint, "Endpoint must be set");
    Assert.hasText(endpoint.getId(), "Endpoint id must be set");
    // Factory may be null, we defer the resolution right before actually creating the container
    NsqListenerEndpointDescriptor descriptor = new NsqListenerEndpointDescriptor(endpoint, factory);
    synchronized (endpointDescriptors) {
      if (startImmediately) { // Register and start immediately
        this.endpointRegistry.registerListenerContainer(descriptor.endpoint,
                                                        resolveContainerFactory(descriptor), true);
      } else {
        this.endpointDescriptors.add(descriptor);
      }
    }
  }

  /**
   * Register a new {@link NsqListenerEndpoint} using the default
   * {@link MessageListenerContainerFactory} to create the underlying container.
   * @param endpoint the {@link NsqListenerEndpoint} instance to register.
   * @see #setContainerFactory(MessageListenerContainerFactory)
   * @see #registerEndpoint(NsqListenerEndpoint, MessageListenerContainerFactory)
   */
  public void registerEndpoint(NsqListenerEndpoint endpoint) {
    registerEndpoint(endpoint, null);
  }
  
  


  private static final class NsqListenerEndpointDescriptor {

    private final NsqListenerEndpoint endpoint;
    private final MessageListenerContainerFactory<?> containerFactory;

    private NsqListenerEndpointDescriptor(NsqListenerEndpoint endpoint,
                                          MessageListenerContainerFactory<?> containerFactory) {
      this.endpoint = endpoint;
      this.containerFactory = containerFactory;
    }

  }


}
