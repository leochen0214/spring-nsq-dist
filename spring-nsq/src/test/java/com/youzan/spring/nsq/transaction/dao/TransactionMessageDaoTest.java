package com.youzan.spring.nsq.transaction.dao;

import com.youzan.spring.nsq.transaction.builder.TransactionMessageBuilder;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * @author: clong
 * @date: 2018-12-01
 */
public class TransactionMessageDaoTest {

  private EmbeddedDatabase db;
  private JdbcTemplate jdbcTemplate;
  private String table = "nsq_transaction_message";

  private final ClassRelativeResourceLoader resourceLoader =
      new ClassRelativeResourceLoader(getClass());
  private ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

  // class to test
  private DefaultTransactionMessageDao transactionMessageDao;

  @BeforeEach
  public void setup() {
    db = new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(H2)
        .setScriptEncoding("UTF-8")
        .ignoreFailedDrops(true)
        .addScript("schema.sql")
        .build();

    jdbcTemplate = new JdbcTemplate(db);
    transactionMessageDao = new DefaultTransactionMessageDao(jdbcTemplate);
  }


  @Test
  public void test_insert() {
    TransactionMessage message = TransactionMessageBuilder.builder()
        .businessKey("123")
        .eventType("event")
        .env("dev")
        .payload("hello")
        .state(0)
        .topic("topic1")
        .shardingId(0)
        .build();

    int affectedRows = transactionMessageDao.insert(message);

    assertThat(affectedRows).isEqualTo(1);
    assertThat(message.getId()).isGreaterThan(0);
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, table)).isEqualTo(1);
  }


  @Test
  public void test_updateState() {
    long id = 1L;
    populateData("/db-test-data.sql");
    int state = MessageStateEnum.PUBLISHED.getCode();

    int affectedRows = transactionMessageDao.updateState(id, 0, state);

    assertThat(affectedRows).isEqualTo(1);
    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, table, "state=1")).isEqualTo(1);
  }

  @Test
  public void test_querySingleMessage() {
    String event = "event";
    String businessKey = "123";
    int shardingId = 0;
    populateData("/db-test-data.sql");

    TransactionMessage result =
        transactionMessageDao.querySingleMessage(businessKey, event, shardingId);

    assertTransactionMessage(result);
  }

  @Test
  public void test_querySingleMessageOfNonSharding() {
    String event = "event";
    String businessKey = "123";
    populateData("/db-test-data.sql");

    TransactionMessage result =
        transactionMessageDao.querySingleMessageOfNonSharding(businessKey, event);

    assertTransactionMessage(result);
  }


  @Test
  public void test_queryPublishFailedMessages_withEnvAndShardingIdCondition() {
    populateData("/db-test-data.sql");

    Date oneDayAgo = Date.from(ZonedDateTime.now().minusDays(1).toInstant());

    List<TransactionMessage> result =
        transactionMessageDao.queryPublishFailedMessages(oneDayAgo, 10, "dev", 0);

    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);
  }

  @Test
  public void test_queryPublishFailedMessages_onlyWithShardingIdCondition() {
    populateData("/db-test-data2.sql");

    Date oneDayAgo = Date.from(ZonedDateTime.now().minusDays(1).toInstant());

    List<TransactionMessage> result =
        transactionMessageDao.queryPublishFailedMessages(oneDayAgo, 10, 0);

    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void test_batchDelete_withShardingIdCondition() {
    populateData("/db-test-data3.sql");

    int rows = transactionMessageDao.batchDelete(new Date(), 10, 1);

    assertThat(rows).isEqualTo(1);
    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, table, "sharding_id=0"))
        .isEqualTo(1);
  }

  @Test
  public void test_batchDeleteOfNonSharding() {
    populateData("/db-test-data3.sql");

    int rows = transactionMessageDao.batchDeleteOfNonSharding(new Date(), 10);

    assertThat(rows).isEqualTo(1);
    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, table, "sharding_id=1"))
        .isEqualTo(1);
  }


  @AfterEach
  public void tearDown() {
    db.shutdown();
  }

  private void populateData(String path) {
    databasePopulator.addScript(resource(path));
    databasePopulator.setIgnoreFailedDrops(true);
    DatabasePopulatorUtils.execute(databasePopulator, db);
  }

  private Resource resource(String path) {
    return resourceLoader.getResource(path);
  }

  private void assertTransactionMessage(TransactionMessage result) {
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getCreatedAt()).isBeforeOrEqualsTo(new Date());
    assertThat(result.getUpdatedAt()).isBeforeOrEqualsTo(new Date());
    assertThat(result.getShardingId()).isEqualTo(0);
    assertThat(result.getBusinessKey()).isEqualTo("123");
    assertThat(result.getEventType()).isEqualTo("event");
    assertThat(result.getState()).isEqualTo(MessageStateEnum.CREATED.getCode());
    assertThat(result.getEnv()).isEqualTo("dev");
    assertThat(result.getPayload()).isEqualTo("payload");
    assertThat(result.getTopic()).isEqualTo("topic1");
  }


}
