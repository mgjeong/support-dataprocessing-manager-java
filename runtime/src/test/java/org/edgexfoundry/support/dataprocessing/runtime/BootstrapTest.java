package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BootstrapTest {

  private static WorkflowTableManager workflowTableManager;

  @BeforeClass
  public static void initialize() {
    File dbFile = new File("./" + Settings.DB_TEST_PATH);
    dbFile.deleteOnExit();
    workflowTableManager = WorkflowTableManager.getInstance();
    workflowTableManager.initialize("jdbc:sqlite:" + dbFile.getAbsolutePath(), Settings.DB_CLASS);
  }

  @Test
  public void testBootstrap() throws Exception {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.execute(); // insert

    // run twice to update
    bootstrap.execute(); // update
    bootstrap.terminate();
  }

  @AfterClass
  public static void terminate() {
    workflowTableManager.terminate();
  }
}
