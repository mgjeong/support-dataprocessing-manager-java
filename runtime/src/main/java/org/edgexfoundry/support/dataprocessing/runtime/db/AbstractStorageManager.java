package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public abstract class AbstractStorageManager {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageManager.class);

  private Database database;
  private transient boolean isTerminated = false;

  public AbstractStorageManager(String jdbcUrl, String jdbcClass) {
    this.database = Database.getInstance();
    initialize(jdbcUrl, jdbcClass);
  }

  public void initialize(String jdbcUrl, String jdbcClass) {
    this.database.initialize(jdbcUrl, jdbcClass);
  }

  protected synchronized Connection getConnection() throws SQLException {
    if (isTerminated) {
      return null;
    }

    return this.database.getConnection();
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

  public synchronized void commit() {
    this.database.commit();
  }

  public synchronized void rollback() {
    this.database.rollback();
  }


  protected PreparedStatement createPreparedStatement(Connection con, String sql, Object... params)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    for (int i = 0; i < params.length; i++) {
      ps.setObject(i + 1, params[i]);
    }
    return ps;
  }

  public synchronized void terminate() {
    if (isTerminated) {
      return;
    }

    isTerminated = true;
    if (database != null) {
      //connection.rollback();
      database.commit();
      database.close();
    }
  }

}
