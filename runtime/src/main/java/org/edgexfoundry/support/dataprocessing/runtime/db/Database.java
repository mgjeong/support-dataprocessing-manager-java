package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Database {

  private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

  private static Database instance = null;

  public synchronized static Database getInstance() {
    if (instance == null) {
      instance = new Database();
    }

    return instance;
  }

  private Connection connection = null;
  private int transactionCounter = 0;
  private String jdbcUrl = null;
  private String jdbcClass = null;

  private Database() {

  }

  public synchronized void initialize(String jdbcUrl, String jdbcClass) {
    this.jdbcUrl = jdbcUrl;
    this.jdbcClass = jdbcClass;
  }

  private String getJdbcUrl() {
    return this.jdbcUrl;
  }

  private String getJdbcClass() {
    return this.jdbcClass;
  }

  public synchronized Connection getTransaction() throws SQLException {
    // increment transaction counter
    transactionCounter++;
    connection = getConnection();
    connection.setAutoCommit(false);
    return connection;
  }

  public synchronized Connection getConnection() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      return connection; // valid connection already exists
    }

    try {
      Class.forName(getJdbcClass());
      connection = DriverManager.getConnection(getJdbcUrl());
      return connection;
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
  }

  public synchronized void commit() {
    try {
      if (transactionCounter > 0) {
        transactionCounter--;
      }

      if (transactionCounter == 0 && connection != null && !connection.isClosed()) {
        connection.commit();
        connection.setAutoCommit(true);
      }

    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public synchronized void rollback() {
    try {
      if (transactionCounter > 0) {
        transactionCounter--;
      }

      if (transactionCounter == 0 && connection != null && !connection.isClosed()) {
        connection.rollback();
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  public synchronized void close() {
    try {
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}

