package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.io.File;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatabaseManagerTest {

  private static File testDB = new File("./db_manager_test.db");

  @BeforeClass
  public static void setup() {
    if (testDB.exists()) {
      throw new RuntimeException(testDB.getAbsolutePath() + " already exists.");
    }
  }

  @Test
  public void testInvalidHost() {
    try {
      DatabaseManager.getInstance().getDatabase(null);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void validHost() {
    SQLiteDatabase database = DatabaseManager.getInstance()
        .getDatabase("jdbc:sqlite:" + testDB.getPath());
    Assert.assertNotNull(database);
  }

  @AfterClass
  public static void cleanup() {
    if (testDB.exists()) {
      testDB.delete();
    }
  }
}
