package org.edgexfoundry.support.dataprocessing.runtime.monitor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EngineManager.class})
public class MonitoringManagerTest {

  private static MonitoringManager monitoringManager;
  private static Engine flinkEngine;
  private static JobTableManager jobTableManager;

  @BeforeClass
  public static void initialize() {
    monitoringManager = MonitoringManager.getInstance();
    flinkEngine = mock(FlinkEngine.class);
    EngineManager engineManager = spy(EngineManager.getInstance());
    doReturn(flinkEngine).when(engineManager).getEngine(anyString(), anyInt(), any());

    jobTableManager = mock(JobTableManager.class);
    List<Job> jobs = new ArrayList<>();
    Job job = new Job("jobId", 1L);
    job.getState().setState(State.RUNNING);
    jobs.add(job);
    doReturn(jobs).when(jobTableManager).getJobs();

    WhiteboxImpl.setInternalState(monitoringManager, "jobTableManager", jobTableManager);
    WhiteboxImpl.setInternalState(monitoringManager, "engineManager", engineManager);
  }

  @Test
  public void testAddJob() {
    Job job = Job.create("jobId", 1L);
    monitoringManager.addJob(job);

    job.getState().setState(State.RUNNING);
    monitoringManager.addJob(job);

    monitoringManager.addJob(null);
  }

  @Test
  public void testRemoveJob() {
    Job job = Job.create("jobId", 1L);
    monitoringManager.removeJob(job);

    job.getState().setState(State.RUNNING);
    monitoringManager.addJob(job);
    monitoringManager.removeJob(job);

    monitoringManager.removeJob(null);
  }

  @Test
  public void testValidThread() throws Exception {
    try {

      // start thread
      monitoringManager.startMonitoring();

      // create sample job
      Job job = Job.create("jobId", 1L);
      job.getState().setState(State.RUNNING);
      job.getState().setEngineType("FLINK");
      job.getState().setHost("localhost");
      job.getState().setPort(8081);
      monitoringManager.addJob(job);

      doThrow(new Exception("Mocked")).when(flinkEngine).updateMetrics(any());

      // let the thread run
      System.out.println("Wait 3 seconds until monitoring thread starts");
      Thread.sleep(3000); // wait 3 seconds
    } finally {
      monitoringManager.terminate();
    }
  }

  @Test
  public void testInvalidThread() throws InterruptedException {
    try {
      monitoringManager.startMonitoring();
      monitoringManager.startMonitoring(); // this should not create another thread

    } finally {
      monitoringManager.terminate();
      monitoringManager.terminate(); // this should not interrupt thread twice
    }
  }
}
