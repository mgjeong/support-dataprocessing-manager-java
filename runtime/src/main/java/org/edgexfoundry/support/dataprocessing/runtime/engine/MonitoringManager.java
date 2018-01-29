package org.edgexfoundry.support.dataprocessing.runtime.engine;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowGroupState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class MonitoringManager implements Runnable {
  static Semaphore semaphore = new Semaphore(1);
  private static long DEFALUT_INTERVAL = 60;
  private static MonitoringManager instance = null;

  private HashMap<Long, HashMap<String, JobState>> workflowStates;

  private Thread thread = null;
  private Boolean isRun = false;
  private long interval = 0;


  private MonitoringManager(long interval) {

    setInterval(interval);
    createThread();

//    workflowStates = new HashMap<>();
    workflowStates = JobTableManager.getInstance().getWorkflowState();
  }

  public static synchronized WorkflowMetric convertWorkflowMetrics(HashMap<Long, HashMap<String, JobState>> healthOfworkflows) {
    ArrayList<WorkflowMetric.GroupInfo> groups = new ArrayList<>();
    WorkflowMetric metrics = new WorkflowMetric();

    long success, finished;

    for (HashMap.Entry<Long, HashMap<String, JobState>> groupEntry : healthOfworkflows.entrySet()) {

      success = 0;
      finished = 0;

      Long workflowId = groupEntry.getKey();
      HashMap<String, JobState> group = groupEntry.getValue();

      for (HashMap.Entry<String, JobState> jobStateEntry : group.entrySet()) {
        JobState jobState = jobStateEntry.getValue();
        JobState.State state = jobState.getState();
        switch (state) {
          case CREATED:
          case RUNNING:
            success++;
            break;
          case STOPPED:
          case ERROR:
            finished++;
            break;
        }
      }

      WorkflowMetric.GroupInfo gInfo = new WorkflowMetric.GroupInfo();
      gInfo.setGroupId(workflowId.toString());
      gInfo.setWorks(new WorkflowMetric.Work(success, finished));
      groups.add(gInfo);
    }

    metrics.setGroups(groups);
    return metrics;
  }

  public static synchronized WorkflowGroupState convertGroupMetrics(String groupId, HashMap<Long, HashMap<String, JobState>> healthOfworkflows) {

    if (healthOfworkflows.containsKey(Long.parseLong(groupId))) {

      WorkflowGroupState groupState = new WorkflowGroupState();

      return groupState.setGroupId(groupId).setJobStates(new ArrayList<>(healthOfworkflows.get(Long.parseLong(groupId)).values()));

    } else {
      return new WorkflowGroupState();
    }
  }

  public synchronized static MonitoringManager getInstance() {
    if (instance == null) {
      instance = getInstance(DEFALUT_INTERVAL);
    }
    return instance;
  }

  public synchronized static MonitoringManager getInstance(long interval) {
    if (instance == null) {
      instance = new MonitoringManager(interval);
    }
    return instance;
  }

  public MonitoringManager setInterval(long interval) {
    this.interval = interval;
    return this;
  }

  private void createThread() {
    thread = new Thread(this);

    enabled();
  }

  public MonitoringManager stop() throws InterruptedException {
    disabled();

    thread.join();

    return this;
  }

  public MonitoringManager start() {
    thread.start();
    return this;
  }

  public MonitoringManager enabled() {
    try {

      semaphore.acquire();
      isRun = true;
    } catch (InterruptedException e) {
      e.printStackTrace();
      isRun = false;
    } finally {
      semaphore.release();
    }
    return this;
  }

  public MonitoringManager disabled() {
    try {

      semaphore.acquire();
      isRun = false;
      thread.join(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      semaphore.release();
    }
    return this;
  }

  @Override
  public void run() {

    HashMap<String, Engine> engines = null;

    while (isRun) {
      try {

        semaphore.acquire();

        if (null == workflowStates) {
          continue;
        }

        for (Map.Entry<Long, HashMap<String, JobState>> workflowStatesEntry : workflowStates.entrySet()) {
          ArrayList<String> removingKeys = new ArrayList<>();
          HashMap<String, JobState> jobStateHashMap = workflowStatesEntry.getValue();
          for (Map.Entry<String, JobState> jobStateEntry : jobStateHashMap.entrySet()) {
            JobState jobState = jobStateEntry.getValue();
            if (null != jobState.getHost()) {
              Engine engine = EngineManager.getEngine(jobState.getHost() + ":" + jobState.getPort(),
                WorkflowData.EngineType.valueOf(jobState.getEngineType()));

              if (engine.updateMetrics(jobState)) {
                // It has been updated.
                JobTableManager.getInstance().updateWorkflowJobState(jobState);
              }
            } else {
              removingKeys.add(jobStateEntry.getKey());
            }
          }

          for (String s : removingKeys) {
            jobStateHashMap.remove(s);
            // TBD :: also will be removed on db.
          }
          removingKeys = null;
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        semaphore.release();
        try {
          Thread.sleep(interval * 1000);  // Seconds.
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public synchronized void addJob(Job job) {

    HashMap<String, JobState> jobStateHashMap;
    long workflowId = job.getWorkflowId();

    if (workflowStates.containsKey(workflowId)) {
      workflowStates.get(workflowId).put(job.getId(), job.getState());
    } else {
      jobStateHashMap = new HashMap<>();
      jobStateHashMap.put(job.getId(), job.getState());

      workflowStates.put(workflowId, jobStateHashMap);
    }
  }

  public synchronized WorkflowMetric getWorkflowsState() {
    return MonitoringManager.convertWorkflowMetrics(workflowStates);
  }

  public synchronized WorkflowGroupState getGroupState(String groupId) {
    return MonitoringManager.convertGroupMetrics(groupId, workflowStates);
  }
}
