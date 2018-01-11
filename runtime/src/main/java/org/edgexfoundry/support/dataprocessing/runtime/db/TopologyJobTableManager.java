package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJob;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJobGroup;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJobState;

public class TopologyJobTableManager extends AbstractStorageManager {

  private static TopologyJobTableManager instance = null;

  public synchronized static TopologyJobTableManager getInstance() {
    if (instance == null) {
      instance = new TopologyJobTableManager();
    }
    return instance;
  }

  public TopologyJobState addOrUpdateTopologyJobState(String jobGroupId, String jobId,
      TopologyJobState jobState) {
    if (jobGroupId == null || jobId == null || jobState == null) {
      throw new RuntimeException("Job group id, job id or job state is null.");
    }

    String sql = "INSERT OR REPLACE INTO job_state (groupId, jobId, state, startTime) "
        + "VALUES (?, ?, ?, ?)";
    synchronized (writeLock) {
      try (PreparedStatement ps = createPreparedStatement(getConnection(), sql,
          jobGroupId, jobId, jobState.getState(), jobState.getStartTime())) {
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
  }

  public TopologyJob addOrUpdateTopologyJob(TopologyJob topologyJob) {
    if (topologyJob == null) {
      throw new RuntimeException("Topology job is null.");
    }

    String sql = "INSERT OR REPLACE INTO job (id, groupId, engineId, config) VALUES (?, ?, ?, ?)";
    int transactionKey = 2;
    synchronized (writeLock) {
      try (PreparedStatement ps = createPreparedStatement(getConnection(transactionKey), sql,
          topologyJob.getId(), topologyJob.getGroupId(),
          topologyJob.getEngineId(), topologyJob.getConfigStr())) {
        int affectedRows;
        affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
          throw new RuntimeException("Failed to insert job.");
        } else {
          addOrUpdateTopologyJobState(topologyJob.getGroupId(), topologyJob.getId(),
              topologyJob.getState());
          commit(transactionKey);
          return topologyJob;
        }
      } catch (SQLException e) {
        rollback(transactionKey);
        throw new RuntimeException(e);
      }
    }
  }

  public TopologyJobGroup addOrUpdateTopologyJobGroup(TopologyJobGroup topologyJobGroup) {
    if (topologyJobGroup == null) {
      throw new RuntimeException("Topology job group is null.");
    }

    String sql = "INSERT OR REPLACE INTO job_group (id, topologyId) VALUES (?, ?)";
    int transactionKey = 4;
    synchronized (writeLock) {
      try (PreparedStatement ps = createPreparedStatement(getConnection(transactionKey), sql,
          topologyJobGroup.getId(), topologyJobGroup.getTopologyId())) {
        int affectedRows;
        affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
          throw new RuntimeException("Failed to insert job group.");
        } else {
          // Add jobs
          for (TopologyJob job : topologyJobGroup.getJobs()) {
            addOrUpdateTopologyJob(job);
          }
          commit(transactionKey);
          return topologyJobGroup;
        }
      } catch (SQLException e) {
        rollback(transactionKey);
        throw new RuntimeException(e);
      }
    }
  }

  public Collection<TopologyJob> listTopologyJob(String jobGroupId) {
    if (jobGroupId == null) {
      throw new RuntimeException("Job group id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.groupId AS groupId, ");
    sb.append("job.engineId AS engineId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.groupId = job.groupId AND ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.groupId = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, jobGroupId);
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
    job.setGroupId(rs.getString("groupId"));
    job.setEngineId(rs.getString("engineId"));
    job.setConfigStr(rs.getString("config"));

    TopologyJobState jobState = new TopologyJobState();
    jobState.setState(rs.getString("state"));
    jobState.setStartTime(rs.getLong("startTime"));

    job.setState(jobState);
    return job;
  }

  public TopologyJobGroup getTopologyJobGroup(Long topologyId) {
    if (topologyId == null) {
      throw new RuntimeException("Topology id is null.");
    }

    String sql = "SELECT id, topologyId FROM job_group WHERE topologyId = ?";
    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, topologyId);
        ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) {
        return null;
      } else {
        TopologyJobGroup group = mapTopologyJobGroup(rs);
        Collection<TopologyJob> jobs = listTopologyJob(group.getId());
        group.addJobs(jobs);
        return group;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private TopologyJobGroup mapTopologyJobGroup(ResultSet rs) throws SQLException {
    TopologyJobGroup group = new TopologyJobGroup();
    group.setId(rs.getString("id"));
    group.setTopologyId(rs.getLong("topologyId"));
    return group;
  }
}
