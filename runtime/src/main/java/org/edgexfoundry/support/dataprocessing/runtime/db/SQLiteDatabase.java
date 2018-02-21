package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteDataSource;

public final class SQLiteDatabase {

  private SQLiteDataSource dataSource;

  public synchronized void initialize(String host) {
    SQLiteConfig config = new SQLiteConfig();
    config.setJournalMode(JournalMode.WAL);
    // Set config if necessary
    dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(host);
  }

  public synchronized Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new SQLException(SQLiteDatabase.class.getSimpleName() + " is not initialized.");
    }

    try {
      Connection connection = dataSource.getConnection();
      if (connection == null) {
        throw new SQLException("Failed to retrieve connection from data source.");
      }
      return connection;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }
}

