package com.youzan.spring.nsq.transaction.dao;

import com.youzan.platform.service_chain.context.ServiceChainContext;
import com.youzan.spring.nsq.transaction.domain.MessageStateEnum;
import com.youzan.spring.nsq.transaction.domain.TransactionMessage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: clong
 * @date: 2018-09-11
 */
@Slf4j
public class DefaultTransactionMessageDao implements TransactionMessageDao {

  private static final String DEFAULT_TABLE_NAME = "nsq_transaction_message";

  private static final String ZAN_TEST_RDS_TAG = "/*!ctx:shadow*/";

  private static final int MAX_DELETE_SIZE = 500;

  private static final String ALL_COLUMNS =
      "id,created_at,updated_at,sharding_id,business_key,event_type,state,env,payload,topic";

  private static final String INSERT_SQL_FORMAT =
      "insert into %s (" + ALL_COLUMNS + ") values (null, now(), now(), ?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE_STATE_SQL_FORMAT =
      "update %s set updated_at = now(), state=? where id=? and sharding_id=?";

  private static final String SELECT_ALL = "select " + ALL_COLUMNS + " from %s ";

  private static final String QUERY_SINGLE_MESSAGE_SQL_FORMAT =
      SELECT_ALL + "where business_key=? and event_type=? and sharding_id=?";

  private static final String BASE_QUERY_MESSAGES =
      SELECT_ALL + "where state=? and sharding_id=? and created_at>=? and created_at <=?";
  private static final String ORDER_BY_CREATED_AT_LIMIT = " order by created_at limit ?";

  private static final String QUERY_MESSAGES_SQL_FORMAT =
      BASE_QUERY_MESSAGES + ORDER_BY_CREATED_AT_LIMIT;

  private static final String QUERY_MESSAGES_WITH_ENV_SQL_FORMAT =
      BASE_QUERY_MESSAGES + " and env=?" + ORDER_BY_CREATED_AT_LIMIT;

  private static final String BATCH_DELETE_SQL_FORMAT =
      "delete from %s where state=1 and sharding_id=? and created_at <= ? limit ?";

  private final JdbcTemplate jdbcTemplate;

  /**
   * local transactional message table name
   */
  private String tableName = DEFAULT_TABLE_NAME;

  public DefaultTransactionMessageDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void setTableName(String tableName) {
    if (StringUtils.hasText(tableName)) {
      this.tableName = tableName;
    }
  }

  @Override
  public int insert(TransactionMessage message) {
    String sql = addHeaderIfIsPressureTest(getInsertSql());

    KeyHolder keyHolder = new GeneratedKeyHolder();

    int affectRows = jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      ps.setInt(1, message.getShardingId());
      ps.setString(2, message.getBusinessKey());
      ps.setString(3, message.getEventType());
      ps.setInt(4, message.getState());
      ps.setString(5, message.getEnv());
      ps.setString(6, message.getPayload());
      ps.setString(7, message.getTopic());

      return ps;
    }, keyHolder);

    Number id = keyHolder.getKey();
    if (id != null) {
      message.setId(id.longValue());
    }
    return affectRows;
  }

  @Override
  public int updateState(Long id, int shardingId, int toState) {
    String sql = addHeaderIfIsPressureTest(getUpdateStateSql());

    return jdbcTemplate.update(sql, ps -> {
      ps.setInt(1, toState);
      ps.setLong(2, id);
      ps.setInt(3, shardingId);
    });
  }

  @Override
  public TransactionMessage querySingleMessage(String businessKey, String eventType,
                                               int shardingId) {
    String sql = addHeaderIfIsPressureTest(getQuerySingleMessageSql());

    List<TransactionMessage> list =
        jdbcTemplate.query(sql, getRowMapper(), businessKey, eventType, shardingId);

    if (list.isEmpty()) {
      return null;
    }

    if (list.size() > 1) {
      log.warn(
          "querySingleMessage occur more than one record, businessKey={}, eventType={}, shardingId={}",
          businessKey, eventType, shardingId);
    }

    return list.get(0);
  }

  @Override
  public List<TransactionMessage> queryPublishFailedMessages(
      Date from, Date to, int fetchSize, String env, int shardingId) {
    int state = MessageStateEnum.CREATED.getCode();
    String sqlFormat;
    Object[] args;
    if (StringUtils.hasText(env)) {
      sqlFormat = QUERY_MESSAGES_WITH_ENV_SQL_FORMAT;
      args = new Object[]{state, shardingId, from, to, env, fetchSize};
    } else {
      sqlFormat = QUERY_MESSAGES_SQL_FORMAT;
      args = new Object[]{state, shardingId, from, to, fetchSize};
    }

    String sql = addHeaderIfIsPressureTest(getQueryMessagesSql(sqlFormat));
    return jdbcTemplate.query(sql, getRowMapper(), args);
  }

  @Override
  public int batchDelete(Date untilTo, int fetchSize, int shardingId) {
    int limit = Math.min(fetchSize, MAX_DELETE_SIZE);
    String sql = addHeaderIfIsPressureTest(String.format(BATCH_DELETE_SQL_FORMAT, tableName));
    return jdbcTemplate.update(sql, shardingId, untilTo, limit);
  }


  private String getInsertSql() {
    return format(INSERT_SQL_FORMAT, tableName);
  }

  private String addHeaderIfIsPressureTest(String sql) {
    if (isPressureTest()) {
      return ZAN_TEST_RDS_TAG + sql;
    }
    return sql;
  }

  private boolean isPressureTest() {
    Optional<Boolean> invocationZanTest = ServiceChainContext.isInvocationZanTest();
    return invocationZanTest.isPresent() && invocationZanTest.get();
  }

  private String getUpdateStateSql() {
    return format(UPDATE_STATE_SQL_FORMAT, tableName);
  }

  private String getQuerySingleMessageSql() {
    return format(QUERY_SINGLE_MESSAGE_SQL_FORMAT, tableName);
  }

  private String getQueryMessagesSql(String sqlFormat) {
    return String.format(sqlFormat, tableName, ">=");
  }

  private String format(String format, String parameter) {
    return String.format(format, parameter);
  }

  private RowMapper<TransactionMessage> getRowMapper() {
    return (rs, i) -> {
      TransactionMessage message = new TransactionMessage();
      message.setId(rs.getLong("id"));
      message.setCreatedAt(rs.getTimestamp("created_at"));
      message.setUpdatedAt(rs.getTimestamp("updated_at"));
      message.setShardingId(rs.getInt("sharding_id"));
      message.setBusinessKey(rs.getString("business_key"));
      message.setEventType(rs.getString("event_type"));
      message.setState(rs.getInt("state"));
      message.setEnv(rs.getString("env"));
      message.setPayload(rs.getString("payload"));
      message.setTopic(rs.getString("topic"));

      return message;
    };
  }
}
