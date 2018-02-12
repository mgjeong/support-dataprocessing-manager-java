package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

public class DatabaseManager {

  private static DatabaseManager instance = null;

  public synchronized static DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
    return instance;
  }

  private Map<String, SQLiteDatabase> databaseMap;

  private DatabaseManager() {
    this.databaseMap = new ConcurrentHashMap<>();
  }

  public synchronized SQLiteDatabase getDatabase(String host) {
    if (StringUtils.isEmpty(host)) {
      throw new RuntimeException("Invalid host.");
    }

    if (databaseMap.get(host) == null) {
      SQLiteDatabase database = new SQLiteDatabase();
      database.initialize(host);
      databaseMap.put(host, database);
    }
    return databaseMap.get(host);
  }
}
