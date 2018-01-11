package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public abstract class AbstractStorageManager {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageManager.class);

  private Connection connection;
  private transient boolean isTerminated = false;

  /**
   * Used to control transaction.
   *
   * Commits/rollbacks only if transaction number match.
   */
  private int transactionNumber = -1;

  private String getJdbcUrl() {
    return "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;
  }

  private String getJdbcClass() {
    return "org.sqlite.JDBC";
  }

  protected synchronized Connection getConnection() throws SQLException {
    return getConnection(-1);
  }

  protected synchronized Connection getConnection(int transactionNumber) throws SQLException {
    if (isTerminated) {
      return null;
    }

    if (this.transactionNumber == -1) {
      this.transactionNumber = transactionNumber;
    }

    if (connection != null && !connection.isClosed()) {
      return connection; // valid connection already exists
    }

    try {
      Class.forName(getJdbcClass());
      this.connection = DriverManager.getConnection(getJdbcUrl());
      this.connection.setAutoCommit(false);
      return this.connection;
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
  }

  public void executeSqlScript(Resource resource) {
    if (resource == null) {
      throw new RuntimeException("Resource is null.");
    }

    int transactionNumber = 5;
    try {
      ScriptUtils.executeSqlScript(getConnection(transactionNumber), resource);
      commit(transactionNumber);
    } catch (SQLException e) {
      rollback(transactionNumber);
      throw new RuntimeException(e);
    }
  }

  protected void commit() {
    commit(-1);
  }

  protected void commit(int transactionNumber) {
    try {
      if (this.transactionNumber == transactionNumber) {
        getConnection().commit();
        this.transactionNumber = -1;
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      this.transactionNumber = -1;
    }
  }

  protected void rollback() {
    rollback(-1);
  }

  protected void rollback(int transactionNumber) {
    try {
      if (this.transactionNumber == transactionNumber) {
        getConnection().rollback();
        this.transactionNumber = -1;
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      this.transactionNumber = -1;
    }
  }

  protected PreparedStatement createPreparedStatement(Connection con, String sql, Object... params)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    for (int i = 0; i < params.length; i++) {
      ps.setObject(i + 1, params[i]);
    }
    return ps;
  }

  public void terminate() {
    try {
      if (this.isTerminated) {
        return;
      }

      this.isTerminated = true;
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

}
