package com.youzan.spring.nsq.annotation;

import com.youzan.spring.nsq.config.NsqListenerEndpointRegistrar;
import com.youzan.spring.nsq.core.MessageListenerContainerFactory;

/**
 * Optional interface to be implemented by Spring managed bean willing to customize how nsq listener
 * endpoints are configured. Typically used to defined the default {@link
 * MessageListenerContainerFactory NsqListenerContainerFactory} to use or
 * for registering nsq endpoints in a <em>programmatic</em> fashion as opposed to the
 * <em>declarative</em> approach of using the @{@link NsqListener} annotation.
 *
 * <p>See @{@link EnableNsq} for detailed usage examples.
 *
 * @author: clong
 * @date: 2018-08-30
 */
public interface NsqListenerConfigurer {

  /**
   * Callback allowing a {@link com.youzan.spring.nsq.config.NsqListenerEndpointRegistry
   * NsqListenerEndpointRegistry} and specific {@link com.youzan.spring.nsq.config.NsqListenerEndpoint
   * NsqListenerEndpoint} instances to be registered against the given {@link
   * NsqListenerEndpointRegistrar}. The default {@link MessageListenerContainerFactory
   * NsqListenerContainerFactory} can also be customized.
   *
   * @param registrar the registrar to be configured
   */
  void configureNsqListeners(NsqListenerEndpointRegistrar registrar);


}
