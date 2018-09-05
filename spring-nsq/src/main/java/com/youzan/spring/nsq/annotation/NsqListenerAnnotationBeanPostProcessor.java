package com.youzan.spring.nsq.annotation;

import com.youzan.spring.nsq.config.MethodNsqListenerEndpoint;
import com.youzan.spring.nsq.config.NsqConfigConstants;
import com.youzan.spring.nsq.core.MessageListenerContainerFactory;
import com.youzan.spring.nsq.config.NsqListenerEndpointRegistrar;
import com.youzan.spring.nsq.config.NsqListenerEndpointRegistry;
import com.youzan.spring.nsq.handler.NsqListenerErrorHandler;
import com.youzan.spring.nsq.support.ExpressionResolver;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import static com.youzan.spring.nsq.config.NsqConfigConstants.DEFAULT_NSQ_LISTENER_CONTAINER_FACTORY_BEAN_NAME;

/**
 * Bean post-processor that registers methods annotated with {@link NsqListener} to be invoked by a
 * nsq message listener container created under the covers by a {@link
 * MessageListenerContainerFactory} according to the parameters of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link NsqListener}.
 *
 * <p>This post-processor is automatically registered by Spring's {@link EnableNsq}
 * annotation.
 *
 * <p>Auto-detect any {@link NsqListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container factory or for
 * fine-grained control over endpoints registration. See {@link EnableNsq} Javadoc for complete
 * usage details.
 *
 * @author: clong
 * @date: 2018-08-30
 * @see NsqListener
 * @see NsqListenerErrorHandler
 * @see EnableNsq
 * @see NsqListenerConfigurer
 * @see NsqListenerEndpointRegistrar
 * @see NsqListenerEndpointRegistry
 * @see com.youzan.spring.nsq.config.NsqListenerEndpoint
 * @see MethodNsqListenerEndpoint
 */
@Slf4j
public class NsqListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered,
                                                               BeanFactoryAware,
                                                               SmartInitializingSingleton {

  private static final String GENERATED_ID_PREFIX = "NsqListenerEndpointContainer#";

  private final Set<Class<?>> nonAnnotatedClasses =
      Collections.newSetFromMap(new ConcurrentHashMap<>(64));
  private final NsqListenerEndpointRegistrar registrar = new NsqListenerEndpointRegistrar();
  private final AtomicInteger counter = new AtomicInteger();

  private NsqListenerEndpointRegistry endpointRegistry;
  private String containerFactoryBeanName = DEFAULT_NSQ_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
  private BeanFactory beanFactory;
  private NsqMessageHandlerMethodFactoryAdapter messageHandlerMethodFactory =
      new NsqMessageHandlerMethodFactoryAdapter();
  private ExpressionResolver resolver;


  /**
   * Set the {@link NsqListenerEndpointRegistry} that will hold the created endpoint and manage the
   * lifecycle of the related listener container.
   *
   * @param endpointRegistry the {@link NsqListenerEndpointRegistry} to set.
   */
  public void setEndpointRegistry(NsqListenerEndpointRegistry endpointRegistry) {
    this.endpointRegistry = endpointRegistry;
  }

  /**
   * Set the {@link MessageHandlerMethodFactory} to use to configure the message listener
   * responsible to serve an endpoint detected by this processor.
   * <p>By default, {@link DefaultMessageHandlerMethodFactory} is used and it
   * can be configured further to support additional method arguments or to customize conversion and
   * validation support. See {@link DefaultMessageHandlerMethodFactory} Javadoc for more details.
   *
   * @param factory the {@link MessageHandlerMethodFactory} instance.
   */
  public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory factory) {
    this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(factory);
  }


  @Override
  public int getOrder() {
    return LOWEST_PRECEDENCE;
  }


  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    this.resolver = new ExpressionResolver(beanFactory);
  }

  @Override
  public void afterSingletonsInstantiated() {
    this.registrar.setBeanFactory(this.beanFactory);
    applyNsqListenerConfigurerIfPossible();

    if (this.registrar.getEndpointRegistry() == null) {
      if (this.endpointRegistry == null) {
        this.endpointRegistry =
            this.beanFactory.getBean(NsqConfigConstants.NSQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                                     NsqListenerEndpointRegistry.class);
      }
      this.registrar.setEndpointRegistry(endpointRegistry);
    }

    if (StringUtils.hasText(containerFactoryBeanName)) {
      this.registrar.setContainerFactoryBeanName(containerFactoryBeanName);
    }

    // Set the custom handler method factory once resolved by the configurer
    MessageHandlerMethodFactory handlerMethodFactory = registrar.getMessageHandlerMethodFactory();
    if (handlerMethodFactory != null) {
      this.setMessageHandlerMethodFactory(handlerMethodFactory);
    }

    // Actually register all listeners
    this.registrar.afterPropertiesSet();
  }

  private void applyNsqListenerConfigurerIfPossible() {
    if (this.beanFactory instanceof ListableBeanFactory) {
      Map<String, NsqListenerConfigurer> instances =
          ((ListableBeanFactory) this.beanFactory).getBeansOfType(NsqListenerConfigurer.class);
      if (!instances.isEmpty()) {
        instances.values().forEach(configurer -> configurer.configureNsqListeners(this.registrar));
      }
    }
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();
    if (!this.nonAnnotatedClasses.contains(clazz)) {
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      Map<Method, Set<NsqListener>> annotatedMethods = MethodIntrospector
          .selectMethods(targetClass, getMetadataLookup());
      if (annotatedMethods.isEmpty()) {
        this.nonAnnotatedClasses.add(clazz);
        if (log.isDebugEnabled()) {
          log.debug("No @NsqListener annotations found on bean type: {}", clazz);
        }
      } else {
        // Non-empty set of methods
        annotatedMethods.forEach((method, nsqListeners) -> {
          nsqListeners.forEach(listener -> processListener(listener, method, bean, beanName));
        });

        if (log.isDebugEnabled()) {
          log.debug("{} @NsqListener methods processed on bean '{} ': annotatedMethods={}",
                    annotatedMethods.size(), beanName, annotatedMethods);
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("nonAnnotatedClasses of @NsqListener total number is: {}",
                nonAnnotatedClasses.size());
    }

    return bean;
  }

  private MethodIntrospector.MetadataLookup<Set<NsqListener>> getMetadataLookup() {
    return method -> {
      Set<NsqListener> listenerMethods = findListenerAnnotations(method);
      return (!listenerMethods.isEmpty() ? listenerMethods : null);
    };
  }

  /*
   * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
   */
  private Set<NsqListener> findListenerAnnotations(Method method) {
    Set<NsqListener> listeners = new HashSet<>();
    NsqListener ann = AnnotationUtils.findAnnotation(method, NsqListener.class);
    if (ann != null) {
      listeners.add(ann);
    }
    NsqListeners anns = AnnotationUtils.findAnnotation(method, NsqListeners.class);
    if (anns != null) {
      listeners.addAll(Arrays.asList(anns.value()));
    }
    return listeners;
  }

  protected void processListener(NsqListener listener, Method method, Object bean,
                                 String beanName) {
    Method m = checkProxy(method, bean);
    MethodNsqListenerEndpoint endpoint = new MethodNsqListenerEndpoint();
    endpoint.setId(getEndpointId(listener));
    endpoint.setTopics(resolveTopics(listener));
    endpoint.setChannel(resolver.resolveExpressionAsString(listener.channel(), "channel"));
    endpoint.setAutoStartup(resolver.resolveExpressionAsBoolean(listener.autoStartup(), "autoStartup"));
    endpoint.setPartitionID(resolver.resolveExpressionAsInteger(listener.partitionID(), "partitionID"));
    endpoint.setOrdered(listener.ordered());
    endpoint.setAutoFinish(listener.autoFinish());
    setGroup(endpoint, listener);
    endpoint.setBean(bean);
    endpoint.setMethod(m);
    endpoint.setBeanFactory(beanFactory);
    endpoint.setErrorHandler(getErrorHandler(listener));
    this.messageHandlerMethodFactory.setBeanFactory(beanFactory);
    endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);

    MessageListenerContainerFactory<?> f = getListenerContainerFactory(listener, beanName, m);
    this.registrar.registerEndpoint(endpoint, f);
  }

  private MessageListenerContainerFactory<?> getListenerContainerFactory(NsqListener listener,
                                                                         String beanName,
                                                                         Method methodToUse) {
    String containerFactoryBeanName = resolver.resolve(listener.containerFactory());
    if (StringUtils.hasText(containerFactoryBeanName)) {
      try {
        return beanFactory.getBean(containerFactoryBeanName, MessageListenerContainerFactory.class);
      } catch (NoSuchBeanDefinitionException ex) {
        throw new BeanInitializationException(
            "Could not register nsq listener endpoint on [" + methodToUse
            + "] for bean " + beanName + ", no " + MessageListenerContainerFactory.class
                .getSimpleName()
            + " with id '" + containerFactoryBeanName + "' was found in the application context",
            ex);
      }
    }
    return null;
  }

  private NsqListenerErrorHandler getErrorHandler(NsqListener listener) {
    String errorHandlerBeanName =
        resolver.resolveExpressionAsString(listener.errorHandler(), "errorHandler");

    if (StringUtils.hasText(errorHandlerBeanName)) {
      return beanFactory.getBean(errorHandlerBeanName, NsqListenerErrorHandler.class);
    }
    return null;
  }


  private void setGroup(MethodNsqListenerEndpoint endpoint, NsqListener nsqListener) {
    String group = nsqListener.containerGroup();
    if (StringUtils.hasText(group)) {
      Object resolvedGroup = resolver.resolveExpression(group);
      if (resolvedGroup instanceof String) {
        endpoint.setGroup((String) resolvedGroup);
      }
    }
  }


  private String getEndpointId(NsqListener listener) {
    if (StringUtils.hasText(listener.id())) {
      return resolver.resolve(listener.id());
    } else {
      return GENERATED_ID_PREFIX + this.counter.getAndIncrement();
    }
  }

  private String[] resolveTopics(NsqListener listener) {
    String[] topics = listener.topics();
    List<String> result = new ArrayList<>();

    if (topics.length > 0) {
      for (String topic1 : topics) {
        Object topic = resolver.resolveExpression(topic1);
        resolver.resolveAsString(topic, result);
      }
    }
    return result.toArray(new String[0]);
  }


  private Method checkProxy(Method methodArg, Object bean) {
    Method method = methodArg;
    if (AopUtils.isJdkDynamicProxy(bean)) {
      try {
        // Found a @NsqListener method on the target class for this JDK proxy ->
        // is it also present on the proxy itself?
        method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
        Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
        for (Class<?> iface : proxiedInterfaces) {
          try {
            method = iface.getMethod(method.getName(), method.getParameterTypes());
            break;
          } catch (NoSuchMethodException noMethod) {
          }
        }
      } catch (SecurityException ex) {
        ReflectionUtils.handleReflectionException(ex);
      } catch (NoSuchMethodException ex) {
        throw new IllegalStateException(String.format(
            "@NsqListener method '%s' found on bean target class '%s', " +
            "but not found in any interface(s) for bean JDK proxy. Either " +
            "pull the method up to an interface or switch to subclass (CGLIB) " +
            "proxies by setting proxy-target-class/proxyTargetClass " +
            "attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()),
                                        ex);
      }
    }
    return method;
  }

}
