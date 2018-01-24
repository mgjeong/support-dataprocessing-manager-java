package org.edgexfoundry.support.dataprocessing.runtime.engine;

import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class MonitoringManager implements Runnable {
  private static long DEFALUT_INTERVAL = 60;

  private Thread thread = null;
  private Boolean isRun = false;
  private long interval = 0;

  static Semaphore semaphore = new Semaphore(1);

  private static MonitoringManager instance = null;


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

  private MonitoringManager(long interval) {

    setInterval(interval);
    createThread();

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

    HashMap<String, Engine> engines  = null;

    while(isRun) {
      try {

        semaphore.acquire();

        engines = EngineManager.getEngineList();

        for(Map.Entry<String, Engine> entry : engines.entrySet()) {
          Engine engine = entry.getValue();
          ArrayList<JobState> jobStates = engine.getMetrics();

          for(JobState state : jobStates) {
            try {
//              JobTableManager.getInstance().updateWorkflowJobState(state);
              JobTableManager jobTableManager = new JobTableManager(
                      "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH);
              jobTableManager.updateWorkflowJobState(state);
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
}
