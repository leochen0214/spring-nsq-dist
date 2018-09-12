package com.youzan.spring.nsq.transaction.dao;

import com.youzan.spring.nsq.transaction.domain.TransactionalMessage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

/**
 * @author: clong
 * @date: 2018-09-11
 */
public class DefaultTransactionalMessageDao implements TransactionalMessageDao {

  private static final String DEFAULT_TABLE_NAME = "nsq_transactional_message";

  private static final String ALL_COLUMNS =
      "id, created_at, updated_at, sharding_id, business_key, event_type, state, env, payload";

  private static final String INSERT_SQL_FORMAT =
      "insert into %s (" + ALL_COLUMNS + ") values (null, now(), now(), ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE_STATE_SQL_FORMAT =
      "update %s set updated_at = now(), state = ? where id = ? and sharding_id = ?";

  private final JdbcTemplate jdbcTemplate;

  /**
   * local transactional message table name
   */
  private String tableName = DEFAULT_TABLE_NAME;

  public DefaultTransactionalMessageDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public int insert(TransactionalMessage message) {
    String sql = getInsertSql();
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int affectRows = jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      ps.setInt(1, message.getShardingId());
      ps.setString(2, message.getBusinessKey());
      ps.setString(3, message.getEventType());
      ps.setInt(4, message.getState());
      ps.setString(5, message.getEnv());
      ps.setString(6, message.getPayload());

      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key != null) {
      message.setId(key.longValue());
    }

    return affectRows;
  }

  @Override
  public int updateState(Long id, int shardingId, int toState) {
    String sql = getUpdateStateSql();

    return jdbcTemplate.update(sql, ps -> {
      ps.setInt(1, toState);
      ps.setLong(2, id);
      ps.setInt(3, shardingId);
    });
  }


  private String getInsertSql() {
    return format(INSERT_SQL_FORMAT, tableName);
  }

  private String getUpdateStateSql() {
    return format(UPDATE_STATE_SQL_FORMAT, tableName);
  }

  private String format(String format, String parameter) {
    return String.format(format, parameter);
  }
}
