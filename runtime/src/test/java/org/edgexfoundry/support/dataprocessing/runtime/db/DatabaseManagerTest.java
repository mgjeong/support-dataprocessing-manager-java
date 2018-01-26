package org.edgexfoundry.support.dataprocessing.runtime.db;

import org.junit.Assert;
import org.junit.Test;

public class DatabaseManagerTest extends DatabaseTest {

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
}
