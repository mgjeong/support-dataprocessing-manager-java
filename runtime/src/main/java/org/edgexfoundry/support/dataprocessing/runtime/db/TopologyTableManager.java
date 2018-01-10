package org.edgexfoundry.support.dataprocessing.runtime.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ClusterWithService;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ClusterWithService.ServiceConfiguration;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Namespace;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge.StreamGrouping;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TopologyTableManager extends AbstractStorageManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyTableManager.class);

  private static TopologyTableManager instance = null;

  public synchronized static TopologyTableManager getInstance() {
    if (instance == null) {
      instance = new TopologyTableManager();
    }
    return instance;
  }

  private TopologyTableManager() {
  }

  @Deprecated
  private Namespace mockNamespace() {
    Namespace.Info firstInfo = new Namespace.Info();
    firstInfo.setId(1L);
    firstInfo.setDescription("First namespace");
    firstInfo.setName("Dover");
    firstInfo.setStreamingEngine("STORM");
    firstInfo.setTimeSeriesDB(null);
    firstInfo.setTimestamp(System.currentTimeMillis());

    Namespace.ServiceClusterMap firstMap = new Namespace.ServiceClusterMap();
    firstMap.setClusterId(1L);
    firstMap.setNamespaceId(1L);
    firstMap.setServiceName("STORM");

    // enrich
    Namespace first = new Namespace();
    first.setNamespace(firstInfo);
    first.addMapping(firstMap);
    return first;
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
   * Returns a collection of every topology component bundles
   *
   * @return Topology component bundles
   */
  public Collection<TopologyComponentBundle> listTopologyComponentBundles() {
    Collection<TopologyComponentBundle> bundles = new ArrayList<>();
    String sql = "SELECT * FROM topology_component_bundle";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        bundles.add(mapToTopologyComponentBundle(rs));
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    return bundles;
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
   * Inserts or updates existing topology
   *
   * @param topologyId topology id to update
   * @param topology topology to update
   * @return updated topology
   */
  public Topology addOrUpdateTopology(Long topologyId, Topology topology) {
    if (topology == null || topologyId == null) {
      throw new RuntimeException("Topology or id is null.");
    }
    topology.setId(topologyId);

    String sql = "INSERT OR REPLACE INTO topology (id, name, config) VALUES (?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyId,
        topology.getName(),
        topology.getConfigStr())) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating topology failed, no rows affected.");
      } else {
        topology.setId(topologyId);
        return topology;
      }
    } catch (Exception e) {
      rollback();
      throw new RuntimeException(e);
    }
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
   * @param topologyId topology id
   * @param editorMetadata updated editor metadata
   * @return updated editor metadata
   */
  public TopologyEditorMetadata addOrUpdateTopologyEditorMetadata(
      Long topologyId,
      TopologyEditorMetadata editorMetadata) {
    if (topologyId == null || editorMetadata == null) {
      throw new RuntimeException("Topology id or editor metadata is null.");
    }
    editorMetadata.setTopologyId(topologyId);

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
  public Topology removeTopology(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    Topology topology = getTopology(topologyId);
    if (topology == null) {
      throw new RuntimeException("Topology with id=" + topologyId + " not found.");
    }

    String sql = "DELETE FROM topology WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId)) {
      ps.executeUpdate();
      commit();
      return topology;
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


  /**
   * Returns a list of topology component bundles of a specific type
   *
   * @param type Type of a component bundle to list
   * @return List of component bundles with specified type
   */
  public Collection<TopologyComponentBundle> listTopologyComponentBundles(
      TopologyComponentType type) {
    if (type == null) {
      throw new RuntimeException("Invalid topology component type.");
    }

    Collection<TopologyComponentBundle> bundles = new ArrayList<>();
    String sql = "SELECT * FROM topology_component_bundle WHERE type = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, type.name());
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        bundles.add(mapToTopologyComponentBundle(rs));
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    return bundles;
  }

  /**
   * Inserts a topology component bundle
   *
   * @param bundle bundle to insert
   * @return inserted bundle with id assigned
   */
  public TopologyComponentBundle addTopologyComponentBundle(TopologyComponentBundle bundle) {
    if (bundle == null) {
      throw new RuntimeException("Topology component bundle is null.");
    }

    String sql = "INSERT INTO topology_component_bundle "
        + "(name, type, subType, streamingEngine, path, classname, param, componentUISpecification, removable) "
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        bundle.getName(), bundle.getType().name(), bundle.getSubType(), bundle.getStreamingEngine(),
        bundle.getBundleJar(), bundle.getFieldHintProviderClass(),
        bundle.getTopologyComponentUISpecification().toString(),
        bundle.getTopologyComponentUISpecification().toString(),
        bundle.isBuiltin() ? "0" : "1")) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating bundle failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          bundle.setId(generatedKeys.getLong(1));
          commit();
          return bundle;
        } else {
          throw new RuntimeException("Creating bundle failed, no ID obtained.");
        }
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts or replaces topology component bundle
   *
   * @param bundle bundle to update
   * @return updated bundle
   */
  public TopologyComponentBundle addOrUpdateTopologyComponentBundle(
      TopologyComponentBundle bundle) {
    if (bundle == null) {
      throw new RuntimeException("Topology component bundle is null.");
    }

    String sql = "INSERT OR REPLACE INTO topology_component_bundle "
        + "(id, name, type, subType, streamingEngine, path, classname, param, componentUISpecification, removable) "
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        bundle.getId(),
        bundle.getName(), bundle.getType().name(), bundle.getSubType(), bundle.getStreamingEngine(),
        bundle.getBundleJar(), bundle.getFieldHintProviderClass(),
        bundle.getTopologyComponentUISpecification().toString(),
        bundle.getTopologyComponentUISpecification().toString(),
        bundle.isBuiltin() ? '0' : '1')) {

      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating bundle failed, no rows affected.");
      } else {
        commit();
        return bundle;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  public TopologyComponentBundle getTopologyComponentBundle(String componentName,
      TopologyComponentType componentType,
      String componentSubType) {
    if (componentName == null || componentType == null || componentSubType == null) {
      throw new RuntimeException("Component name, type or subtype is null.");
    }

    String sql = "SELECT * FROM topology_component_bundle WHERE " +
        "name = ? AND type = ? AND subType = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, componentName,
        componentType.name(), componentSubType);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      } else {
        return mapToTopologyComponentBundle(rs);
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes a topology component bundle by id
   *
   * @param topologyComponentBundleId bundle id to remove
   */
  public void removeTopologyComponentBundle(Long topologyComponentBundleId) {
    if (topologyComponentBundleId == null) {
      throw new RuntimeException("Topology component bundle id is null.");
    }

    String sql = "DELETE FROM topology_component_bundle WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyComponentBundleId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of topology streams for a given topology id
   *
   * @param topologyId topology id
   * @return Topology streams
   */
  public Collection<TopologyStream> listTopologyStreams(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    Collection<TopologyStream> streams = new ArrayList<>();
    String sql = "SELECT * FROM topology_stream WHERE topologyId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        streams.add(mapToTopologyStream(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return streams;
  }

  public TopologyStream getTopologyStream(Long topologyId, Long streamId) {
    if (topologyId == null || streamId == null) {
      throw new RuntimeException("Topology id or stream id is null.");
    }

    String sql = "SELECT * FROM topology_stream WHERE topologyId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId, streamId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        throw new RuntimeException("Topology stream does not exist.");
      } else {
        return mapToTopologyStream(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts a new topology stream
   *
   * @param topologyStream topology stream
   * @return topology stream with updated id
   */
  public TopologyStream addTopologyStream(TopologyStream topologyStream) {
    if (topologyStream == null) {
      throw new RuntimeException("TopologyStream is null.");
    }

    String sql = "INSERT INTO topology_stream (topologyId, componentId, streamName, fields) VALUES"
        + "(?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyStream.getTopologyId(), topologyStream.getComponentId(),
        topologyStream.getStreamId(), topologyStream.getFieldsStr())) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating stream failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          commit();
          topologyStream.setId(generatedKeys.getLong(1));
          return topologyStream;
        } else {
          throw new RuntimeException("Creating stream failed, no ID obtained.");
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts or replaces existing topology stream
   *
   * @param topologyStreamId topology stream id to update
   * @param stream topology stream to update
   * @return updated topology stream
   */
  public TopologyStream addOrUpdateTopologyStream(Long topologyStreamId, TopologyStream stream) {
    if (stream == null) {
      throw new RuntimeException("Topology stream is null.");
    }
    stream.setId(topologyStreamId);

    // Add if stream id does not exist
    if (topologyStreamId == null) {
      stream = addTopologyStream(stream);
      return stream;
    }

    // Otherwise, update
    stream.setId(topologyStreamId);
    String sql =
        "INSERT OR REPLACE INTO topology_stream (id, topologyId, componentId, streamName, fields) VALUES"
            + "(?,?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyStreamId,
        stream.getTopologyId(), stream.getComponentId(),
        stream.getStreamId(), stream.getFieldsStr())) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating stream failed, no rows affected.");
      } else {
        commit();
        return stream;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes a topology stream using topology and component ids
   *
   * @param topologyId topology id
   * @param streamId stream id
   * @return removed topology stream
   */
  public TopologyStream removeTopologyStream(Long topologyId, Long streamId) {
    if (topologyId == null || streamId == null) {
      throw new RuntimeException("Topology id or component id is null.");
    }

    TopologyStream stream = getTopologyStream(topologyId, streamId);
    if (stream == null) {
      throw new RuntimeException("Topology stream does not exist.");
    }

    String sql = "DELETE FROM topology_stream WHERE topologyId = ? AND componentId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        streamId)) {
      ps.executeUpdate();
      commit();
      return stream;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of topology edges for a given topology id
   *
   * @param topologyId topology id
   * @return topology edges
   */
  public Collection<TopologyEdge> listTopologyEdges(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    Collection<TopologyEdge> edges = new ArrayList<>();
    String sql = "SELECT * FROM topology_edge WHERE topologyId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        edges.add(mapToTopologyEdge(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return edges;
  }

  /**
   * Returns a topology edge for given topology id and edge id
   *
   * @param topologyId topology id
   * @param edgeId edge id
   * @return {@link TopologyEdge}
   */
  public TopologyEdge getTopologyEdge(Long topologyId, Long edgeId) {
    if (topologyId == null || edgeId == null) {
      throw new RuntimeException("Topology id or edge id is null.");
    }

    String sql = "SELECT * FROM topology_edge WHERE topology_id = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId, edgeId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        throw new RuntimeException(
            "Edge not found for topologyId:" + topologyId + " / edgeId:" + edgeId);
      } else {
        return mapToTopologyEdge(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Insert a topology edge
   *
   * @param topologyId topology id
   * @param topologyEdge topology edge to add
   * @return updated topology edge
   */
  public TopologyEdge addTopologyEdge(Long topologyId, TopologyEdge topologyEdge) {
    if (topologyId == null || topologyEdge == null) {
      throw new RuntimeException("Topology id or edge is null.");
    }
    topologyEdge.setTopologyId(topologyId);

    String sql = "INSERT INTO topology_edge (topologyId, fromId, toId, streamGroupings) "
        + "VALUES (?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyEdge.getTopologyId(), topologyEdge.getFromId(),
        topologyEdge.getToId(), topologyEdge.getStreamGroupings())) {
      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating edge failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          commit();
          topologyEdge.setId(generatedKeys.getLong(1));
          return topologyEdge;
        } else {
          throw new RuntimeException("Creating edge failed, no ID obtained.");
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts or replaces topology edge for a given topology id, edge id
   *
   * @param topologyId topology id
   * @param topologyEdgeId edge id
   * @param topologyEdge edge
   * @return updated topology edge
   */
  public TopologyEdge addOrUpdateTopologyEdge(Long topologyId, Long topologyEdgeId,
      TopologyEdge topologyEdge) {
    if (topologyId == null || topologyEdgeId == null || topologyEdge == null) {
      throw new RuntimeException("Topology id, edge id or edge is null.");
    }
    topologyEdge.setId(topologyEdgeId);
    topologyEdge.setTopologyId(topologyId);

    String sql =
        "INSERT OR REPLACE INTO topology_edge (id, topologyId, fromId, toId, streamGroupings) "
            + "VALUES (?,?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyEdgeId,
        topologyEdge.getTopologyId(), topologyEdge.getFromId(),
        topologyEdge.getToId(), topologyEdge.getStreamGroupings())) {
      // update
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating edge failed, no rows affected.");
      }
      commit();
      return topologyEdge;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  private void removeTopologyComponentEdges(Long topologyId, Long componentId) {
    if (topologyId == null || componentId == null) {
      throw new RuntimeException("Topology id or component id is null.");
    }

    String sql = "DELETE FROM topology_edge WHERE topologyId = ? AND (fromId = ? OR toId = ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        componentId, componentId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Remove topology edge by topology id and edge id
   *
   * @param topologyId topology id
   * @param edgeId edge id
   */
  public TopologyEdge removeTopologyEdge(Long topologyId, Long edgeId) {
    if (topologyId == null || edgeId == null) {
      throw new RuntimeException("Topology id or edge id is null.");
    }

    TopologyEdge edge = getTopologyEdge(topologyId, edgeId);
    if (edge == null) {
      throw new RuntimeException("Topology edge not found.");
    }

    String sql = "DELETE FROM topology_edge WHERE topologyId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId, edgeId)) {
      ps.executeUpdate();
      commit();
      return edge;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of topology components for given topology id
   *
   * @param topologyId topology id
   * @return A collection of topology components
   */
  public Collection<TopologyComponent> listTopologyComponents(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    // Retrieve streams
    Collection<TopologyStream> streams = listTopologyStreams(topologyId);
    Map<Long, List<TopologyStream>> streamMap = streams.stream()
        .collect(Collectors.groupingBy(TopologyStream::getComponentId));

    Collection<TopologyComponent> components = new ArrayList<>();
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT ")
        .append("topology_component.id AS id,")
        .append("topology_component.topologyId AS topologyId,")
        .append("topology_component.componentBundleId AS componentBundleId,")
        .append("topology_component.name AS name,")
        .append("topology_component.config as config,")
        .append("topology_component_bundle.type AS type ")
        .append("FROM topology_component, topology_component_bundle ")
        .append("WHERE topology_component.topologyId = ? ")
        .append("AND topology_component.componentBundleId = topology_component_bundle.id");
    String sql = sqlBuilder.toString();
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        TopologyComponent component = mapToTopologyComponent(rs);
        if (component == null) {
          continue;
        } else if (component instanceof TopologySource
            && streamMap.get(component.getId()) != null) {
          ((TopologySource) component).setOutputStreams(streamMap.get(component.getId()));
        } else if (component instanceof TopologyProcessor
            && streamMap.get(component.getId()) != null) {
          ((TopologyProcessor) component).setOutputStreams(streamMap.get(component.getId()));
        }
        components.add(component);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return components;
  }

  /**
   * Inserts a new topology component for a given topology id
   *
   * @param topologyId topology id
   * @param topologyComponent topology component to insert
   * @return updated topology component
   */
  public <T extends TopologyComponent> T addTopologyComponent(Long topologyId,
      TopologyComponent topologyComponent) {
    if (topologyId == null || topologyComponent == null) {
      throw new RuntimeException("Either topology id or component is null.");
    }

    String sql = "INSERT INTO topology_component (topologyId, componentBundleId, name, config) "
        + "VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyId, topologyComponent.getTopologyComponentBundleId(),
        topologyComponent.getName(), topologyComponent.getConfigStr())) {

      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating component failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          topologyComponent.setId(generatedKeys.getLong(1));

          // Insert output streams
          List<TopologyStream> streams = null;
          if ((topologyComponent instanceof TopologySource)) {
            streams = ((TopologySource) topologyComponent).getOutputStreams();
          } else if ((topologyComponent instanceof TopologyProcessor)) {
            streams = ((TopologyProcessor) topologyComponent).getOutputStreams();
          }
          if (streams != null) {
            for (TopologyStream stream : streams) {
              stream.setTopologyId(topologyId);
              stream.setComponentId(topologyComponent.getId());
              addTopologyStream(stream);
            }
          }

          commit();
          return (T) topologyComponent;
        } else {
          throw new RuntimeException("Creating component failed, no ID obtained.");
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds or updates existing topology component for a given topology id
   *
   * @param topologyId topology id
   * @param topologyComponentId topology component id to update
   * @param topologyComponent topology component to update
   * @return updated topology component
   */
  public <T extends TopologyComponent> T addOrUpdateTopologyComponent(Long topologyId,
      Long topologyComponentId,
      TopologyComponent topologyComponent) {
    if (topologyId == null || topologyComponentId == null || topologyComponent == null) {
      throw new RuntimeException("Topology id, component id or component is null.");
    }
    topologyComponent.setId(topologyComponentId);
    topologyComponent.setTopologyId(topologyId);

    String sql =
        "INSERT OR REPLACE INTO topology_component (id, topologyId, componentBundleId, name, config) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyComponentId,
        topologyId, topologyComponent.getTopologyComponentBundleId(),
        topologyComponent.getName(), topologyComponent.getConfigStr())) {

      // insert
      int affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating component failed, no rows affected.");
      }

      // Update output streams
      List<TopologyStream> streams = null;
      if ((topologyComponent instanceof TopologySource)) {
        streams = ((TopologySource) topologyComponent).getOutputStreams();
      } else if ((topologyComponent instanceof TopologyProcessor)) {
        streams = ((TopologyProcessor) topologyComponent).getOutputStreams();
      }
      if (streams != null) {
        for (TopologyStream stream : streams) {
          stream.setTopologyId(topologyId);
          stream.setComponentId(topologyComponentId);
          addOrUpdateTopologyStream(stream.getId(), stream);
        }
      }

      commit();
      return (T) topologyComponent;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Remove topology component by topology id and component id
   *
   * @param topologyId topology id
   * @param topologyComponentId component id
   */
  public <T extends TopologyComponent> T removeTopologyComponent(Long topologyId,
      Long topologyComponentId) {
    if (topologyId == null || topologyComponentId == null) {
      throw new RuntimeException("Topology id or component id is null.");
    }

    TopologyComponent component = getTopologyComponent(topologyId, topologyComponentId);
    if (component == null) {
      throw new RuntimeException("Topology component does not exist.");
    }

    // remove edges
    removeTopologyComponentEdges(topologyId, topologyComponentId);

    // remove streams
    removeTopologyComponentStreams(topologyId, topologyComponentId);

    String sql = "DELETE FROM topology_component WHERE topologyId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        topologyComponentId)) {
      ps.executeUpdate();
      commit();
      return (T) component;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  private void removeTopologyComponentStreams(Long topologyId, Long topologyComponentId) {
    if (topologyId == null || topologyComponentId == null) {
      throw new RuntimeException("Topology id or component id is null.");
    }

    String sql = "DELETE FROM topology_stream WHERE topologyId = ? AND componentId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        topologyComponentId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
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

  private TopologyEdge mapToTopologyEdge(ResultSet rs) throws SQLException {
    TopologyEdge edge = new TopologyEdge();
    edge.setId(rs.getLong("id"));
    edge.setTopologyId(rs.getLong("topologyId"));
    edge.setFromId(rs.getLong("fromId"));
    edge.setToId(rs.getLong("toId"));
    edge.setStreamGroupingsStr(rs.getString("streamGroupings"));
    return edge;
  }

  private TopologyComponentBundle mapToTopologyComponentBundle(ResultSet rs)
      throws SQLException, IOException {
    TopologyComponentBundle bundle = new TopologyComponentBundle();
    bundle.setId(rs.getLong("id"));
    bundle.setName(rs.getString("name"));
    bundle.setType(TopologyComponentType.toTopologyComponentType(rs.getString("type")));
    bundle.setSubType(rs.getString("subType"));
    bundle.setStreamingEngine(rs.getString("streamingEngine"));

    String ui = rs.getString("componentUISpecification");
    ComponentUISpecification uiSpecification = ComponentUISpecification
        .create(ui, ComponentUISpecification.class);
    bundle.setTopologyComponentUISpecification(uiSpecification);

    // TODO: is this correct mapping?
    bundle.setBundleJar(rs.getString("path"));
    bundle.setTransformationClass(rs.getString("classname"));
    bundle.setBuiltin(rs.getByte("removable") == (byte) '0');
    // TODO: Hard-coded
    bundle.setMavenDeps("");
    bundle.setFieldHintProviderClass("");
    bundle.setTimestamp(System.currentTimeMillis());
    return bundle;
  }

  private TopologyStream mapToTopologyStream(ResultSet rs) throws SQLException {
    TopologyStream stream = new TopologyStream();
    stream.setId(rs.getLong("id"));
    stream.setTopologyId(rs.getLong("topologyId"));
    stream.setComponentId(rs.getLong("componentId"));
    stream.setStreamId(rs.getString("streamName"));
    stream.setFieldsStr(rs.getString("fields"));
    return stream;
  }

  private TopologyComponent mapToTopologyComponent(ResultSet rs) throws SQLException {
    TopologyComponentType type = TopologyComponentType
        .toTopologyComponentType(rs.getString("type"));
    TopologyComponent component = null;
    switch (type) {
      case SOURCE:
        component = new TopologySource();
        break;
      case PROCESSOR:
        component = new TopologyProcessor();
        break;
      case SINK:
        component = new TopologySink();
        break;
      case LINK:
      case TOPOLOGY:
        break;
    }
    if (component == null) {
      return null;
    }
    component.setId(rs.getLong("id"));
    component.setTopologyId(rs.getLong("topologyId"));
    component.setTopologyComponentBundleId(rs.getLong("componentBundleId"));
    component.setName(rs.getString("name"));
    component.setConfigStr(rs.getString("config"));

    return component;
  }

  @Deprecated
  public Collection<TopologyVersion> listTopologyVersionInfos(Long topologyId) {
    List<TopologyVersion> versions = new ArrayList<>();
    TopologyVersion firstVersion = new TopologyVersion();
    firstVersion.setId(1L);
    firstVersion.setDescription("First version");
    firstVersion.setName("CURRENT");
    firstVersion.setTimestamp(System.currentTimeMillis());
    firstVersion.setTopologyId(topologyId);
    versions.add(firstVersion);

    return Collections.unmodifiableCollection(versions);
  }

  @Deprecated
  public Collection<Namespace> listNamespaces() {
    Collection<Namespace> namespaces = new ArrayList<>();
    namespaces.add(mockNamespace());
    return namespaces;
  }

  public Collection<TopologySource> listSources(Long topologyId) {
    Collection<TopologyComponent> components = listTopologyComponents(topologyId);
    return components.stream().filter(component -> component instanceof TopologySource)
        .map(component -> (TopologySource) component)
        .collect(Collectors.toSet());
  }

  public Collection<TopologySink> listSinks(Long topologyId) {
    Collection<TopologyComponent> components = listTopologyComponents(topologyId);
    return components.stream().filter(component -> component instanceof TopologySink)
        .map(component -> (TopologySink) component)
        .collect(Collectors.toSet());
  }

  public Collection<TopologyProcessor> listProcessors(Long topologyId) {
    Collection<TopologyComponent> components = listTopologyComponents(topologyId);
    return components.stream().filter(component -> component instanceof TopologyProcessor)
        .map(component -> (TopologyProcessor) component)
        .collect(Collectors.toSet());
  }

  public <T extends TopologyComponent> T getTopologyComponent(Long topologyId, Long componentId) {
    if (topologyId == null || componentId == null) {
      throw new RuntimeException("Topology id or component id is null.");
    }

    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT ")
        .append("topology_component.id AS id,")
        .append("topology_component.topologyId AS topologyId,")
        .append("topology_component.componentBundleId AS componentBundleId,")
        .append("topology_component.name AS name,")
        .append("topology_component.config as config,")
        .append("topology_component_bundle.type AS type ")
        .append("FROM topology_component, topology_component_bundle ")
        .append("WHERE topology_component.topologyId = ? ")
        .append("AND topology_component.id = ? ")
        .append("AND topology_component.componentBundleId = topology_component_bundle.id");
    String sql = sqlBuilder.toString();
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId,
        componentId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      } else {
        TopologyComponent component = mapToTopologyComponent(rs);
        // Get output streams
        Collection<TopologyStream> streams = listTopologyStreams(topologyId);
        for (TopologyStream stream : streams) {
          if (stream.getComponentId() == component.getId()) {
            if (component instanceof TopologySource) {
              ((TopologySource) component).addOutputStream(stream);
            } else if (component instanceof TopologyProcessor) {
              ((TopologyProcessor) component).addOutputStream(stream);
            }
          }
        }

        return (T) component;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Deprecated
  public TopologyEditorToolbar getTopologyEditorToolbar() {
    TopologyEditorToolbar toolbar = new TopologyEditorToolbar();
    toolbar.setUserId(1L);
    JsonObject data = new JsonObject();
    JsonArray sources = new JsonArray();
    JsonArray processors = new JsonArray();
    JsonArray sinks = new JsonArray();
    for (TopologyComponentBundle bundle : listTopologyComponentBundles()) {
      JsonObject b = new JsonObject();
      b.addProperty("bundleId", bundle.getId());
      if (bundle.getType() == TopologyComponentType.SOURCE) {
        sources.add(b);
      } else if (bundle.getType() == TopologyComponentType.SINK) {
        sinks.add(b);
      } else if (bundle.getType() == TopologyComponentType.PROCESSOR) {
        processors.add(b);
      }
    }
    data.add("sources", sources);
    data.add("sinks", sinks);
    data.add("processors", processors);
    toolbar.setData(data.toString());
    toolbar.setTimestamp(System.currentTimeMillis());
    return toolbar;
  }

  @Deprecated
  public TopologyEditorToolbar addOrUpdateTopologyEditorToolbar(TopologyEditorToolbar toolbar) {
    return toolbar;
  }

  @Deprecated
  public Collection<ClusterWithService> listClusterWithServices() {
    Collection<ClusterWithService> clusterWithServices = new ArrayList<>();
    ClusterWithService cs = new ClusterWithService();
    cs.setId(1L);
    ClusterWithService.Cluster cluster = new ClusterWithService.Cluster();
    cluster.setId(1L);
    cluster.setDescription("First cluster");
    cluster.setName("First Cluster");
    cluster.setTimestamp(System.currentTimeMillis());
    cs.setCluster(cluster);

    List<ServiceConfiguration> serviceConfigurations = new ArrayList<>();
    ServiceConfiguration sc = new ServiceConfiguration();
    ClusterWithService.Service service = new ClusterWithService.Service();
    service.setId(1L);
    service.setName("STORM");
    service.setClusterId(1L);
    service.setDescription("");
    service.setTimestamp(System.currentTimeMillis());
    sc.setService(service);
    List<ClusterWithService.Configuration> configurations = new ArrayList<>();
    ClusterWithService.Configuration configuration = new ClusterWithService.Configuration();
    configuration.setId(1L);
    configuration.setServiceId(1L);
    configuration.setName("storm");
    configuration.setConfiguration("{}");
    configuration.setDescription("");
    configuration.setFilename("");
    configuration.setTimestamp(System.currentTimeMillis());
    configuration.setConfigurationMap(new HashMap<>());
    configurations.add(configuration);
    sc.setConfigurations(configurations);
    serviceConfigurations.add(sc);
    cs.setServiceConfigurations(serviceConfigurations);
    clusterWithServices.add(cs);
    return clusterWithServices;
  }

  public String exportTopology(Topology topology) throws Exception {
    TopologyData topologyData = doExportTopology(topology);
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(topologyData);
  }

  public TopologyData doExportTopology(Topology topology) {
    TopologyData topologyData = new TopologyData();
    topologyData.setTopologyName(topology.getName());
    topologyData.setConfig(topology.getConfigStr());
    topologyData.setTopologyEditorMetadata(
        getTopologyEditorMetadata(topology.getId()));

    topologyData.setSources(
        new ArrayList<>(listSources(topology.getId())));
    topologyData.setProcessors(
        new ArrayList<>(listProcessors(topology.getId())));
    topologyData.setSinks(
        new ArrayList<>(listSinks(topology.getId())));
    topologyData
        .setEdges(new ArrayList<>(listTopologyEdges(topology.getId())));

    return topologyData;
  }

  public Topology importTopology(String topologyName, TopologyData topologyData) {
    // Add topology
    Topology newTopology = new Topology();
    newTopology.setName(topologyName);
    newTopology.setConfigStr(topologyData.getConfigStr());
    // Hard-code
    newTopology.setTimestamp(System.currentTimeMillis());
    newTopology.setVersionId(1L);
    newTopology.setNamespaceId(1L);
    newTopology = addTopology(newTopology);

    // Add topology editor meta data
    TopologyEditorMetadata metadata = topologyData.getTopologyEditorMetadata();
    metadata.setTopologyId(newTopology.getId());
    addTopologyEditorMetadata(metadata);

    // Map edge connection with new component ids
    // Key: old component id, value: new component id
    Map<Long, Long> updatedEdgeMap = new HashMap<>();
    // Key: old stream id, value: new stream id
    Map<Long, Long> updatedStreamMap = new HashMap<>();

    // Add source
    List<TopologySource> sources = topologyData.getSources();
    for (TopologySource source : sources) {
      Long oldId = source.getId();
      source.setTopologyId(newTopology.getId());
      TopologySource newSource = addTopologyComponent(newTopology.getId(), source);
      for (TopologyStream stream : source.getOutputStreams()) {
        Long oldStreamId = stream.getId();
        stream.setTopologyId(newTopology.getId());
        stream.setComponentId(newSource.getId());
        TopologyStream newStream = addTopologyStream(stream);
        updatedStreamMap.put(oldStreamId, newStream.getId());
      }
      updatedEdgeMap.put(oldId, newSource.getId());
    }

    // Add processor
    List<TopologyProcessor> processors = topologyData.getProcessors();
    for (TopologyProcessor processor : processors) {
      Long oldId = processor.getId();
      processor.setTopologyId(newTopology.getId());
      TopologyProcessor newProcessor = addTopologyComponent(newTopology.getId(), processor);
      for (TopologyStream stream : processor.getOutputStreams()) {
        Long oldStreamId = stream.getId();
        stream.setTopologyId(newTopology.getId());
        stream.setComponentId(newProcessor.getId());
        TopologyStream newStream = addTopologyStream(stream);
        updatedStreamMap.put(oldStreamId, newStream.getId());
      }
      updatedEdgeMap.put(oldId, newProcessor.getId());
    }

    // Add sink
    List<TopologySink> sinks = topologyData.getSinks();
    for (TopologySink sink : sinks) {
      Long oldId = sink.getId();
      sink.setTopologyId(newTopology.getId());
      TopologySink newSink = addTopologyComponent(newTopology.getId(), sink);
      updatedEdgeMap.put(oldId, newSink.getId());
    }

    // Add edges
    List<TopologyEdge> edges = topologyData.getEdges();
    for (TopologyEdge edge : edges) {
      edge.setTopologyId(newTopology.getId());
      edge.setFromId(updatedEdgeMap.get(edge.getFromId()));
      edge.setToId(updatedEdgeMap.get(edge.getToId()));
      for (StreamGrouping streamGrouping : edge.getStreamGroupings()) {
        streamGrouping.setStreamId(updatedStreamMap.get(streamGrouping.getStreamId()));
      }
      addTopologyEdge(newTopology.getId(), edge);
    }

    return newTopology;
  }
}
