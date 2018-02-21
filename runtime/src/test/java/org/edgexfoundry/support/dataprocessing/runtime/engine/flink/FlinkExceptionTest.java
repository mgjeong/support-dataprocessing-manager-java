package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import org.junit.Assert;
import org.junit.Test;

public class FlinkExceptionTest {

  @Test
  public void testGetterAndSetter() {
    FlinkException exception = new FlinkException();
    exception.setRootException("rootexception");
    Assert.assertEquals("rootexception", exception.getRootException());
  }
}
