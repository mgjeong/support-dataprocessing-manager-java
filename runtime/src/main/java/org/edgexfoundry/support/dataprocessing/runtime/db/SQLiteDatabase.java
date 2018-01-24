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

  public synchronized Connection getConnection() {
    if (dataSource == null) {
      throw new RuntimeException(SQLiteDatabase.class.getSimpleName() + " is not initialized.");
    }

    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

