package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import org.junit.Test;

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
      Bootstrap bootstrap = new Bootstrap("jdbc:sqlite:" + dbFile.getAbsolutePath());
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
