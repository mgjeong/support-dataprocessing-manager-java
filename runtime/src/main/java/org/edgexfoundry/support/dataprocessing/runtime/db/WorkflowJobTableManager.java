package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

public class WorkflowJobTableManager extends AbstractStorageManager {

  private static WorkflowJobTableManager instance = null;

  public synchronized static WorkflowJobTableManager getInstance() {
    if (instance == null) {
      instance = new WorkflowJobTableManager(
          "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH,
          Settings.DB_CLASS);
    }
    return instance;
  }

  private WorkflowJobTableManager(String jdbcUrl, String jdbcClass) {
    super(jdbcUrl, jdbcClass);
  }

  public JobState addOrUpdateWorkflowJobState(String jobId, JobState jobState) {
    if (jobId == null || jobState == null) {
      throw new RuntimeException("Job id or job state is null.");
    }

    String sql = "INSERT OR REPLACE INTO job_state (jobId, state, startTime, engineId, engineType) "
        + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        jobId, jobState.getState().name(), jobState.getStartTime(), jobState.getEngineId(),
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

  public Job addOrUpdateWorkflowJob(Job job) {
    if (job == null) {
      throw new RuntimeException("Workflow job is null.");
    }

    String sql = "INSERT OR REPLACE INTO job (id, workflowId, config) VALUES (?, ?, ?)";
    try (PreparedStatement ps = createPreparedStatement(getTransaction(), sql,
        job.getId(), job.getWorkflowId(),
        job.getConfigStr())) {
      int affectedRows;
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("Failed to insert job.");
      } else {
        addOrUpdateWorkflowJobState(job.getId(), job.getState());
        commit();
        return job;
      }
    } catch (SQLException e) {
      rollback();
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
