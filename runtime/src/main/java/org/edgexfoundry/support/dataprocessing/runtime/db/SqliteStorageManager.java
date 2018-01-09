package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class SqliteStorageManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqliteStorageManager.class);

  @Autowired
  private ResourceLoader resourceLoader;

  public SqliteStorageManager() {
    if (resourceLoader == null) {
      resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
    }
  }

  private Connection getConnection() throws SQLException {
    String url = "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;

    try {
      Class.forName("org.sqlite.JDBC");

      Connection conn = DriverManager.getConnection(url);
      conn.setAutoCommit(false);
      return conn;
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
  }


  /**
   * Creates required table(s) using SQL script file from resource.
   */
  private void createTablesIfNotExist() throws SQLException {
    Resource resource = resourceLoader.getResource("db/sqlite/create_tables.sql");
    try (Connection connection = getConnection()) {
      ScriptUtils.executeSqlScript(connection, resource);
      connection.commit();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void main(String[] args) throws SQLException {
    new SqliteStorageManager().createTablesIfNotExist();
  }

}
