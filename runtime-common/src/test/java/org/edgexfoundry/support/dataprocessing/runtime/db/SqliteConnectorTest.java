package org.edgexfoundry.support.dataprocessing.runtime.db;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sqlite.SQLiteErrorCode;

import java.sql.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberModifier.field;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DriverManager.class, SqliteConnector.class})
public class SqliteConnectorTest {

  @Before
  public void setup(){
     initMocks(this);
  }

  @Test
  public void getInstanceTest() throws SQLException {

    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);
    ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);

    mockStatic(DriverManager.class);

    try {
      when(DriverManager.getConnection(anyString())).thenReturn(connection);
      when(connection.createStatement()).thenReturn(statement);
      when(statement.executeQuery(any())).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);
    }  catch (SQLException e) {
      e.printStackTrace();
      Assert.fail();
    }

    SqliteConnector sqliteConnector = SqliteConnector.getInstance();
    Assert.assertNotNull(sqliteConnector);
  }

  @Test
  public void createTableTest() throws IllegalAccessException {

    SqliteConnector.TableDesc td = new SqliteConnector.TableDesc("test");
    td.addColum("output", new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
    td.addColum("taskinfo", new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));

    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);
    ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);

    mockStatic(DriverManager.class);

    try {
      when(DriverManager.getConnection(anyString())).thenReturn(connection);
      when(connection.createStatement()).thenReturn(statement);
      when(statement.executeQuery(any())).thenReturn(resultSet);
      when(resultSet.getMetaData()).thenReturn(metaData);
      when(metaData.getColumnCount()).thenReturn(0);

      doThrow(new SQLException("", "", SQLiteErrorCode.SQLITE_BUSY.code)).when(statement).executeUpdate(anyString());
    }  catch (SQLException e) {
      e.printStackTrace();
      Assert.fail();
    }

    SqliteConnector sqliteConnector = SqliteConnector.getInstance();
    Assert.assertNotNull(sqliteConnector);

    try {
      sqliteConnector.createTable(td);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void closeConnectionTest() throws IllegalAccessException {


    SqliteConnector sqliteConnector = SqliteConnector.getInstance();
    Assert.assertNotNull(sqliteConnector);


    sqliteConnector.close();
  }
}
