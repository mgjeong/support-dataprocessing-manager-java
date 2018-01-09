package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
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

  private transient boolean isTerminated = false;

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
    if (isTerminated) {
      return null;
    } else if (connection != null && !connection.isClosed()) {
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
    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

      return mapToTopology(rs);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a collection of topologies
   *
   * @return Collection of topologies
   */
  public Collection<Topology> listTopologies() {
    String sql = "SELECT id, name, config FROM topology";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql);
        ResultSet rs = ps.executeQuery()) {
      Collection<Topology> topologies = new ArrayList<>();
      while (rs.next()) {
        topologies.add(mapToTopology(rs));
      }
      return topologies;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds a topology to database. Topology id is automatically generated.
   *
   * @param topology topology to add
   * @return topology with a new id assigned
   */
  public Topology addTopology(Topology topology) {
    if (topology == null) {
      throw new RuntimeException("Topology is null.");
    }

    String sql = "INSERT INTO topology (name, config) VALUES (?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topology.getName(),
        topology.getConfigStr())) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating topology failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          commit();
          topology.setId(generatedKeys.getLong(1));
          return topology;
        } else {
          throw new RuntimeException("Creating topology failed, no ID obtained.");
        }
      }
    } catch (Exception e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts or replaces existing topology
   *
   * @param topologyId topology id to update
   * @param topology topology to update
   * @return updated topology
   */
  public Topology updateTopology(Long topologyId, Topology topology) {
    if (topologyId == null || topology == null) {
      throw new RuntimeException("Topology or its id is null.");
    }

    String sql = "INSERT OR REPLACE INTO topology (id, name, config) VALUES(?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        topology.getName(), topology.getConfigStr())) {

      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating topology failed, no rows affected.");
      } else {
        commit();
        topology.setId(topologyId);
        return topology;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes a topology using id
   *
   * @param topologyId topology id
   */
  public void removeTopology(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    String sql = "DELETE FROM topology WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a topology editor metadata of a topology
   *
   * @param topologyId Topology id
   * @return Editor metadata
   */
  public TopologyEditorMetadata getTopologyEditorMetadata(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    String sql = "SELECT topologyId, data FROM topology_editor_metadata WHERE topologyId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {

      if (!rs.next()) {
        throw new RuntimeException(
            "Topology editor metadata for " + topologyId + " does not exist.");
      } else {
        TopologyEditorMetadata metadata = new TopologyEditorMetadata();
        metadata.setTopologyId(rs.getLong("topologyId"));
        metadata.setData(rs.getString("data"));
        // TODO: Hard-coded
        metadata.setTimestamp(System.currentTimeMillis());
        metadata.setVersionId(1L);
        return metadata;
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts a new topology editor metadata
   *
   * @param editorMetadata new topology editor metadata
   * @return inserted topology editor metadata
   */
  public TopologyEditorMetadata addTopologyEditorMetadata(TopologyEditorMetadata editorMetadata) {
    if (editorMetadata == null) {
      throw new RuntimeException("Topology editor metadata is null.");
    }

    String sql = "INSERT INTO topology_editor_metadata (topologyId, data) VALUES (?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        editorMetadata.getTopologyId(), editorMetadata.getData())) {
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating topology editor metadata failed, no rows affected.");
      } else {
        commit();
        return editorMetadata;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts or replaces existing topology editor metadata
   *
   * @param editorMetadata updated editor metadata
   * @return updated editor metadata
   */
  public TopologyEditorMetadata addOrUpdateTopologyEditorMetadata(
      TopologyEditorMetadata editorMetadata) {
    if (editorMetadata == null) {
      throw new RuntimeException("Topology editor metadata is null.");
    }

    String sql = "INSERT OR REPLACE INTO topology_editor_metadata (topologyId, data) VALUES(?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        editorMetadata.getTopologyId(), editorMetadata.getData())) {
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating topology editor metadata failed, no rows affected.");
      } else {
        commit();
        return editorMetadata;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes topology editor metadata for a topology
   *
   * @param topologyId topology id
   */
  public void removeTopologyEditorMetadata(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    String sql = "DELETE FROM topology_editor_metadata WHERE topologyId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  public Collection<TopologyComponentBundle> listTopologyComponentBundles() {
    throw new UnsupportedOperationException();
  }

  private void commit() {
    try {
      getConnection().commit();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private void rollback() {
    try {
      getConnection().rollback();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private Topology mapToTopology(ResultSet rs) throws SQLException {
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
  }

  public void terminate() {
    try {
      this.isTerminated = true;
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}
