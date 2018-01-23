package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BootstrapTest {

  private static WorkflowTableManager workflowTableManager;

  private static File dbFile = new File("./" + Settings.DB_TEST_PATH);

  @BeforeClass
  public static void initialize() {
    if (dbFile.exists()) {
      throw new RuntimeException(dbFile.getAbsolutePath() + " already exists.");
    }
    //workflowTableManager = WorkflowTableManager.getInstance();
    //workflowTableManager.initialize("jdbc:sqlite:" + dbFile.getAbsolutePath());
    workflowTableManager = new WorkflowTableManager("jdbc:sqlite:" + dbFile.getAbsolutePath());
  }

  @Test
  public void testBootstrap() throws Exception {
    Bootstrap bootstrap = new Bootstrap("jdbc:sqlite:" + dbFile.getAbsolutePath());
    bootstrap.execute(); // insert

    // run twice to update
    bootstrap.execute(); // update
  }

  @AfterClass
  public static void terminate() {
    if (dbFile.exists()) {
      dbFile.delete();
    }
  }
}
