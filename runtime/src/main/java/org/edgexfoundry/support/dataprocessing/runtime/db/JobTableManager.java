package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

public class JobTableManager extends AbstractStorageManager {

  private static JobTableManager instance = null;

  private JobTableManager(String jdbcUrl) {
    super(jdbcUrl);
  }

  public static synchronized JobTableManager getInstance() {
    if (instance == null) {
      instance = new JobTableManager(Settings.JDBC_PATH);
    }
    return instance;
  }

  public synchronized JobState updateJobState(JobState jobState) {
    String sql = "UPDATE job_state SET "
        + "state=?, startTime=?, finishTime=?, errorMessage=?, "
        + "engineId=?, engineType=?, engineHost=?, enginePort=? where jobId=?";
    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql, jobState.getState().name(),
            jobState.getStartTime(), jobState.getFinishTime(), jobState.getErrorMessage(),
            jobState.getEngineId(), jobState.getEngineType(), jobState.getHost(),
            jobState.getPort(), jobState.getJobId())) {

      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        int affectedRows;
        affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
          throw new RuntimeException("Failed to update job state.");
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

  public synchronized Job addJob(Job job) {
    if (job == null) {
      throw new RuntimeException("Job is null.");
    }

    String sqlJob = "INSERT INTO job (id, workflowId, config) VALUES (?, ?, ?)";
    try (Connection connection = getConnection();
        PreparedStatement psJob = createPreparedStatement(connection, sqlJob, job.getId(),
            job.getWorkflowId(), job.getConfigStr())) {
      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        int affectedRows;
        affectedRows = psJob.executeUpdate();
        if (affectedRows == 0) {
          throw new SQLException("Failed to insert job.");
        }

        // Add state
        String sqlJobState =
            "INSERT INTO job_state "
                + "(jobId, state, startTime, finishTime, errorMessage, engineId, engineType, engineHost, enginePort) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psJobState = createPreparedStatement(connection, sqlJobState,
            job.getId(), job.getState().getState().name(), job.getState().getStartTime(),
            job.getState().getFinishTime(), job.getState().getErrorMessage(),
            job.getState().getEngineId(), job.getState().getEngineType(), job.getState().getHost(),
            job.getState().getPort())) {
          affectedRows = psJobState.executeUpdate();
          if (affectedRows == 0) {
            throw new SQLException("Failed to insert job state.");
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

  private Job mapToJob(ResultSet rs) throws SQLException {
    Job job = new Job(rs.getString("id"), rs.getLong("workflowId"));
    job.setConfigStr(rs.getString("config"));

    JobState jobState = job.getState();
    jobState.setState(rs.getString("state"));
    jobState.setStartTime(rs.getLong("startTime"));
    jobState.setFinishTime(rs.getLong("finishTime"));
    jobState.setErrorMessage(rs.getString("errorMessage"));
    jobState.setEngineId(rs.getString("engineId"));
    jobState.setEngineType(rs.getString("engineType"));
    jobState.setHost(rs.getString("engineHost"));
    jobState.setPort(rs.getInt("enginePort"));

    return job;
  }

  public Collection<Job> getJobs() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.workflowId AS workflowId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.finishTime AS finishTime, ");
    sb.append("job_state.errorMessage AS errorMessage, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType, ");
    sb.append("job_state.engineHost AS engineHost, ");
    sb.append("job_state.enginePort AS enginePort ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id ");
    String sql = sb.toString();

    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql);
        ResultSet rs = ps.executeQuery()) {
      List<Job> jobs = new ArrayList<>();
      while (rs.next()) {
        jobs.add(mapToJob(rs));
      }
      return jobs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<Job> getJobsByWorkflow(Long workflowId) {
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
    sb.append("job_state.finishTime AS finishTime, ");
    sb.append("job_state.errorMessage AS errorMessage, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType, ");
    sb.append("job_state.engineHost AS engineHost, ");
    sb.append("job_state.enginePort AS enginePort ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.workflowId = ?");
    String sql = sb.toString();

    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql, workflowId);
        ResultSet rs = ps.executeQuery()) {
      List<Job> jobs = new ArrayList<>();
      while (rs.next()) {
        jobs.add(mapToJob(rs));
      }
      return jobs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Job getJobById(String jobId) {
    if (jobId == null) {
      throw new RuntimeException("Workflow job id is null.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append("job.id AS id, ");
    sb.append("job.workflowId AS workflowId, ");
    sb.append("job.config AS config, ");
    sb.append("job_state.state AS state, ");
    sb.append("job_state.startTime AS startTime, ");
    sb.append("job_state.finishTime AS finishTime, ");
    sb.append("job_state.errorMessage AS errorMessage, ");
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType, ");
    sb.append("job_state.engineHost AS engineHost, ");
    sb.append("job_state.enginePort AS enginePort ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.id = ?");
    String sql = sb.toString();

    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql, jobId);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return mapToJob(rs);
      } else {
        throw new SQLException("Workflow job not found.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void removeJob(String jobId) {
    if (jobId == null) {
      throw new RuntimeException("Workflow job id is null.");
    }

    String sql = "DELETE FROM job WHERE id = ?";
    String sqlState = "DELETE FROM job_state WHERE jobId = ?";
    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql, jobId);
        PreparedStatement psState = createPreparedStatement(connection, sqlState, jobId)) {
      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        ps.executeUpdate();
        psState.executeUpdate();
        connection.commit();
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
}
