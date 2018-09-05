package com.youzan.example;
// tag::configuration[]
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.spring.nsq.exception.ListenerExecutionFailedException;
import com.youzan.spring.nsq.handler.NsqListenerErrorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author: clong
 * @date: 2018-09-04
 */
@Component
public class PersonListenerErrorHandler implements NsqListenerErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(PersonListenerErrorHandler.class);

  @Override
  public void handleError(NSQMessage message, ListenerExecutionFailedException exception)
      throws Exception {
    logger.error("oops, message={}", message, exception);
  }
}
// end::configuration[]
