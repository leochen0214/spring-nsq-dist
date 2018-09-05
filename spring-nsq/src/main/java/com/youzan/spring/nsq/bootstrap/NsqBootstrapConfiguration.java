package com.youzan.spring.nsq.bootstrap;

import com.youzan.spring.nsq.annotation.EnableNsq;
import com.youzan.spring.nsq.annotation.NsqListener;
import com.youzan.spring.nsq.annotation.NsqListenerAnnotationBeanPostProcessor;
import com.youzan.spring.nsq.config.NsqConfigConstants;
import com.youzan.spring.nsq.config.NsqListenerEndpointRegistry;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * {@code @Configuration} class that registers a {@link NsqListenerAnnotationBeanPostProcessor} bean
 * capable of processing Spring's @{@link NsqListener} annotation. Also register a default {@link
 * NsqListenerEndpointRegistry}.
 *
 * <p>This configuration class is automatically imported when using the @{@link EnableNsq}
 * annotation.  See {@link EnableNsq} Javadoc for complete usage.
 *
 * @author: clong
 * @date: 2018-09-02
 */
@Configuration
public class NsqBootstrapConfiguration {


  @SuppressWarnings("rawtypes")
  @Bean(name = NsqConfigConstants.NSQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public NsqListenerAnnotationBeanPostProcessor defaultNsqListenerAnnotationBeanPostProcessor() {
    return new NsqListenerAnnotationBeanPostProcessor();
  }

  @Bean(name = NsqConfigConstants.NSQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
  public NsqListenerEndpointRegistry defaultNsqListenerEndpointRegistry() {
    return new NsqListenerEndpointRegistry();
  }


}
