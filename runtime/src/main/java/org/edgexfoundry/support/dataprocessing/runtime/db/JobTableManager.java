package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

public class JobTableManager extends AbstractStorageManager {

  private static JobTableManager instance = null;

  public synchronized static JobTableManager getInstance() {
    if (instance == null) {
      instance = new JobTableManager("jdbc:sqlite:"
          + Settings.DOCKER_PATH + Settings.DB_PATH);
    }
    return instance;
  }

  private JobTableManager(String jdbcUrl) {
    super(jdbcUrl);
  }

  public JobState addOrUpdateWorkflowJobState(String jobId, JobState jobState) {
    if (jobId == null || jobState == null) {
      throw new RuntimeException("Job id or job state is null.");
    }

    String sql = "INSERT OR REPLACE INTO job_state (jobId, state, startTime, engineId, engineType) "
        + "VALUES (?, ?, ?, ?, ?)";
    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql,
            jobId, jobState.getState().name(), jobState.getStartTime(), jobState.getEngineId(),
            jobState.getEngineType())) {
      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        int affectedRows;
        affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
          throw new RuntimeException("Failed to insert job state.");
        } else {
          connection.commit();
          return jobState;
        }
      } catch (SQLException e) {
        connection.rollback();
        throw e;
      } finally {
        connection.setAutoCommit(oldState);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Job addOrUpdateWorkflowJob(Job job) {
    if (job == null) {
      throw new RuntimeException("Workflow job is null.");
    }

    String sqlJob = "INSERT OR REPLACE INTO job (id, workflowId, config) VALUES (?, ?, ?)";
    String sqlJobState = "INSERT OR REPLACE INTO job_state "
        + "(jobId, state, startTime, engineId, engineType) "
        + "VALUES (?, ?, ?, ?, ?)";
    try (
        Connection connection = getConnection();
        PreparedStatement psJob = createPreparedStatement(connection, sqlJob,
            job.getId(), job.getWorkflowId(), job.getConfigStr())) {
      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        int affectedRows;
        affectedRows = psJob.executeUpdate();
        if (affectedRows == 0) {
          throw new RuntimeException("Failed to insert job.");
        }

        // Add state
        try (PreparedStatement psJobState = createPreparedStatement(connection,
            sqlJobState, job.getId(), job.getState().getState(), job.getState().getStartTime(),
            job.getState().getEngineId(), job.getState().getEngineType())) {
          affectedRows = psJobState.executeUpdate();
          if (affectedRows == 0) {
            throw new RuntimeException("Failed to insert job state.");
          }
        }

        // Commit change
        connection.commit();
        return job;
      } catch (SQLException e) {
        connection.rollback();
        throw e;
      } finally {
        connection.setAutoCommit(oldState);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<Job> listWorkflowJobs(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Workflow id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.workflowId AS workflowId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.workflowId = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId);
        ResultSet rs = ps.executeQuery()) {
      Collection<Job> jobs = new ArrayList<>();
      while (rs.next()) {
        jobs.add(mapToWorkflowJob(rs));
      }
      return jobs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Job mapToWorkflowJob(ResultSet rs) throws SQLException {
    Job job = new Job();
    job.setId(rs.getString("id"));
    job.setWorkflowId(rs.getLong("workflowId"));
    job.setConfigStr(rs.getString("config"));

    JobState jobState = new JobState();
    jobState.setState(rs.getString("state"));
    jobState.setStartTime(rs.getLong("startTime"));
    jobState.setEngineId(rs.getString("engineId"));
    jobState.setEngineType(rs.getString("engineType"));
    job.setState(jobState);

    return job;
  }

  public Job getWorkflowJob(Long workflowId, String jobId) {
    if (workflowId == null || jobId == null) {
      throw new RuntimeException("Workflow id or job id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.workflowId AS workflowId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.workflowId = ? AND job.id = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, workflowId, jobId);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return mapToWorkflowJob(rs);
      } else {
        throw new RuntimeException("Workflow job not found.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
