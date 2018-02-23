/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
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

public class AbstractStorageManagerTest extends DatabaseTest{
  private static SampleTableManager sampleTableManager;

  @BeforeClass
  public static void setup() {
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

  private static class SampleTableManager extends AbstractStorageManager {

    public SampleTableManager(String jdbcUrl) {
      super(jdbcUrl);
    }
  }
}
