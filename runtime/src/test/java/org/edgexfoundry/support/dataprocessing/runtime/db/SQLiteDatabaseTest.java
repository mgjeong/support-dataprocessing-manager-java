package org.edgexfoundry.support.dataprocessing.runtime.db;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

public class SQLiteDatabaseTest {

  private static SQLiteDatabase database = new SQLiteDatabase();
  private static File databaseFile = new File("./test.db");

  @BeforeClass
  public static void setupDatabase() {
    database.initialize("jdbc:sqlite:" + databaseFile.getPath());
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
    } catch (RuntimeException e) {
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
    } catch (RuntimeException e) {
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

  @AfterClass
  public static void cleanup() {
    if (databaseFile.exists()) {
      databaseFile.delete();
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