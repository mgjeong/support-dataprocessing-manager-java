package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class DatabaseTest {

  protected static File testDB = null;
  protected static File testDBwal = null;
  protected static File testDBshm = null;

  @BeforeClass
  public static void createFiles() {
    String randomDatabaseFile = "./test_" + System.currentTimeMillis();
    testDB = new File(randomDatabaseFile + ".db");
    testDBwal = new File(randomDatabaseFile + ".db-wal");
    testDBshm = new File(randomDatabaseFile + ".db-shm");

    if (testDB.exists() && !testDB.delete()) {
      throw new RuntimeException(testDB.getAbsolutePath() + " already exists.");
    } else if (testDBwal.exists() && !testDBwal.delete()) {
      throw new RuntimeException(testDBwal.getAbsolutePath() + " already exists.");
    } else if (testDBshm.exists() && !testDBshm.delete()) {
      throw new RuntimeException(testDBshm.getAbsolutePath() + " already exists.");
    }
  }

  @AfterClass
  public static void deleteFiles() {
    if (testDB.exists()) {
      testDB.delete();
    }
    if (testDBwal.exists()) {
      testDBwal.delete();
    }
    if (testDBshm.exists()) {
      testDBshm.delete();
    }
  }
}
