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

  private SQLiteDatabase database;

  public AbstractStorageManager(String jdbcUrl) {
    this.database = DatabaseManager.getInstance().getDatabase(jdbcUrl);
  }

  protected synchronized Connection getConnection() {
    return this.database.getConnection();
  }

  public void executeSqlScript(Resource resource) {
    if (resource == null) {
      throw new RuntimeException("Resource is null.");
    }

    try (Connection conn = getConnection()) {
      boolean oldState = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try {
        ScriptUtils.executeSqlScript(conn, resource);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(oldState);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected PreparedStatement createPreparedStatement(Connection con, String sql, Object... params)
      throws SQLException {
    try {
      PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);
      }
      return ps;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }
}
