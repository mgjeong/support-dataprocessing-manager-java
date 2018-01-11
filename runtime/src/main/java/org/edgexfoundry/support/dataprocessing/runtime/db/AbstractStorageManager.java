package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public abstract class AbstractStorageManager {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageManager.class);
  protected static final Object writeLock = new Object();
  protected static final ReentrantLock connectionLock = new ReentrantLock(false);

  private static Connection connection;
  private static transient boolean isTerminated = false;

  /**
   * Used to control transaction.
   *
   * Commits/rollbacks only if transaction number match.
   */
  private static int tNumber = -1;

  private static String getJdbcUrl() {
    return "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;
  }

  private static String getJdbcClass() {
    return "org.sqlite.JDBC";
  }

  protected synchronized static Connection getConnection() throws SQLException {
    return getConnection(-1);
  }

  protected synchronized static Connection getConnection(int transactionNumber)
      throws SQLException {
    if (isTerminated) {
      return null;
    }

    if (tNumber == -1) {
      tNumber = transactionNumber;
    }

    if (connection != null && !connection.isClosed()) {
      return connection; // valid connection already exists
    }

    try {
      Class.forName(getJdbcClass());
      connection = DriverManager.getConnection(getJdbcUrl());
      connection.setAutoCommit(false);
      return connection;
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

  protected static void commit() {
    commit(-1);
  }

  protected static void commit(int transactionNumber) {
    try {
      if (tNumber == transactionNumber) {
        getConnection().commit();
        tNumber = -1;
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      tNumber = -1;
    }
  }

  protected static void rollback() {
    rollback(-1);
  }

  protected static void rollback(int transactionNumber) {
    try {
      if (tNumber == transactionNumber) {
        getConnection().rollback();
        tNumber = -1;
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      tNumber = -1;
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

  public static void terminate() {
    try {
      if (isTerminated) {
        return;
      }

      isTerminated = true;
      if (connection != null && !connection.isClosed()) {
        connection.rollback();
        connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

}
