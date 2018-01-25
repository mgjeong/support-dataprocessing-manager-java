package org.edgexfoundry.support.dataprocessing.runtime.engine;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
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

    workflowStates = JobTableManager.getInstance().getWorkflowState();

  }

  public static WorkflowMetric convertWorkflowMetrics(HashMap<Long, HashMap<String, JobState>> healthOfworkflows) {

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

  public static WorkflowGroupState convertGroupMetrics(String groupId, HashMap<Long, HashMap<String, JobState>> healthOfworkflows) {

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
    enabled();

    thread = new Thread(this);
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
    } finally {
      semaphore.release();
    }
    return this;
  }

  public MonitoringManager disabled() {
    try {

      semaphore.acquire();
      isRun = false;

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

        engines = EngineManager.getEngineList();

        for (Map.Entry<String, Engine> entry : engines.entrySet()) {
          Engine engine = entry.getValue();
          ArrayList<JobState> jobStates = engine.getMetrics();

          for (JobState state : jobStates) {
            try {
              JobTableManager.getInstance().updateWorkflowJobState(state);
//              JobTableManager jobTableManager = new JobTableManager(
//                      "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH);
//              jobTableManager.updateWorkflowJobState(state);

              // It should be updated // TEMPORARY CODE for supporting test.
              for (HashMap.Entry<Long, HashMap<String, JobState>> groupEntry : workflowStates.entrySet()) {
                HashMap<String, JobState> jobStateHashMap = groupEntry.getValue();
                for (HashMap.Entry<String, JobState> jobStateEntry : jobStateHashMap.entrySet()) {
                  if (jobStateEntry.getValue().getEngineId().compareTo(state.getEngineId()) == 0) {
                    jobStateEntry.getValue().setHost(engine.getHost()).setPort(engine.getPort());
                  }
                }
              }
              //
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }

        Thread.sleep(interval * 1000);  // Seconds.
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        semaphore.release();
      }
    }
  }

  public WorkflowMetric getWorkflowsState() {

    // WILL BE REMOVED when USING CACHING : START
    workflowStates = JobTableManager.getInstance().getWorkflowState();
    // WILL BE REMOVED when USING CACHING : END

    return MonitoringManager.convertWorkflowMetrics(workflowStates);
  }

  public WorkflowGroupState getGroupState(String groupId) {
    // WILL BE REMOVED when USING CACHING : START
    workflowStates = JobTableManager.getInstance().getWorkflowState();
    // WILL BE REMOVED when USING CACHING : END

    return MonitoringManager.convertGroupMetrics(groupId, workflowStates);
  }
}
