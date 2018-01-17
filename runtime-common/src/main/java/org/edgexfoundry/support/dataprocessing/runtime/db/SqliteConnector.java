
/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sqlite.SQLiteErrorCode;

@Deprecated
public final class SqliteConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(SqliteConnector.class);

  private static SqliteConnector instance = null;

  private Object updateLock = new Object();

  private Connection conn = null;

  private SqliteConnector() {
  }

  /**
  *
  * @return
  */
  public static synchronized SqliteConnector getInstance() {
    if (instance == null) {
      instance = new SqliteConnector();
      instance.connect();
    }
    return instance;
  }

  protected void connect() {
    if (conn != null) {
      return;
    }

    try {
      // db parameters
      String url = "jdbc:sqlite:" + Settings.DOCKER_PATH + Settings.DB_PATH;
      Class.forName("org.sqlite.JDBC");

      // create a connection to the database
      conn = DriverManager.getConnection(url);

      // set WAL
      executeSelect("PRAGMA journal_mode=WAL");

      // set auto-commit to false
      conn.setAutoCommit(false);

      LOGGER.info("Connection to SQLite has been established.");
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public synchronized void close() {
    try {
      if (conn != null) {
        conn.rollback(); // rollback, if any
        conn.close();
        conn = null; // explicitly garbage collect conn
        instance = null;
      }
    } catch (SQLException ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }

  public void createTable(TableDesc desc) throws Exception {
    executeUpdate(desc.toQuery());
  }

  public void executeUpdate(String query) throws SQLException {
    synchronized (updateLock) {
      Statement stmt = null;
      try {
        boolean executed = false;
        int retryCount = 0;
        try {
          while (!executed && retryCount < 5) {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            conn.commit();
            executed = true;
          }
        } catch (SQLException e) {
          if (conn != null) {
            conn.rollback();
          }

          if (e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code
              && retryCount < 5) {
            long randomSleepInterval = (long) (Math.random() * 500L);
            LOGGER.error("SQLite database is busy. Retrying in "
                + randomSleepInterval + "ms. (" + retryCount + "/5)");
            try {
              Thread.sleep(randomSleepInterval);
            } catch (InterruptedException e1) {
            }

            retryCount++;
          } else {
            throw e;
          }
        }

      } finally {
        if (stmt != null) {
          try {
            stmt.close();
          } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
          }
        }
      }
    }
  }

  public List<Map<String, String>> executeSelect(String query) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery(query);

      // convert rs to list<map<>>
      List<Map<String, String>> retMapList = new ArrayList<>();
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnsNumber = rsmd.getColumnCount();

      while (rs.next()) {
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i <= columnsNumber; i++) {
          map.put(rsmd.getColumnName(i), rs.getString(i));
        }
        retMapList.add(map);
      }

      return retMapList;
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    }
  }

  public static class TableDesc {
    private String tableName = null;
    private String primaryKeys = null;
    private HashMap<String, ColumnDesc> colums = null;

    public TableDesc(String tableName) {
      colums = new HashMap<String, ColumnDesc>();
      this.tableName = tableName;
    }

    public void setPrimaryKeys(String keys) {
      primaryKeys = keys;
    }

    public TableDesc addColum(String name, ColumnDesc value) {
      colums.put(name, value);
      return this;
    }

//    CREATE TABLE database_name.table_name(
//    column1 datatype PRIMARY KEY(one or more columns),
//    column2 datatype,
//    column3 datatype,
//    .....
//    columnN datatype,
//    );

    public String toQuery() throws Exception {

      if (null == tableName) {
        throw new Exception("Doesn't have table name.");
      }

      String query = null;
      query = "CREATE TABLE IF NOT EXISTS " + tableName + '(';
      try {
        Iterator<String> keys = colums.keySet().iterator();
        while (keys.hasNext()) {
          String key = keys.next();
          ColumnDesc ele = colums.get(key);

          query = query + key + " " + ele.type + " " + ele.constrained;
          if (keys.hasNext()) {
            query = query + ", ";
          }
        }
      } catch (NullPointerException ex) {
        LOGGER.info(ex.getMessage());
      }

      if (null != primaryKeys) {
        query += ", " + primaryKeys;
      }
      query = query + ");";
      LOGGER.info(query);
      return query;
    }
  }

  public static class ColumnDesc {
    private String type;
    private String constrained;

    public ColumnDesc(String type, String constrained) {
      this.type = type;
      this.constrained = constrained;
    }
  }

  public static final class Util {
    private Util() {

    }

    public static boolean validateParams(String... params) {
      if (null == params) {
        return true;
      }

      for (int i = 0; i < params.length; ++i) {

        if (null == params[i]) {
          return true;
        }
      }

      return false;
    }
  }
}



