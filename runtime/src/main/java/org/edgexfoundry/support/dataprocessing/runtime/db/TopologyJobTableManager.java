package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJob;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJobState;

public class TopologyJobTableManager extends AbstractStorageManager {

  private static TopologyJobTableManager instance = null;

  public synchronized static TopologyJobTableManager getInstance() {
    if (instance == null) {
      instance = new TopologyJobTableManager(
          "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH,
          Settings.DB_CLASS);
    }
    return instance;
  }

  private TopologyJobTableManager(String jdbcUrl, String jdbcClass) {
    super(jdbcUrl, jdbcClass);
  }

  public TopologyJobState addOrUpdateTopologyJobState(String jobId, TopologyJobState jobState) {
    if (jobId == null || jobState == null) {
      throw new RuntimeException("Job id or job state is null.");
    }

    String sql = "INSERT OR REPLACE INTO job_state (jobId, state, startTime, engineId, engineType) "
        + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        jobId, jobState.getState(), jobState.getStartTime(), jobState.getEngineId(),
        jobState.getEngineType())) {
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Failed to insert job state.");
      } else {
        commit();
        return jobState;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  public TopologyJob addOrUpdateTopologyJob(TopologyJob topologyJob) {
    if (topologyJob == null) {
      throw new RuntimeException("Topology job is null.");
    }

    String sql = "INSERT OR REPLACE INTO job (id, topologyId, config) VALUES (?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
        topologyJob.getId(), topologyJob.getTopologyId(),
        topologyJob.getConfigStr())) {
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Failed to insert job.");
      } else {
        addOrUpdateTopologyJobState(topologyJob.getId(), topologyJob.getState());
        commit();
        return topologyJob;
      }
    } catch (SQLException e) {
      rollback();
      throw new RuntimeException(e);
    }
  }

  public Collection<TopologyJob> listTopologyJobs(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.topologyId AS topologyId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.topologyId = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {
      Collection<TopologyJob> jobs = new ArrayList<>();
      while (rs.next()) {
        jobs.add(mapToTopologyJob(rs));
      }
      return jobs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private TopologyJob mapToTopologyJob(ResultSet rs) throws SQLException {
    TopologyJob job = new TopologyJob();
    job.setId(rs.getString("id"));
    job.setTopologyId(rs.getLong("topologyId"));
    job.setConfigStr(rs.getString("config"));

    TopologyJobState jobState = new TopologyJobState();
    jobState.setState(rs.getString("state"));
    jobState.setStartTime(rs.getLong("startTime"));
    jobState.setEngineId(rs.getString("engineId"));
    jobState.setEngineType(rs.getString("engineType"));
    job.setState(jobState);

    return job;
  }

  public TopologyJob getTopologyJob(Long topologyId, String jobId) {
    if (topologyId == null || jobId == null) {
      throw new RuntimeException("Topology id or job id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.topologyId AS topologyId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.topologyId = ? AND job.id = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId, jobId);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return mapToTopologyJob(rs);
      } else {
        throw new RuntimeException("Topology job not found.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
