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
  protected static final Object writeLock = new Object();

  private static Connection connection;
  private static transient boolean isTerminated = false;
  private static int transactionCounter = 0;

  private static String getJdbcUrl() {
    return "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;
  }

  private static String getJdbcClass() {
    return "org.sqlite.JDBC";
  }

  protected synchronized static Connection getConnection() throws SQLException {
    if (isTerminated) {
      return null;
    }

    // increment transaction counter
    transactionCounter++;

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

    try {
      ScriptUtils.executeSqlScript(getConnection(), resource);
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  protected synchronized static void commit() {
    try {
      if (transactionCounter > 0) {
        transactionCounter--;
      }

      if (transactionCounter == 0 && connection != null && !connection.isClosed()) {
        connection.commit();
      }

    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  protected synchronized static void rollback() {
    try {
      if (transactionCounter > 0) {
        transactionCounter--;
      }

      if (transactionCounter == 0 && connection != null && !connection.isClosed()) {
        connection.rollback();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
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
        //connection.rollback();
        connection.commit();
        connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

}
