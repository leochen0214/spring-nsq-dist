package com.youzan.spring.nsq.transaction.service;

import com.youzan.nsq.client.entity.Message;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.spring.nsq.core.NsqOperations;
import com.youzan.spring.nsq.transaction.CurrentEnvironment;
import com.youzan.spring.nsq.transaction.builder.TransactionMessageBuilder;
import com.youzan.spring.nsq.transaction.dao.TransactionMessageDao;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;

/**
 * @author: clong
 * @date: 2018-12-01
 */
public class TransactionMessageServiceTest {

  @Mock
  private TransactionMessageDao transactionMessageDao;

  @Mock
  private NsqOperations nsqTemplate;

  @Mock
  private CurrentEnvironment currentEnvironment;


  private DefaultTransactionMessageService transactionMessageService;


  @BeforeEach
  public void setup() {
    MockitoAnnotations.initMocks(this);
    transactionMessageService =
        new DefaultTransactionMessageService(transactionMessageDao, nsqTemplate,
                                             currentEnvironment);
  }


  @Test
  @DisplayName("[失败消息重发]-全部重发成功且消息状态都更新为已发送")
  public void test_publishAgain_allMessagesSendSuccess() {
    List<TransactionMessage> messages = getPublishFailedMessages();
    willDoNothing().given(nsqTemplate).send(any(Message.class));
    given(transactionMessageDao.updateState(anyLong(), anyInt(), anyInt())).willAnswer(
        (Answer<Integer>) invocation -> {
          Integer messageStateCode = invocation.getArgument(2);
          assertThat(messageStateCode).isEqualTo(MessageStateEnum.PUBLISHED.getCode());
          return 1;
        });

    long result = transactionMessageService.publishAgain(messages);

    assertThat(result).isEqualTo(messages.size());
  }


  @Test
  @DisplayName("[失败消息重发]-部分重发成功且这部分消息状态都更新为已发送")
  public void test_publishAgain_someMessageSendFailed() {
    List<TransactionMessage> messages = getPublishFailedMessages();
    mockNsqTemplateSend();

    given(transactionMessageDao.updateState(anyLong(), anyInt(), anyInt())).willAnswer(
        (Answer<Integer>) invocation -> {
          Integer messageStateCode = invocation.getArgument(2);
          assertThat(messageStateCode).isEqualTo(MessageStateEnum.PUBLISHED.getCode());
          return 1;
        });

    long result = transactionMessageService.publishAgain(messages);

    assertThat(result).isEqualTo(2);
  }

  @Test
  @DisplayName("[失败消息重发]-部分重发成功且这部分消息状态只有部分状态更新为已发送")
  public void test_publishAgain_someMessageSendFailedAndSomeMessageStateUpdateFailed() {
    List<TransactionMessage> messages = getPublishFailedMessages();
    mockNsqTemplateSend();

    given(transactionMessageDao.updateState(anyLong(), anyInt(), anyInt())).willAnswer(
        (Answer<Integer>) invocation -> {
          Integer messageStateCode = invocation.getArgument(2);
          Long id = invocation.getArgument(0);
          assertThat(messageStateCode).isEqualTo(MessageStateEnum.PUBLISHED.getCode());
          if (id == 2L) {
            throw new RuntimeException("db occur exception");
          }
          return 1;
        });

    long result = transactionMessageService.publishAgain(messages);

    assertThat(result).isEqualTo(1);
  }

  @Test
  public void test_queryPublishFailedMessages() {
    Date from = Date.from(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant());
    int size = 20;
    int shardingId = 0;
    given(currentEnvironment.currentEnv()).willReturn("dev");
    willAnswer(invocation -> {
      Date dateFromArg = invocation.getArgument(0);
      Date dateToArg = invocation.getArgument(1);
      int sizeArg = invocation.getArgument(2);
      String envArg = invocation.getArgument(3);
      int shardingIdArg = invocation.getArgument(4);
      Date to = Date.from(
          ZonedDateTime.now().minusSeconds(transactionMessageService.getSeconds()).toInstant());
      assertThat(dateFromArg).isEqualTo(from);
      assertThat(dateToArg).isBeforeOrEqualsTo(to);
      assertThat(sizeArg).isEqualTo(size);
      assertThat(envArg).isEqualTo("dev");
      assertThat(shardingIdArg).isEqualTo(shardingId);

      return getPublishFailedMessages();
    }).given(transactionMessageDao).queryPublishFailedMessages(any(Date.class),
                                                               any(Date.class),
                                                               anyInt(),
                                                               anyString(),
                                                               anyInt());
    List<TransactionMessage> result =
        transactionMessageService.queryPublishFailedMessages(from, shardingId, size);

    assertThat(result.size()).isEqualTo(3);
  }

  private void mockNsqTemplateSend() {
    willAnswer(invocation -> {
      Message nsqMessage = invocation.getArgument(0);
      if ("t1".equals(nsqMessage.getTopic().getTopicText())) {
        throw new NSQException("nsq client exception");
      }
      return null;
    }).given(nsqTemplate).send(any(Message.class));
  }

  private List<TransactionMessage> getPublishFailedMessages() {
    List<TransactionMessage> list = new ArrayList<>();

    TransactionMessage m1 = TransactionMessageBuilder.builder()
        .id(1L)
        .createdAt(yesterday())
        .businessKey("1")
        .eventType("event")
        .shardingId(0)
        .state(MessageStateEnum.CREATED.getCode())
        .topic("t1")
        .payload("p1")
        .env("dev").build();

    TransactionMessage m2 = TransactionMessageBuilder.builder()
        .id(2L)
        .createdAt(yesterday())
        .businessKey("2")
        .eventType("event")
        .shardingId(0)
        .state(MessageStateEnum.CREATED.getCode())
        .topic("t2")
        .payload("p2")
        .env("dev").build();

    TransactionMessage m3 = TransactionMessageBuilder.builder()
        .id(3L)
        .createdAt(yesterday())
        .businessKey("3")
        .eventType("event")
        .shardingId(0)
        .state(MessageStateEnum.CREATED.getCode())
        .topic("t3")
        .payload("p3")
        .env("dev").build();

    list.add(m1);
    list.add(m2);
    list.add(m3);

    return list;
  }

  private Date yesterday() {
    return Date.from(ZonedDateTime.now().minusDays(1).toInstant());
  }


}
