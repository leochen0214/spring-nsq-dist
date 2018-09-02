package com.youzan.spring.nsq.config;

import com.youzan.spring.nsq.core.MessageListenerContainerFactory;
import com.youzan.spring.nsq.listener.MessageListenerContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.youzan.spring.nsq.config.NsqConfigConstants.DEFAULT_PHASE;

/**
 * Creates the necessary {@link MessageListenerContainer} instances for the registered {@linkplain
 * NsqListenerEndpoint endpoints}. Also manages the lifecycle of the listener containers, in
 * particular within the lifecycle of the application context.
 *
 * <p>Contrary to {@link MessageListenerContainer}s created manually, listener
 * containers managed by registry are not beans in the application context and are not candidates
 * for autowiring. Use {@link #getListenerContainers()} if you need to access this registry's
 * listener containers for management purposes. If you need to access to a specific message listener
 * container, use {@link #getListenerContainer(String)} with the id of the endpoint.
 *
 * @author: clong
 * @date: 2018-08-30
 */
public class NsqListenerEndpointRegistry implements DisposableBean, SmartLifecycle,
                                                    ApplicationContextAware,
                                                    ApplicationListener<ContextRefreshedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(NsqListenerEndpointRegistry.class);

  private final ConcurrentHashMap<String, MessageListenerContainer> listenerContainers =
      new ConcurrentHashMap<>();
  private int phase = DEFAULT_PHASE;
  private ConfigurableApplicationContext applicationContext;
  private boolean contextRefreshed;


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    if (applicationContext instanceof ConfigurableApplicationContext) {
      this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
  }


  /**
   * Return the {@link MessageListenerContainer} with the specified id or {@code null} if no such
   * container exists.
   *
   * @param id the id of the container
   * @return the container or {@code null} if no container with that id exists
   * @see NsqListenerEndpoint#getId()
   * @see #getListenerContainerIds()
   */
  public MessageListenerContainer getListenerContainer(String id) {
    Assert.hasText(id, "Container identifier must not be empty");
    return this.listenerContainers.get(id);
  }

  /**
   * Return the ids of the managed {@link MessageListenerContainer} instance(s).
   *
   * @return the ids.
   * @see #getListenerContainer(String)
   */
  public Set<String> getListenerContainerIds() {
    return Collections.unmodifiableSet(listenerContainers.keySet());
  }

  /**
   * Return the managed {@link MessageListenerContainer} instance(s).
   *
   * @return the managed {@link MessageListenerContainer} instance(s).
   */
  public Collection<MessageListenerContainer> getListenerContainers() {
    return Collections.unmodifiableCollection(listenerContainers.values());
  }

  /**
   * Create a message listener container for the given {@link NsqListenerEndpoint}.
   * <p>This create the necessary infrastructure to honor that endpoint
   * with regards to its configuration.
   *
   * @param endpoint the endpoint to add
   * @param factory  the listener factory to use
   * @see #registerListenerContainer(NsqListenerEndpoint, MessageListenerContainerFactory, boolean)
   */
  public void registerListenerContainer(NsqListenerEndpoint endpoint,
                                        MessageListenerContainerFactory<?> factory) {
    registerListenerContainer(endpoint, factory, false);
  }

  /**
   * Create a message listener container for the given {@link NsqListenerEndpoint}.
   * <p>This create the necessary infrastructure to honor that endpoint
   * with regards to its configuration.
   * <p>The {@code startImmediately} flag determines if the container should be
   * started immediately.
   *
   * @param endpoint         the endpoint to add.
   * @param factory          the {@link MessageListenerContainerFactory} to use.
   * @param startImmediately start the container immediately if necessary
   * @see #getListenerContainers()
   * @see #getListenerContainer(String)
   */

  public void registerListenerContainer(NsqListenerEndpoint endpoint,
                                        MessageListenerContainerFactory<?> factory,
                                        boolean startImmediately) {
    Assert.notNull(endpoint, "Endpoint must not be null");
    Assert.notNull(factory, "Factory must not be null");

    String id = endpoint.getId();
    Assert.hasText(id, "Endpoint id must not be empty");
    synchronized (listenerContainers) {
      Assert.state(!listenerContainers.containsKey(id),
                   "Another endpoint is already registered with id '" + id + "'");
      MessageListenerContainer container = createListenerContainer(endpoint, factory);
      listenerContainers.put(id, container);
      if (StringUtils.hasText(endpoint.getGroup()) && applicationContext != null) {
        getMessageListenerContainerGroup(endpoint).add(container);
      }
      if (startImmediately) {
        startIfNecessary(container);
      }
    }
  }

  /**
   * Create and start a new {@link MessageListenerContainer} using the specified factory.
   *
   * @param endpoint the endpoint to create a {@link MessageListenerContainer}.
   * @param factory  the {@link MessageListenerContainerFactory} to use.
   * @return the {@link MessageListenerContainer}.
   */
  protected MessageListenerContainer createListenerContainer(NsqListenerEndpoint endpoint,
                                                             MessageListenerContainerFactory<?> factory) {
    MessageListenerContainer container = factory.createListenerContainer(endpoint);

    if (container instanceof InitializingBean) {
      try {
        ((InitializingBean) container).afterPropertiesSet();
      } catch (Exception ex) {
        throw new BeanInitializationException("Failed to initialize message listener container",
                                              ex);
      }
    }

    int containerPhase = container.getPhase();
    if (container.isAutoStartup() &&
        containerPhase != DEFAULT_PHASE) {  // a custom phase value
      if (this.phase != DEFAULT_PHASE && this.phase != containerPhase) {
        throw new IllegalStateException("Encountered phase mismatch between container "
                                        + "factory definitions: " + this.phase + " vs "
                                        + containerPhase);
      }
      this.phase = container.getPhase();
    }

    return container;
  }


  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void start() {
    getListenerContainers().forEach(this::startIfNecessary);
  }

  /**
   * Start the specified {@link MessageListenerContainer} if it should be started on startup.
   *
   * @param messageListenerContainer the message listener container to start.
   * @see MessageListenerContainer#isAutoStartup()
   */
  private void startIfNecessary(MessageListenerContainer messageListenerContainer) {
    if (contextRefreshed || messageListenerContainer.isAutoStartup()) {
      messageListenerContainer.start();
    }
  }

  @Override
  public void stop() {
    getListenerContainers().forEach(MessageListenerContainer::stop);
  }

  @Override
  public void stop(Runnable callback) {
    Collection<MessageListenerContainer> containers = getListenerContainers();
    AggregatingCallback aggregatingCallback = new AggregatingCallback(containers.size(), callback);
    for (MessageListenerContainer listenerContainer : containers) {
      if (listenerContainer.isRunning()) {
        listenerContainer.stop(aggregatingCallback);
      } else {
        aggregatingCallback.run();
      }
    }
  }

  @Override
  public boolean isRunning() {
    return getListenerContainers().stream().anyMatch(MessageListenerContainer::isRunning);
  }

  @Override
  public int getPhase() {
    return phase;
  }

  @Override
  public void destroy() {
    getListenerContainers().forEach(this::doDestroyIfNecessary);
  }

  private void doDestroyIfNecessary(MessageListenerContainer listenerContainer) {
    if (listenerContainer instanceof DisposableBean) {
      try {
        ((DisposableBean) listenerContainer).destroy();
      } catch (Exception ex) {
        logger.warn("Failed to destroy message listener container", ex);
      }
    }

  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (event.getApplicationContext().equals(applicationContext)) {
      contextRefreshed = true;
    }
  }

  @SuppressWarnings("unchecked")
  private List<MessageListenerContainer> getMessageListenerContainerGroup(NsqListenerEndpoint endpoint) {
    List<MessageListenerContainer> containerGroup;
    if (applicationContext.containsBean(endpoint.getGroup())) {
      containerGroup = applicationContext.getBean(endpoint.getGroup(), List.class);
    } else {
      containerGroup = new ArrayList<>();
      applicationContext.getBeanFactory().registerSingleton(endpoint.getGroup(), containerGroup);
    }
    return containerGroup;
  }


  private static final class AggregatingCallback implements Runnable {

    private final AtomicInteger count;
    private final Runnable finishCallback;

    private AggregatingCallback(int count, Runnable finishCallback) {
      this.count = new AtomicInteger(count);
      this.finishCallback = finishCallback;
    }

    @Override
    public void run() {
      if (count.decrementAndGet() <= 0) {
        finishCallback.run();
      }
    }

  }


}
