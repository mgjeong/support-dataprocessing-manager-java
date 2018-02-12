package org.edgexfoundry.support.dataprocessing.runtime.engine;

import java.util.Set;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringManager implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringManager.class);

  private Thread self = null;
  private boolean running = false;

  private final Set<Job> runningJobs;
  private final JobTableManager jobTableManager;

  private static MonitoringManager instance = null;

  public synchronized static MonitoringManager getInstance() {
    if (instance == null) {
      instance = new MonitoringManager();
    }
    return instance;
  }

  private MonitoringManager() {
    this.runningJobs = new ConcurrentHashSet<>();
    this.jobTableManager = JobTableManager.getInstance();
  }

  public synchronized void addJob(Job job) {
    if (job == null || job.getState() == null || job.getState().getState() != State.RUNNING) {
      return;
    }

    this.runningJobs.add(job);
  }

  public synchronized void removeJob(Job job) {
    if (job == null || job.getState() == null) {
      return;
    }

    this.runningJobs.removeIf(existingJob -> existingJob.getId().equalsIgnoreCase(job.getId()));
  }

  @Override
  public void run() {
    running = true;
    while (running) {
      try {
        synchronized (runningJobs) {
          runningJobs.forEach(job -> {
            // get engine
            Engine engine = EngineManager.getInstance()
                .getEngine(job.getState().getHost(), job.getState().getPort(), EngineType
                    .valueOf(job.getState().getEngineType()));
            try {
              engine.updateMetrics(job.getState());
            } catch (Exception e) {
              LOGGER.error(e.getMessage(), e);
              // Something went wrong. Job is probably dead.
              job.getState().setState(State.ERROR);
              job.getState().setErrorMessage(e.getMessage());
            }

            // update to database
            if (job.getState().getState() != State.RUNNING) {
              this.jobTableManager.updateJobState(job.getState());
            }
          });

          // remove jobs that are no longer running
          runningJobs.removeIf(job -> job.getState().getState() != State.RUNNING);
        }
      } finally {
        try {
          Thread.sleep(Settings.JOB_MONITORING_INTERVAL);
        } catch (InterruptedException e) {
          running = false;
        }
      }
    }
  }

  public synchronized void terminate() {
    running = false;
    if (this.self != null) {
      this.self.interrupt();
      this.self = null;
    }
  }

  public synchronized void startMonitoring() {
    if (this.self != null) {
      // Thread is already running.
      LOGGER.error("Monitoring thread is already running.");
      return;
    }
    this.self = new Thread(this);
    this.self.start();
  }

  /*
  public static final long INTERVAL = 5;

  static Semaphore semaphore = new Semaphore(1);
  private static MonitoringManager instance = null;

  private HashMap<Long, HashMap<String, JobState>> workflowStates;

  private Thread thread = null;
  private Boolean isRun = false;
  private long interval = 0;


  private MonitoringManager(long interval) {

    setInterval(interval);
    createThread();

    workflowStates = JobTableManager.getInstance().getWorkflowJobMetric();
  }

  public static synchronized WorkflowMetric.Work getCountWorks(HashMap<String, JobState> group) {

    long success, finished;

    success = 0;
    finished = 0;

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
    return new WorkflowMetric.Work(success, finished);
  }

  public static synchronized WorkflowMetric convertWorkflowMetrics(
      HashMap<Long, HashMap<String, JobState>> healthOfworkflows) {
    ArrayList<WorkflowMetric.GroupInfo> groups = new ArrayList<>();
    WorkflowMetric metrics = new WorkflowMetric();

    for (HashMap.Entry<Long, HashMap<String, JobState>> groupEntry : healthOfworkflows.entrySet()) {
      Long workflowId = groupEntry.getKey();
      HashMap<String, JobState> group = groupEntry.getValue();

      WorkflowMetric.GroupInfo gInfo = new WorkflowMetric.GroupInfo();
      gInfo.setGroupId(workflowId.toString());
      gInfo.setWorks(getCountWorks(group));
      groups.add(gInfo);
    }

    metrics.setGroups(groups);
    return metrics;
  }

  public static synchronized WorkflowJobMetric convertGroupMetrics(String groupId, HashMap<Long,
      HashMap<String, JobState>> healthOfworkflows) {

    if (healthOfworkflows.containsKey(Long.parseLong(groupId))) {

      WorkflowJobMetric groupState = new WorkflowJobMetric();

      return groupState.setGroupId(groupId)
          .setJobStates(new ArrayList<>(healthOfworkflows.get(Long.parseLong(groupId)).values()));

    } else {
      return new WorkflowJobMetric();
    }
  }

  public synchronized static MonitoringManager getInstance() {
    if (instance == null) {
      instance = getInstance(MonitoringManager.INTERVAL);
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

        for (Map.Entry<Long, HashMap<String, JobState>> workflowStatesEntry : workflowStates
            .entrySet()) {
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

  public synchronized WorkflowMetric.GroupInfo getGroupState(String groupId) {
    WorkflowMetric.GroupInfo groupInfo = new WorkflowMetric.GroupInfo();
    groupInfo.setGroupId(groupId);
    groupInfo
        .setWorks(MonitoringManager.getCountWorks(workflowStates.get(Long.parseLong(groupId))));
    return groupInfo;
  }

  public synchronized WorkflowJobMetric getGroupDetails(String groupId) {
    return MonitoringManager.convertGroupMetrics(groupId, workflowStates);
  }
  */
}
