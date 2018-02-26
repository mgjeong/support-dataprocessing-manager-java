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

import java.sql.Connection;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteDataSource;

public final class SQLiteDatabase {

  private SQLiteDataSource dataSource;

  public synchronized void initialize(String host) {
    SQLiteConfig config = new SQLiteConfig();
    config.setJournalMode(JournalMode.WAL);
    // Set config if necessary
    dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(host);
  }

  public synchronized Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new SQLException(SQLiteDatabase.class.getSimpleName() + " is not initialized.");
    }

    try {
      Connection connection = dataSource.getConnection();
      if (connection == null) {
        throw new SQLException("Failed to retrieve connection from data source.");
      }
      return connection;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }
}

