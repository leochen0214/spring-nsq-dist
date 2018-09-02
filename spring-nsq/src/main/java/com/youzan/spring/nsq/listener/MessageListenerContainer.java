/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.youzan.spring.nsq.listener;

import com.youzan.spring.nsq.properties.ConsumerConfigProperties;

import org.springframework.context.SmartLifecycle;

/**
 * Internal abstraction used by the framework representing a message listener container. Not meant
 * to be implemented externally.
 *
 * @author Stephane Nicoll
 * @author Gary Russell
 * @author Vladimir Tsanev
 */
public interface MessageListenerContainer extends SmartLifecycle {

  /**
   * Setup the message listener to use. Throws an {@link IllegalArgumentException} if that message
   * listener type is not supported.
   *
   * @param messageListener the {@code object} to wrapped to the {@code MessageListener}.
   */
  void setupMessageListener(Object messageListener);


  /**
   * Return the container properties for this container.
   *
   * @return the properties.
   * @since 2.1.3
   */
  default ConsumerConfigProperties getContainerProperties() {
    throw new UnsupportedOperationException(
        "This container doesn't support retrieving its properties");
  }


  /**
   * Set the autoStartup.
   *
   * @param autoStartup the autoStartup to set.
   * @see SmartLifecycle
   * @since 2.2
   */
  default void setAutoStartup(boolean autoStartup) {
    // empty
  }

}
