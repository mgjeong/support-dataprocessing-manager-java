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
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowGroupState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric;

public class JobTableManager extends AbstractStorageManager {

  private static JobTableManager instance = null;

  public static synchronized JobTableManager getInstance() {
    if (instance == null) {
      instance = new JobTableManager(Settings.JDBC_PATH);
    }
    return instance;
  }

  private JobTableManager(String jdbcUrl) {
    super(jdbcUrl);
  }

  public JobState updateWorkflowJobState(JobState jobState) {

    String sql = "UPDATE job_state SET state=?, finishTime=?, errorMessage=? where engineId=?";
    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql,
        jobState.getState().name(), jobState.getFinishTime(), jobState.getErrorMessage(),
        jobState.getEngineId())) {

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

  public synchronized WorkflowMetric getWorkflowState() {
    String sql = "select  jobid, (select count(*) from job_state where jobid=a.jobid and state='RUNNING' ) as RUNNING, " +
      "(select count(*) from job_state where jobid=a.jobid and (state='ERROR'  or state='STOP')) as 'STOPPED' " +
      "from (select * from job_state group by jobid ) a where jobid=a.jobid";

    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql)) {

      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        ResultSet rs = ps.executeQuery();
        connection.commit();
        WorkflowMetric metrics = mapToworkflowMetric(rs);
        rs.close();
        return metrics;
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

  public synchronized WorkflowGroupState getWorkflowState(String jobId) {
    String sql = " select * from job_state group by 'jobId'=?;";

    try (Connection connection = getConnection();
        PreparedStatement ps = createPreparedStatement(connection, sql,
          jobId)) {

      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        ResultSet rs = ps.executeQuery();
        connection.commit();
        WorkflowGroupState metrics = mapToworkflowJobMetric(rs);
        rs.close();
        return metrics;
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

  public synchronized JobState addOrUpdateWorkflowJobState(String jobId, JobState jobState) {
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
          throw new SQLException("Failed to insert job state.");
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

  public synchronized Job addOrUpdateWorkflowJob(Job job) {
    if (job == null) {
      throw new RuntimeException("Workflow job is null.");
    }

    String sqlJob = "INSERT OR REPLACE INTO job (id, workflowId, config) VALUES (?, ?, ?)";
    String sqlJobState = "INSERT OR REPLACE INTO job_state "
        + "(jobId, state, startTime, engineId, engineType) "
        + "VALUES (?, ?, ?, ?, ?)";
    try (Connection connection = getConnection();
        PreparedStatement psJob = createPreparedStatement(connection, sqlJob,
            job.getId(), job.getWorkflowId(), job.getConfigStr())) {
      boolean oldState = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        int affectedRows;
        affectedRows = psJob.executeUpdate();
        if (affectedRows == 0) {
          throw new SQLException("Failed to insert job.");
        }

        // Add state
        try (PreparedStatement psJobState = createPreparedStatement(connection,
            sqlJobState, job.getId(), job.getState().getState(), job.getState().getStartTime(),
            job.getState().getEngineId(), job.getState().getEngineType())) {
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

  private WorkflowMetric mapToworkflowMetric(ResultSet rs) throws SQLException {
    WorkflowMetric metrics = new WorkflowMetric();

    ArrayList<WorkflowMetric.GroupInfo> groupInfos = new ArrayList<>();

    while(rs.next()) {

      WorkflowMetric.Work work = new WorkflowMetric.Work();
      work.setRunning(rs.getLong("RUNNING"));
      work.setStop(rs.getLong("STOPPED"));

      WorkflowMetric.GroupInfo gInfo = new WorkflowMetric.GroupInfo();
      gInfo.setGroupId(rs.getString("jobId"));
      gInfo.setWorks(work);

      groupInfos.add(gInfo);
    }

    metrics.setGroups(groupInfos);

    return metrics;
  }

  private WorkflowGroupState mapToworkflowJobMetric(ResultSet rs) throws SQLException {

    WorkflowGroupState groupState = new WorkflowGroupState();
    ArrayList<JobState> jobStates = new ArrayList<>();

    while(rs.next()) {
      JobState jobState = new JobState();

      jobState.setEngineId(rs.getString("engineId"));
      jobState.setState(rs.getString("state"));
      jobState.setStartTime(rs.getLong("startTime"));
      jobState.setFinishTime(rs.getLong("finishTime"));
      jobState.setErrorMessage(rs.getString("errorMessage"));
      jobState.setEngineType(rs.getString("engineType"));

      jobStates.add(jobState);
    }
    groupState.setJobStates(jobStates);
    return groupState;
  }

  public Job getWorkflowJob(String jobId) {
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
    sb.append("job_state.engineId AS engineId, ");
    sb.append("job_state.engineType AS engineType ");
    sb.append("FROM job, job_state WHERE ");
    sb.append("job_state.jobId = job.id AND ");
    sb.append("job.id = ?");
    String sql = sb.toString();

    try (PreparedStatement ps = createPreparedStatement(getConnection(), sql, jobId);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return mapToWorkflowJob(rs);
      } else {
        throw new SQLException("Workflow job not found.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void removeWorkflowJob(String jobId) {
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
