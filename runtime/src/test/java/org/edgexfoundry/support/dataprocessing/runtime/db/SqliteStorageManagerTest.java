package org.edgexfoundry.support.dataprocessing.runtime.db;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.junit.Assert;
import org.junit.Test;

public class SqliteStorageManagerTest {

  @Test
  public void testSelectInvalidTopology() throws Exception {
    SqliteStorageManager storageManager = new SqliteStorageManager();
    storageManager.initialize();
    Topology topology = storageManager.getTopology(-1L);
    Assert.assertNull(topology);
  }
}
