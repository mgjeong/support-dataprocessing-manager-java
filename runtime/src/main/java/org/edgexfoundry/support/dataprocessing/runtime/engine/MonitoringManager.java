package org.edgexfoundry.support.dataprocessing.runtime.engine;

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

  private void setInterval(long interval) {
    this.interval = interval;
  }

  private void createThread() {
    enabled();

    thread = new Thread(this);
  }

  public void stop() throws InterruptedException {
    disabled();

    thread.join();
  }

  public void start() {
    thread.run();
  }

  public void enabled() {
    try {

      semaphore.acquire();
      isRun = true;

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      semaphore.release();
    }

  }

  public void disabled() {
    try {

      semaphore.acquire();
      isRun = false;

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      semaphore.release();
    }
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
          try {
            engine.getMetrics();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        Thread.sleep(interval * 1000);  // Seconds.
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        semaphore.release();
      }
    }
  }
}
