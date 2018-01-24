package org.edgexfoundry.support.dataprocessing.runtime.db;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class AbstractStorageManagerTest {

  private static File testDB = new File("./abstract_test.db");
  private static SampleTableManager sampleTableManager;

  @BeforeClass
  public static void setup() {
    if (testDB.exists()) {
      throw new RuntimeException(testDB.getAbsolutePath() + " already exists.");
    }
    sampleTableManager = new SampleTableManager("jdbc:sqlite:" + testDB.getAbsolutePath());
  }

  @Test
  public void testExecuteScript() {
    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    sampleTableManager.executeSqlScript(resource);
  }

  @Test
  public void testInvalidExecuteScript() {
    try {
      sampleTableManager.executeSqlScript(null);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testRollback() throws Exception {
    sampleTableManager = spy(sampleTableManager);
    Connection connection = spy(sampleTableManager.getConnection());
    doThrow(new SQLException("Mocked SQL exception")).when(connection).commit();
    doReturn(connection).when(sampleTableManager).getConnection();

    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    try {
      sampleTableManager.executeSqlScript(resource);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("Mocked SQL exception"));
      // success
    }
  }

  @Test
  public void testCreatePreparedStatement() {
    // normal
    String jobId = "abc";
    try (Connection connection = sampleTableManager.getConnection();
        PreparedStatement ps = sampleTableManager
            .createPreparedStatement(connection, "SELECT * FROM job WHERE id=?", jobId)) {
      ps.executeQuery();
    } catch (SQLException e) {
      Assert.fail(e.getMessage());
    }

    // invalid
    try (Connection connection = sampleTableManager.getConnection();
        PreparedStatement ps = sampleTableManager
            .createPreparedStatement(connection, "SELECT * FROM invalid_query WHERE id=?", jobId)) {
      ps.executeQuery();
      Assert.fail("Should not reach here.");
    } catch (SQLException e) {
      // success
    }
  }

  @AfterClass
  public static void cleanup() {
    if (testDB.exists()) {
      testDB.delete();
    }
  }

  private static class SampleTableManager extends AbstractStorageManager {

    public SampleTableManager(String jdbcUrl) {
      super(jdbcUrl);
    }
  }
}
