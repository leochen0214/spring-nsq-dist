package com.youzan.spring.nsq.listener;

import com.youzan.spring.nsq.core.ConsumerFactory;
import com.youzan.spring.nsq.handler.ErrorHandler;
import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import static com.youzan.spring.nsq.config.NsqConfigConstants.DEFAULT_PHASE;


/**
 * @author: clong
 * @date: 2018-09-01
 */
@Slf4j
public abstract class AbstractMessageListenerContainer
    implements MessageListenerContainer, BeanNameAware, ApplicationEventPublisherAware {

  protected final ConsumerFactory consumerFactory; // NOSONAR (final)
  private final ConsumerConfigProperties containerProperties;

  private final Object lifecycleMonitor = new Object();

  private String beanName;

  private ApplicationEventPublisher applicationEventPublisher;

  protected ErrorHandler errorHandler;

  private boolean autoStartup = true;

  private int phase = DEFAULT_PHASE;

  private volatile boolean running = false;

  /**
   * Construct an instance with the provided factory and properties.
   *
   * @param consumerFactory     the factory.
   * @param containerProperties the properties.
   */
  protected AbstractMessageListenerContainer(ConsumerFactory consumerFactory,
                                             ConsumerConfigProperties containerProperties) {
    Assert.notNull(containerProperties, "'containerProperties' cannot be null");
    this.consumerFactory = consumerFactory;
    this.containerProperties = containerProperties;
  }

  @Override
  public void setBeanName(String name) {
    this.beanName = name;
  }

  public String getBeanName() {
    return this.beanName;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public ApplicationEventPublisher getApplicationEventPublisher() {
    return this.applicationEventPublisher;
  }

  /**
   * Set the error handler to call when the listener throws an exception.
   *
   * @param errorHandler the error handler.
   */
  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }


  /**
   * Get the configured error handler.
   *
   * @return the error handler.
   */
  protected ErrorHandler getErrorHandler() {
    return this.errorHandler;
  }

  @Override
  public boolean isAutoStartup() {
    return this.autoStartup;
  }

  @Override
  public void setAutoStartup(boolean autoStartup) {
    this.autoStartup = autoStartup;
  }

  protected void setRunning(boolean running) {
    this.running = running;
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  public void setPhase(int phase) {
    this.phase = phase;
  }

  @Override
  public int getPhase() {
    return this.phase;
  }


  @Override
  public ConsumerConfigProperties getContainerProperties() {
    return this.containerProperties;
  }

  @Override
  public void setupMessageListener(Object messageListener) {
    this.containerProperties.setMessageListener(messageListener);
  }

  @Override
  public final void start() {
    synchronized (lifecycleMonitor) {
      if (!isRunning()) {
        Assert.isTrue(containerProperties.getMessageListener() instanceof GenericMessageListener,
                      "A " + GenericMessageListener.class.getName()
                      + " implementation must be provided");
        doStart();
      }
    }
  }

  @Override
  public final void stop() {
    synchronized (this.lifecycleMonitor) {
      if (isRunning()) {
        final CountDownLatch latch = new CountDownLatch(1);
        doStop(() -> latch.countDown());
        try {
          latch.await(this.containerProperties.getShutdownTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  @Override
  public void stop(Runnable callback) {
    synchronized (this.lifecycleMonitor) {
      if (isRunning()) {
        doStop(callback);
      }
    }
  }

  protected abstract void doStart();

  protected abstract void doStop(Runnable callback);


}
