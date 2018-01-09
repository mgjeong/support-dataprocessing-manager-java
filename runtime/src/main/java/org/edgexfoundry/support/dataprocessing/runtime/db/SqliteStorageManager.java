package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class SqliteStorageManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqliteStorageManager.class);

  private ResourceLoader resourceLoader;
  private Connection connection;

  public SqliteStorageManager() {
  }

  public void initialize() throws Exception {
    try {
      resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
      getConnection();
      createTablesIfNotExist();
    } catch (Exception e) {
      throw e;
    }
  }

  private Connection getConnection() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      return connection; // valid connection already exists
    }

    String url = "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;
    try {
      Class.forName("org.sqlite.JDBC");
      this.connection = DriverManager.getConnection(url);
      this.connection.setAutoCommit(false);
      LOGGER.info("New connection is created.");
      return this.connection;
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
  }

  /**
   * Creates required table(s) using SQL script file from resource.
   */
  private void createTablesIfNotExist() throws SQLException {
    Resource resource = resourceLoader.getResource("db/sqlite/create_tables.sql");

    Connection connection = getConnection();
    try {
      ScriptUtils.executeSqlScript(connection, resource);
      connection.commit();
    } catch (SQLException e) {
      throw e;
    }
  }

  private PreparedStatement createPreparedStatement(Connection con, String sql, Object... params)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(sql);
    for (int i = 0; i < params.length; i++) {
      ps.setObject(i + 1, params[i]);
    }
    return ps;
  }

  /**
   * Returns a topology by id
   *
   * @param topologyId id of a topology to find
   * @return {@link Topology} if found, otherwise null.
   */
  public Topology getTopology(Long topologyId) {
    if (topologyId == null) {
      return null;
    }

    String sql = "SELECT id, name, config FROM topology WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {

      if (!rs.next()) {
        return null; // topology not found
      }

      Topology topology = new Topology();
      topology.setId(rs.getLong("id"));
      topology.setName(rs.getString("name"));
      topology.setConfigStr(rs.getString("config"));

      //TODO: hard-coded. Delete them or use them
      topology.setNamespaceId(1L);
      topology.setVersionId(1L);
      topology.setTimestamp(System.currentTimeMillis());
      topology.setDescription("");

      return topology;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
