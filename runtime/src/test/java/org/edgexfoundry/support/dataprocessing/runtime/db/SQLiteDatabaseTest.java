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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

public class SQLiteDatabaseTest extends DatabaseTest {

  private static SQLiteDatabase database = new SQLiteDatabase();

  @BeforeClass
  public static void setupDatabase() {
    database.initialize("jdbc:sqlite:" + testDB.getPath());
  }

  @Test
  public void testConnection() throws Exception {
    try (Connection connection = database.getConnection()) {
      Assert.assertNotNull(connection);
    } catch (SQLException e) {
      throw e;
    }
  }

  @Test
  public void testInvalidConnection() throws Exception {
    SQLiteDatabase temp = new SQLiteDatabase();
    try {
      temp.getConnection();
      Assert.fail("Should not reach here.");
    } catch (SQLException e) {
      // success
    }

    // Mock connection
    SQLiteDataSource mockDataSource = mock(SQLiteDataSource.class);
    when(mockDataSource.getConnection()).thenThrow(new SQLException("Mocked exception"));
    Field dataSource = temp.getClass().getDeclaredField("dataSource");
    dataSource.setAccessible(true);
    dataSource.set(temp, mockDataSource);
    try {
      temp.getConnection();
      Assert.fail("Should not reach here");
    } catch (SQLException e) {
      Assert.assertTrue(e.getMessage().contains("Mocked exception"));
      // success
    }
  }

  @Test
  public void testConcurrency() {
    Thread t1 = new CreateTableSample(database, "T1");
    Thread t2 = new CreateTableSample(database, "T2");
    Thread t3 = new CreateTableSample(database, "T3");
    t1.start();
    t2.start();
    t3.start();
    try {
      t1.join();
      t2.join();
      t3.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  private static class CreateTableSample extends Thread {

    private final SQLiteDatabase database;

    public CreateTableSample(SQLiteDatabase database, String name) {
      super(name);
      this.database = database;
    }

    @Override
    public void run() {
      try (Connection connection = this.database.getConnection()) {
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS sample_table("
            + "`id` INTEGER PRIMARY KEY AUTOINCREMENT)");
        System.out.println(super.getName() + ": sample_table created!");
        statement.close();
        Thread.sleep(500L);
        connection.commit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}