/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.monitor;

import java.util.Collection;
import java.util.Set;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringManager implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringManager.class);

  private Thread self = null;
  private boolean running = false;

  private final Set<Job> runningJobs;
  private final JobTableManager jobTableManager;
  private final EngineManager engineManager;

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
    this.engineManager = EngineManager.getInstance();
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
    // Read existing job from database
    Collection<Job> allJobs = jobTableManager.getJobs();
    allJobs.forEach(job -> {
      if (job.getState().getState() == State.RUNNING) {
        synchronized (this.runningJobs) {
          this.runningJobs.add(job);
        }
      }
    });

    running = true;
    while (running) {
      try {
        synchronized (this.runningJobs) {
          this.runningJobs.forEach(job -> {
            // get engine
            Engine engine = engineManager
                .getEngine(job.getState().getHost(), job.getState().getPort(), EngineType
                    .valueOf(job.getState().getEngineType()));
            try {
              engine.updateMetrics(job.getState());
            } catch (Exception e) {
              LOGGER.error("Failed to update metric for jobId=" + job.getId());
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
          this.runningJobs.removeIf(job -> job.getState().getState() != State.RUNNING);
        }
      } finally {
        try {
          Thread.sleep(Settings.getInstance().getJobMonitoringInterval());
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
}
