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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge.StreamGrouping;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream;

public final class WorkflowTableManager extends AbstractStorageManager {

  private static WorkflowTableManager instance = null;

  public synchronized static WorkflowTableManager getInstance() {
    if (instance == null) {
      instance = new WorkflowTableManager("jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH,
          Settings.DB_CLASS);
    }
    return instance;
  }

  private WorkflowTableManager(String jdbcUrl, String jdbcClass) {
    super(jdbcUrl, jdbcClass);
  }

  /**
   * Returns a collection of topologies
   *
   * @return Collection of topologies
   */
  public Collection<Workflow> listWorkflows() {
    String sql = "SELECT id, name, config FROM workflow";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql);
        ResultSet rs = ps.executeQuery()) {
      Collection<Workflow> topologies = new ArrayList<>();
      while (rs.next()) {
        topologies.add(mapToWorkflow(rs));
      }
      return topologies;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a collection of every workflow component bundles
   *
   * @return Workflow component bundles
   */
  public Collection<WorkflowComponentBundle> listWorkflowComponentBundles() {
    Collection<WorkflowComponentBundle> bundles = new ArrayList<>();
    String sql = "SELECT * FROM workflow_component_bundle";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        bundles.add(mapToWorkflowComponentBundle(rs));
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    return bundles;
  }

  /**
   * Adds a workflow to database. Workflow id is automatically generated.
   *
   * @param workflow workflow to add
   * @return workflow with a new id assigned
   */
  public Workflow addWorkflow(Workflow workflow) {
    if (workflow == null) {
      throw new RuntimeException("Workflow is null.");
    }

    String sql = "INSERT INTO workflow (name, config) VALUES (?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflow.getName(),
        workflow.getConfigStr())) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating workflow failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          workflow.setId(generatedKeys.getLong(1));
          commit();
          return workflow;
        } else {
          throw new RuntimeException("Creating workflow failed, no ID obtained.");
        }
      }
    } catch (Exception e) {
      rollback();
      throw new RuntimeException(e);
    }
  }


  /**
   * Inserts or updates existing workflow
   *
   * @param workflowId workflow id to update
   * @param workflow workflow to update
   * @return updated workflow
   */
  public Workflow addOrUpdateWorkflow(Long workflowId, Workflow workflow) {
    if (workflow == null || workflowId == null) {
      throw new RuntimeException("Workflow or id is null.");
    }
    workflow.setId(workflowId);

    String sql = "INSERT OR REPLACE INTO workflow (id, name, config) VALUES (?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowId,
        workflow.getName(),
        workflow.getConfigStr())) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating workflow failed, no rows affected.");
      } else {
        workflow.setId(workflowId);
        commit();
        return workflow;
      }
    } catch (Exception e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a workflow by id
   *
   * @param workflowId id of a workflow to find
   * @return {@link Workflow} if found, otherwise null.
   */
  public Workflow getWorkflow(Long workflowId) {
    if (workflowId == null) {
      return null;
    }

    String sql = "SELECT id, name, config FROM workflow WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {

      if (!rs.next()) {
        return null; // workflow not found
      }

      return mapToWorkflow(rs);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts a new workflow editor metadata
   *
   * @param editorMetadata new workflow editor metadata
   * @return inserted workflow editor metadata
   */
  public WorkflowEditorMetadata addWorkflowEditorMetadata(WorkflowEditorMetadata editorMetadata) {
    if (editorMetadata == null) {
      throw new RuntimeException("Workflow editor metadata is null.");
    }

    String sql = "INSERT INTO workflow_editor_metadata (workflowId, data) VALUES (?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        editorMetadata.getWorkflowId(), editorMetadata.getData())) {
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating workflow editor metadata failed, no rows affected.");
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
   * Inserts or replaces existing workflow editor metadata
   *
   * @param workflowId workflow id
   * @param editorMetadata updated editor metadata
   * @return updated editor metadata
   */
  public WorkflowEditorMetadata addOrUpdateWorkflowEditorMetadata(
      Long workflowId,
      WorkflowEditorMetadata editorMetadata) {
    if (workflowId == null || editorMetadata == null) {
      throw new RuntimeException("Workflow id or editor metadata is null.");
    }
    editorMetadata.setWorkflowId(workflowId);

    String sql = "INSERT OR REPLACE INTO workflow_editor_metadata (workflowId, data) VALUES(?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        editorMetadata.getWorkflowId(), editorMetadata.getData())) {
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating workflow editor metadata failed, no rows affected.");
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
   * Inserts or replaces existing workflow
   *
   * @param workflowId workflow id to update
   * @param workflow workflow to update
   * @return updated workflow
   */
  public Workflow updateWorkflow(Long workflowId, Workflow workflow) {
    if (workflowId == null || workflow == null) {
      throw new RuntimeException("Workflow or its id is null.");
    }

    String sql = "INSERT OR REPLACE INTO workflow (id, name, config) VALUES(?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
        workflow.getName(), workflow.getConfigStr())) {

      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating workflow failed, no rows affected.");
      } else {
        workflow.setId(workflowId);
        commit();
        return workflow;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes a workflow using id
   *
   * @param workflowId workflow id
   */
  public Workflow removeWorkflow(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    Workflow workflow = getWorkflow(workflowId);
    if (workflow == null) {
      throw new RuntimeException("Workflow with id=" + workflowId + " not found.");
    }

    removeWorkflowEditorMetadata(workflowId); // delete meta

    String sql = "DELETE FROM workflow WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId)) {
      ps.executeUpdate();
      commit();
      return workflow;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a workflow editor metadata of a workflow
   *
   * @param workflowId Workflow id
   * @return Editor metadata
   */
  public WorkflowEditorMetadata getWorkflowEditorMetadata(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    String sql = "SELECT workflowId, data FROM workflow_editor_metadata WHERE workflowId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {

      if (!rs.next()) {
        throw new RuntimeException(
            "Workflow editor metadata for " + workflowId + " does not exist.");
      } else {
        WorkflowEditorMetadata metadata = new WorkflowEditorMetadata();
        metadata.setWorkflowId(rs.getLong("workflowId"));
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
   * Removes workflow editor metadata for a workflow
   *
   * @param workflowId workflow id
   */
  public void removeWorkflowEditorMetadata(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    String sql = "DELETE FROM workflow_editor_metadata WHERE workflowId = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }


  /**
   * Returns a list of workflow component bundles of a specific type
   *
   * @param type Type of a component bundle to list
   * @return List of component bundles with specified type
   */
  public Collection<WorkflowComponentBundle> listWorkflowComponentBundles(
      WorkflowComponentBundleType type) {
    if (type == null) {
      throw new RuntimeException("Invalid workflow component type.");
    }

    Collection<WorkflowComponentBundle> bundles = new ArrayList<>();
    String sql = "SELECT * FROM workflow_component_bundle WHERE type = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, type.name());
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        bundles.add(mapToWorkflowComponentBundle(rs));
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    return bundles;
  }

  /**
   * Inserts a workflow component bundle
   *
   * @param bundle bundle to insert
   * @return inserted bundle with id assigned
   */
  public WorkflowComponentBundle addWorkflowComponentBundle(WorkflowComponentBundle bundle) {
    if (bundle == null) {
      throw new RuntimeException("Workflow component bundle is null.");
    }

    String sql = "INSERT INTO workflow_component_bundle "
        + "(name, type, subType, streamingEngine, path, classname, param, componentUISpecification, removable) "
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        bundle.getName(), bundle.getType().name(), bundle.getSubType(),
        bundle.getStreamingEngine(),
        bundle.getBundleJar(), bundle.getTransformationClass(),
        bundle.getWorkflowComponentUISpecification().toString(),
        bundle.getWorkflowComponentUISpecification().toString(),
        bundle.isBuiltin() ? "0" : "1")) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
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
   * Inserts or replaces workflow component bundle
   *
   * @param bundle bundle to update
   * @return updated bundle
   */
  public WorkflowComponentBundle addOrUpdateWorkflowComponentBundle(
      WorkflowComponentBundle bundle) {
    if (bundle == null) {
      throw new RuntimeException("Workflow component bundle is null.");
    }

    String sql = "INSERT OR REPLACE INTO workflow_component_bundle "
        + "(id, name, type, subType, streamingEngine, path, classname, param, componentUISpecification, removable) "
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        bundle.getId(),
        bundle.getName(), bundle.getType().name(), bundle.getSubType(),
        bundle.getStreamingEngine(),
        bundle.getBundleJar(), bundle.getTransformationClass(),
        bundle.getWorkflowComponentUISpecification().toString(),
        bundle.getWorkflowComponentUISpecification().toString(),
        bundle.isBuiltin() ? '0' : '1')) {

      int affectedRows;
      affectedRows = ps.executeUpdate();
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

  public WorkflowComponentBundle getWorkflowComponentBundle(String componentName,
      WorkflowComponentBundleType componentType,
      String componentSubType) {
    if (componentName == null || componentType == null || componentSubType == null) {
      throw new RuntimeException("Component name, type or subtype is null.");
    }

    String sql = "SELECT * FROM workflow_component_bundle WHERE " +
        "name = ? AND type = ? AND subType = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, componentName,
        componentType.name(), componentSubType);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      } else {
        return mapToWorkflowComponentBundle(rs);
      }
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes a workflow component bundle by id
   *
   * @param workflowComponentBundleId bundle id to remove
   */
  public void removeWorkflowComponentBundle(Long workflowComponentBundleId) {
    if (workflowComponentBundleId == null) {
      throw new RuntimeException("Workflow component bundle id is null.");
    }

    String sql = "DELETE FROM workflow_component_bundle WHERE id = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowComponentBundleId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of workflow streams for a given workflow id
   *
   * @param workflowId workflow id
   * @return Workflow streams
   */
  public Collection<WorkflowStream> listWorkflowStreams(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    Collection<WorkflowStream> streams = new ArrayList<>();
    String sql = "SELECT * FROM workflow_stream WHERE workflowId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        streams.add(mapToWorkflowStream(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return streams;
  }

  public WorkflowStream getWorkflowStream(Long workflowId, Long streamId) {
    if (workflowId == null || streamId == null) {
      throw new RuntimeException("Workflow id or stream id is null.");
    }

    String sql = "SELECT * FROM workflow_stream WHERE workflowId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId, streamId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        throw new RuntimeException("Workflow stream does not exist.");
      } else {
        return mapToWorkflowStream(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Inserts a new workflow stream
   *
   * @param workflowStream workflow stream
   * @return workflow stream with updated id
   */
  public WorkflowStream addWorkflowStream(WorkflowStream workflowStream) {
    if (workflowStream == null) {
      throw new RuntimeException("WorkflowStream is null.");
    }

    String sql = "INSERT INTO workflow_stream (workflowId, componentId, streamName, fields) VALUES"
        + "(?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowStream.getWorkflowId(), workflowStream.getComponentId(),
        workflowStream.getStreamId(), workflowStream.getFieldsStr())) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating stream failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          workflowStream.setId(generatedKeys.getLong(1));
          commit();
          return workflowStream;
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
   * Inserts or replaces existing workflow stream
   *
   * @param workflowStreamId workflow stream id to update
   * @param stream workflow stream to update
   * @return updated workflow stream
   */
  public WorkflowStream addOrUpdateWorkflowStream(Long workflowStreamId, WorkflowStream stream) {
    if (stream == null) {
      throw new RuntimeException("Workflow stream is null.");
    }
    stream.setId(workflowStreamId);

    // Add if stream id does not exist
    if (workflowStreamId == null) {
      stream = addWorkflowStream(stream);
      return stream;
    }

    // Otherwise, update
    stream.setId(workflowStreamId);
    String sql =
        "INSERT OR REPLACE INTO workflow_stream (id, workflowId, componentId, streamName, fields) VALUES"
            + "(?,?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowStreamId,
        stream.getWorkflowId(), stream.getComponentId(),
        stream.getStreamId(), stream.getFieldsStr())) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
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
   * Removes a workflow stream using workflow and component ids
   *
   * @param workflowId workflow id
   * @param streamId stream id
   * @return removed workflow stream
   */
  public WorkflowStream removeWorkflowStream(Long workflowId, Long streamId) {
    if (workflowId == null || streamId == null) {
      throw new RuntimeException("Workflow id or stream id is null.");
    }

    WorkflowStream stream = getWorkflowStream(workflowId, streamId);
    if (stream == null) {
      throw new RuntimeException("Workflow stream does not exist.");
    }

    String sql = "DELETE FROM workflow_stream WHERE workflowId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
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
   * Returns a list of workflow edges for a given workflow id
   *
   * @param workflowId workflow id
   * @return workflow edges
   */
  public Collection<WorkflowEdge> listWorkflowEdges(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    Collection<WorkflowEdge> edges = new ArrayList<>();
    String sql = "SELECT * FROM workflow_edge WHERE workflowId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        edges.add(mapToWorkflowEdge(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return edges;
  }

  /**
   * Returns a workflow edge for given workflow id and edge id
   *
   * @param workflowId workflow id
   * @param edgeId edge id
   * @return {@link WorkflowEdge}
   */
  public WorkflowEdge getWorkflowEdge(Long workflowId, Long edgeId) {
    if (workflowId == null || edgeId == null) {
      throw new RuntimeException("Workflow id or edge id is null.");
    }

    String sql = "SELECT * FROM workflow_edge WHERE workflowId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId, edgeId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        throw new RuntimeException(
            "Edge not found for workflowId:" + workflowId + " / edgeId:" + edgeId);
      } else {
        return mapToWorkflowEdge(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Insert a workflow edge
   *
   * @param workflowId workflow id
   * @param workflowEdge workflow edge to add
   * @return updated workflow edge
   */
  public WorkflowEdge addWorkflowEdge(Long workflowId, WorkflowEdge workflowEdge) {
    if (workflowId == null || workflowEdge == null) {
      throw new RuntimeException("Workflow id or edge is null.");
    }
    workflowEdge.setWorkflowId(workflowId);

    String sql = "INSERT INTO workflow_edge (workflowId, fromId, toId, streamGroupings) "
        + "VALUES (?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowEdge.getWorkflowId(), workflowEdge.getFromId(),
        workflowEdge.getToId(), workflowEdge.getStreamGroupings())) {
      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating edge failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          workflowEdge.setId(generatedKeys.getLong(1));
          commit();
          return workflowEdge;
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
   * Inserts or replaces workflow edge for a given workflow id, edge id
   *
   * @param workflowId workflow id
   * @param workflowEdgeId edge id
   * @param workflowEdge edge
   * @return updated workflow edge
   */
  public WorkflowEdge addOrUpdateWorkflowEdge(Long workflowId, Long workflowEdgeId,
      WorkflowEdge workflowEdge) {
    if (workflowId == null || workflowEdgeId == null || workflowEdge == null) {
      throw new RuntimeException("Workflow id, edge id or edge is null.");
    }
    workflowEdge.setId(workflowEdgeId);
    workflowEdge.setWorkflowId(workflowId);

    String sql =
        "INSERT OR REPLACE INTO workflow_edge (id, workflowId, fromId, toId, streamGroupings) "
            + "VALUES (?,?,?,?,?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowEdgeId,
        workflowEdge.getWorkflowId(), workflowEdge.getFromId(),
        workflowEdge.getToId(), workflowEdge.getStreamGroupings())) {
      // update
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating edge failed, no rows affected.");
      }
      commit();
      return workflowEdge;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  private void removeWorkflowComponentEdges(Long workflowId, Long componentId) {
    if (workflowId == null || componentId == null) {
      throw new RuntimeException("Workflow id or component id is null.");
    }

    String sql = "DELETE FROM workflow_edge WHERE workflowId = ? AND (fromId = ? OR toId = ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
        componentId, componentId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Remove workflow edge by workflow id and edge id
   *
   * @param workflowId workflow id
   * @param edgeId edge id
   */
  public WorkflowEdge removeWorkflowEdge(Long workflowId, Long edgeId) {
    if (workflowId == null || edgeId == null) {
      throw new RuntimeException("Workflow id or edge id is null.");
    }

    WorkflowEdge edge = getWorkflowEdge(workflowId, edgeId);
    if (edge == null) {
      throw new RuntimeException("Workflow edge not found.");
    }

    String sql = "DELETE FROM workflow_edge WHERE workflowId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
        edgeId)) {
      ps.executeUpdate();
      commit();
      return edge;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of workflow components for given workflow id
   *
   * @param workflowId workflow id
   * @return A collection of workflow components
   */
  public Collection<WorkflowComponent> listWorkflowComponents(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    // Retrieve streams
    Collection<WorkflowStream> streams = listWorkflowStreams(workflowId);
    Map<Long, List<WorkflowStream>> streamMap = streams.stream()
        .collect(Collectors.groupingBy(WorkflowStream::getComponentId));

    Collection<WorkflowComponent> components = new ArrayList<>();
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT ")
        .append("workflow_component.id AS id,")
        .append("workflow_component.workflowId AS workflowId,")
        .append("workflow_component.componentBundleId AS componentBundleId,")
        .append("workflow_component.name AS name,")
        .append("workflow_component.config as config,")
        .append("workflow_component_bundle.path as path,")
        .append("workflow_component_bundle.classname as classname,")
        .append("workflow_component_bundle.type AS type, ")
        .append("workflow_component_bundle.streamingEngine AS streamingEngine ")
        .append("FROM workflow_component, workflow_component_bundle ")
        .append("WHERE workflow_component.workflowId = ? ")
        .append("AND workflow_component.componentBundleId = workflow_component_bundle.id");
    String sql = sqlBuilder.toString();
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        WorkflowComponent component = mapToWorkflowComponent(rs);
        if (component == null) {
          continue;
        } else if (component instanceof WorkflowSource
            && streamMap.get(component.getId()) != null) {
          ((WorkflowSource) component).setOutputStreams(streamMap.get(component.getId()));
        } else if (component instanceof WorkflowProcessor
            && streamMap.get(component.getId()) != null) {
          ((WorkflowProcessor) component).setOutputStreams(streamMap.get(component.getId()));
        }
        components.add(component);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return components;
  }

  /**
   * Inserts a new workflow component for a given workflow id
   *
   * @param workflowId workflow id
   * @param workflowComponent workflow component to insert
   * @return updated workflow component
   */
  public <T extends WorkflowComponent> T addWorkflowComponent(Long workflowId,
      WorkflowComponent workflowComponent) {
    if (workflowId == null || workflowComponent == null) {
      throw new RuntimeException("Either workflow id or component is null.");
    }

    String sql = "INSERT INTO workflow_component (workflowId, componentBundleId, name, config) "
        + "VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowId, workflowComponent.getWorkflowComponentBundleId(),
        workflowComponent.getName(), workflowComponent.getConfigStr())) {

      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Creating component failed, no rows affected.");
      }

      // Get auto-incremented id
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          workflowComponent.setId(generatedKeys.getLong(1));

          // Insert output streams
          List<WorkflowStream> streams = null;
          if ((workflowComponent instanceof WorkflowSource)) {
            streams = ((WorkflowSource) workflowComponent).getOutputStreams();
          } else if ((workflowComponent instanceof WorkflowProcessor)) {
            streams = ((WorkflowProcessor) workflowComponent).getOutputStreams();
          }
          if (streams != null) {
            for (WorkflowStream stream : streams) {
              stream.setWorkflowId(workflowId);
              stream.setComponentId(workflowComponent.getId());
              addWorkflowStream(stream);
            }
          }

          commit();
          return (T) workflowComponent;
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
   * Adds or updates existing workflow component for a given workflow id
   *
   * @param workflowId workflow id
   * @param workflowComponentId workflow component id to update
   * @param workflowComponent workflow component to update
   * @return updated workflow component
   */
  public <T extends WorkflowComponent> T addOrUpdateWorkflowComponent(Long workflowId,
      Long workflowComponentId,
      WorkflowComponent workflowComponent) {
    if (workflowId == null || workflowComponentId == null || workflowComponent == null) {
      throw new RuntimeException("Workflow id, component id or component is null.");
    }
    workflowComponent.setId(workflowComponentId);
    workflowComponent.setWorkflowId(workflowId);

    String sql =
        "INSERT OR REPLACE INTO workflow_component (id, workflowId, componentBundleId, name, config) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        workflowComponentId,
        workflowId, workflowComponent.getWorkflowComponentBundleId(),
        workflowComponent.getName(), workflowComponent.getConfigStr())) {

      // insert
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Updating component failed, no rows affected.");
      }

      // Update output streams
      List<WorkflowStream> streams = null;
      if ((workflowComponent instanceof WorkflowSource)) {
        streams = ((WorkflowSource) workflowComponent).getOutputStreams();
      } else if ((workflowComponent instanceof WorkflowProcessor)) {
        streams = ((WorkflowProcessor) workflowComponent).getOutputStreams();
      }
      if (streams != null) {
        for (WorkflowStream stream : streams) {
          stream.setWorkflowId(workflowId);
          stream.setComponentId(workflowComponentId);
          addOrUpdateWorkflowStream(stream.getId(), stream);
        }
      }

      commit();
      return (T) workflowComponent;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  /**
   * Remove workflow component by workflow id and component id
   *
   * @param workflowId workflow id
   * @param workflowComponentId component id
   */

  public <T extends WorkflowComponent> T removeWorkflowComponent(Long workflowId,
      Long workflowComponentId) {
    if (workflowId == null || workflowComponentId == null) {
      throw new RuntimeException("Workflow id or component id is null.");
    }

    WorkflowComponent component = getWorkflowComponent(workflowId, workflowComponentId);
    if (component == null) {
      throw new RuntimeException("Workflow component does not exist.");
    }

    // remove edges
    removeWorkflowComponentEdges(workflowId, workflowComponentId);

    // remove streams
    // removeWorkflowComponentStreams(workflowId, workflowComponentId);

    String sql = "DELETE FROM workflow_component WHERE workflowId = ? AND id = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
        workflowComponentId)) {
      ps.executeUpdate();
      commit();
      return (T) component;
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  private void removeWorkflowComponentStreams(Long workflowId, Long workflowComponentId) {
    if (workflowId == null || workflowComponentId == null) {
      throw new RuntimeException("Workflow id or component id is null.");
    }

    String sql = "DELETE FROM workflow_stream WHERE workflowId = ? AND componentId = ?";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql, workflowId,
        workflowComponentId)) {
      ps.executeUpdate();
      commit();
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  private Workflow mapToWorkflow(ResultSet rs) throws SQLException {
    Workflow workflow = new Workflow();
    workflow.setId(rs.getLong("id"));
    workflow.setName(rs.getString("name"));
    workflow.setConfigStr(rs.getString("config"));
    return workflow;
  }

  private WorkflowEdge mapToWorkflowEdge(ResultSet rs) throws SQLException {
    WorkflowEdge edge = new WorkflowEdge();
    edge.setId(rs.getLong("id"));
    edge.setWorkflowId(rs.getLong("workflowId"));
    edge.setFromId(rs.getLong("fromId"));
    edge.setToId(rs.getLong("toId"));
    edge.setStreamGroupingsStr(rs.getString("streamGroupings"));
    return edge;
  }

  private WorkflowComponentBundle mapToWorkflowComponentBundle(ResultSet rs)
      throws SQLException, IOException {
    WorkflowComponentBundle bundle = new WorkflowComponentBundle();
    bundle.setId(rs.getLong("id"));
    bundle.setName(rs.getString("name"));
    bundle.setType(WorkflowComponentBundleType.toWorkflowComponentBundleType(rs.getString("type")));
    bundle.setSubType(rs.getString("subType"));
    bundle.setStreamingEngine(rs.getString("streamingEngine"));

    String ui = rs.getString("componentUISpecification");
    ComponentUISpecification uiSpecification = ComponentUISpecification
        .create(ui, ComponentUISpecification.class);
    bundle.setWorkflowComponentUISpecification(uiSpecification);

    // TODO: is this correct mapping?
    bundle.setBundleJar(rs.getString("path"));
    bundle.setTransformationClass(rs.getString("classname"));
    bundle.setBuiltin(rs.getByte("removable") == (byte) '0');
    return bundle;
  }

  private WorkflowStream mapToWorkflowStream(ResultSet rs) throws SQLException {
    WorkflowStream stream = new WorkflowStream();
    stream.setId(rs.getLong("id"));
    stream.setWorkflowId(rs.getLong("workflowId"));
    stream.setComponentId(rs.getLong("componentId"));
    stream.setStreamId(rs.getString("streamName"));
    stream.setFieldsStr(rs.getString("fields"));
    return stream;
  }

  private WorkflowComponent mapToWorkflowComponent(ResultSet rs) throws SQLException {
    WorkflowComponentBundleType type = WorkflowComponentBundleType
        .toWorkflowComponentBundleType(rs.getString("type"));
    WorkflowComponent component = null;
    switch (type) {
      case SOURCE:
        component = new WorkflowSource();
        break;
      case PROCESSOR:
        component = new WorkflowProcessor();
        break;
      case SINK:
        component = new WorkflowSink();
        break;
      case LINK:
      case WORKFLOW:
        break;
    }
    if (component == null) {
      return null;
    }
    component.setId(rs.getLong("id"));
    component.setWorkflowId(rs.getLong("workflowId"));
    component.setWorkflowComponentBundleId(rs.getLong("componentBundleId"));
    component.setName(rs.getString("name"));
    component.setEngineType(rs.getString("streamingEngine"));
    component.setConfigStr(rs.getString("config"));
    component.setPath(rs.getString("path"));
    component.setClassname(rs.getString("classname"));

    return component;
  }

  public Collection<WorkflowSource> listSources(Long workflowId) {
    Collection<WorkflowComponent> components = listWorkflowComponents(workflowId);
    return components.stream().filter(component -> component instanceof WorkflowSource)
        .map(component -> (WorkflowSource) component)
        .collect(Collectors.toSet());
  }

  public Collection<WorkflowSink> listSinks(Long workflowId) {
    Collection<WorkflowComponent> components = listWorkflowComponents(workflowId);
    return components.stream().filter(component -> component instanceof WorkflowSink)
        .map(component -> (WorkflowSink) component)
        .collect(Collectors.toSet());
  }

  public Collection<WorkflowProcessor> listProcessors(Long workflowId) {
    Collection<WorkflowComponent> components = listWorkflowComponents(workflowId);
    return components.stream().filter(component -> component instanceof WorkflowProcessor)
        .map(component -> (WorkflowProcessor) component)
        .collect(Collectors.toSet());
  }

  public <T extends WorkflowComponent> T getWorkflowComponent(Long workflowId, Long componentId) {
    if (workflowId == null || componentId == null) {
      throw new RuntimeException("Workflow id or component id is null.");
    }

    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT ")
        .append("workflow_component.id AS id,")
        .append("workflow_component.workflowId AS workflowId,")
        .append("workflow_component.componentBundleId AS componentBundleId,")
        .append("workflow_component.name AS name,")
        .append("workflow_component.config as config,")
        .append("workflow_component_bundle.path as path,")
        .append("workflow_component_bundle.classname as classname,")
        .append("workflow_component_bundle.type AS type, ")
        .append("workflow_component_bundle.streamingEngine AS streamingEngine ")
        .append("FROM workflow_component, workflow_component_bundle ")
        .append("WHERE workflow_component.workflowId = ? ")
        .append("AND workflow_component.id = ? ")
        .append("AND workflow_component.componentBundleId = workflow_component_bundle.id");
    String sql = sqlBuilder.toString();
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId,
        componentId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      } else {
        WorkflowComponent component = mapToWorkflowComponent(rs);
        // Get output streams
        Collection<WorkflowStream> streams = listWorkflowStreams(workflowId);
        for (WorkflowStream stream : streams) {
          if (stream.getComponentId() == component.getId()) {
            if (component instanceof WorkflowSource) {
              ((WorkflowSource) component).addOutputStream(stream);
            } else if (component instanceof WorkflowProcessor) {
              ((WorkflowProcessor) component).addOutputStream(stream);
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
  public WorkflowEditorToolbar getWorkflowEditorToolbar() {
    WorkflowEditorToolbar toolbar = new WorkflowEditorToolbar();
    toolbar.setUserId(1L);
    JsonObject data = new JsonObject();
    JsonArray sources = new JsonArray();
    JsonArray processors = new JsonArray();
    JsonArray sinks = new JsonArray();
    for (WorkflowComponentBundle bundle : listWorkflowComponentBundles()) {
      JsonObject b = new JsonObject();
      b.addProperty("bundleId", bundle.getId());
      if (bundle.getType() == WorkflowComponentBundleType.SOURCE) {
        sources.add(b);
      } else if (bundle.getType() == WorkflowComponentBundleType.SINK) {
        sinks.add(b);
      } else if (bundle.getType() == WorkflowComponentBundleType.PROCESSOR) {
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
  public WorkflowEditorToolbar addOrUpdateWorkflowEditorToolbar(WorkflowEditorToolbar toolbar) {
    return toolbar;
  }

  public String exportWorkflow(Workflow workflow) throws Exception {
    WorkflowData workflowData = doExportWorkflow(workflow);
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(workflowData);
  }

  public WorkflowData doExportWorkflow(Workflow workflow) {
    WorkflowData workflowData = new WorkflowData();
    workflowData.setWorkflowId(workflow.getId());
    workflowData.setWorkflowName(workflow.getName());
    workflowData.setConfig(workflow.getConfigStr());
    workflowData.setWorkflowEditorMetadata(
        getWorkflowEditorMetadata(workflow.getId()));

    workflowData.setSources(
        new ArrayList<>(listSources(workflow.getId())));
    workflowData.setProcessors(
        new ArrayList<>(listProcessors(workflow.getId())));
    workflowData.setSinks(
        new ArrayList<>(listSinks(workflow.getId())));
    workflowData
        .setEdges(new ArrayList<>(listWorkflowEdges(workflow.getId())));

    return workflowData;
  }

  public Workflow importWorkflow(String workflowName, WorkflowData workflowData) {
    // Add workflow
    Workflow newWorkflow = new Workflow();
    newWorkflow.setName(workflowName);
    newWorkflow.setConfigStr(workflowData.getConfigStr());
    newWorkflow = addWorkflow(newWorkflow);

    // Add workflow editor meta data
    WorkflowEditorMetadata metadata = workflowData.getWorkflowEditorMetadata();
    metadata.setWorkflowId(newWorkflow.getId());
    addWorkflowEditorMetadata(metadata);

    // Map edge connection with new component ids
    // Key: old component id, value: new component id
    Map<Long, Long> updatedEdgeMap = new HashMap<>();
    // Key: old stream id, value: new stream id
    Map<Long, Long> updatedStreamMap = new HashMap<>();

    // Add source
    List<WorkflowSource> sources = workflowData.getSources();
    for (WorkflowSource source : sources) {
      Long oldId = source.getId();
      source.setWorkflowId(newWorkflow.getId());
      WorkflowSource newSource = addWorkflowComponent(newWorkflow.getId(), source);
      for (WorkflowStream stream : source.getOutputStreams()) {
        Long oldStreamId = stream.getId();
        stream.setWorkflowId(newWorkflow.getId());
        stream.setComponentId(newSource.getId());
        WorkflowStream newStream = addWorkflowStream(stream);
        updatedStreamMap.put(oldStreamId, newStream.getId());
      }
      updatedEdgeMap.put(oldId, newSource.getId());
    }

    // Add processor
    List<WorkflowProcessor> processors = workflowData.getProcessors();
    for (WorkflowProcessor processor : processors) {
      Long oldId = processor.getId();
      processor.setWorkflowId(newWorkflow.getId());
      WorkflowProcessor newProcessor = addWorkflowComponent(newWorkflow.getId(), processor);
      for (WorkflowStream stream : processor.getOutputStreams()) {
        Long oldStreamId = stream.getId();
        stream.setWorkflowId(newWorkflow.getId());
        stream.setComponentId(newProcessor.getId());
        WorkflowStream newStream = addWorkflowStream(stream);
        updatedStreamMap.put(oldStreamId, newStream.getId());
      }
      updatedEdgeMap.put(oldId, newProcessor.getId());
    }

    // Add sink
    List<WorkflowSink> sinks = workflowData.getSinks();
    for (WorkflowSink sink : sinks) {
      Long oldId = sink.getId();
      sink.setWorkflowId(newWorkflow.getId());
      WorkflowSink newSink = addWorkflowComponent(newWorkflow.getId(), sink);
      updatedEdgeMap.put(oldId, newSink.getId());
    }

    // Add edges
    List<WorkflowEdge> edges = workflowData.getEdges();
    for (WorkflowEdge edge : edges) {
      edge.setWorkflowId(newWorkflow.getId());
      edge.setFromId(updatedEdgeMap.get(edge.getFromId()));
      edge.setToId(updatedEdgeMap.get(edge.getToId()));
      for (StreamGrouping streamGrouping : edge.getStreamGroupings()) {
        streamGrouping.setStreamId(updatedStreamMap.get(streamGrouping.getStreamId()));
      }
      addWorkflowEdge(newWorkflow.getId(), edge);
    }

    return newWorkflow;
  }
}
