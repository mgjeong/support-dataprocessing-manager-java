package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.junit.Assert;
import org.junit.Test;

public class JobStateTest {

  @Test
  public void testSetterAndGetter() {
    long now = System.currentTimeMillis();

    JobState state = new JobState("jobId");
    state.setState(State.RUNNING);
    state.setStartTime(now);
    state.setEngineId("engineId");
    state.setEngineType("FLINK");
    state.setErrorMessage(null);
    state.setHost("localhost");
    state.setPort(1234);
    state.setFinishTime(System.currentTimeMillis());

    Assert.assertEquals(State.RUNNING, state.getState());
    Assert.assertEquals(now, state.getStartTime().longValue());
    Assert.assertEquals("engineId", state.getEngineId());
    Assert.assertEquals("FLINK", state.getEngineType());
    Assert.assertNull(state.getErrorMessage());
    Assert.assertEquals("localhost", state.getHost());
    Assert.assertEquals(1234, state.getPort());
    Assert.assertNotNull(state.getFinishTime());
    Assert.assertEquals("jobId", state.getJobId());

    state.setState("STOPPED");
    Assert.assertEquals(State.STOPPED, state.getState());
  }

  @Test
  public void testInvalidSetter() {
    JobState state = new JobState("");
    try {
      state.setState("invalidstate");
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    try {
      String n = null;
      state.setState(n);
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }
  }

}
