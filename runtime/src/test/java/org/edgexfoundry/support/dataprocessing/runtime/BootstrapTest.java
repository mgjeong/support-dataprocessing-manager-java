package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import java.lang.reflect.Field;
import org.edgexfoundry.support.dataprocessing.runtime.db.AbstractStorageManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.DatabaseManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WorkflowTableManager.class)
public class BootstrapTest {

  @Test
  public void testBootstrap() throws Exception {
    File dbFile = new File("./bootstrap_test.db");
    File dbFileWal = new File("./bootstrap_test.db-wal");
    File dbFileShm = new File("./bootstrap_test.db-shm");
    if (dbFile.exists()) {
      throw new RuntimeException(dbFile.getAbsolutePath() + " already exists.");
    }

    try {
      // Setup workflow table manager
      WorkflowTableManager workflowTableManager = WorkflowTableManager.getInstance();
      java.lang.reflect.Field databaseField = AbstractStorageManager.class
          .getDeclaredField("database");
      databaseField.setAccessible(true);
      databaseField.set(workflowTableManager,
          DatabaseManager.getInstance().getDatabase("jdbc:sqlite:" + dbFile.getAbsolutePath()));

      // Setup bootstrap
      Bootstrap bootstrap = new Bootstrap();
      Field wtmField = Bootstrap.class.getDeclaredField("workflowTableManager");
      wtmField.setAccessible(true);
      wtmField.set(bootstrap, workflowTableManager);

      bootstrap.execute(); // insert

      // run twice to update
      bootstrap.execute(); // update
    } finally {
      if (dbFile.exists()) {
        dbFile.delete();
      }
      if (dbFileWal.exists()) {
        dbFileWal.delete();
      }
      if (dbFileShm.exists()) {
        dbFileShm.delete();
      }
    }
  }
}
