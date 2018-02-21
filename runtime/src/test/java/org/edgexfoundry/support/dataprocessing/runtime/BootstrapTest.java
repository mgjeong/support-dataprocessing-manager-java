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
package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import java.lang.reflect.Field;
import org.edgexfoundry.support.dataprocessing.runtime.db.AbstractStorageManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.DatabaseManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WorkflowTableManager.class)
public class BootstrapTest {

  @Test
  public void testBootstrap() throws Exception {
    File dbFile = new File("./bootstrap_test.db");
    File dbFileWal = new File("./bootstrap_test.db-wal");
    File dbFileShm = new File("./bootstrap_test.db-shm");
    if (dbFile.exists()) {
      throw new RuntimeException(dbFile.getAbsolutePath() + " already exists.");
    }

    try {
      // Setup workflow table manager
      WorkflowTableManager workflowTableManager = WorkflowTableManager.getInstance();
      java.lang.reflect.Field databaseField = AbstractStorageManager.class
          .getDeclaredField("database");
      databaseField.setAccessible(true);
      databaseField.set(workflowTableManager,
          DatabaseManager.getInstance().getDatabase("jdbc:sqlite:" + dbFile.getAbsolutePath()));

      // Setup bootstrap
      Bootstrap bootstrap = new Bootstrap();
      Field wtmField = Bootstrap.class.getDeclaredField("workflowTableManager");
      wtmField.setAccessible(true);
      wtmField.set(bootstrap, workflowTableManager);

      bootstrap.execute(); // insert

      // run twice to update
      bootstrap.execute(); // update
    } finally {
      if (dbFile.exists()) {
        dbFile.delete();
      }
      if (dbFileWal.exists()) {
        dbFileWal.delete();
      }
      if (dbFileShm.exists()) {
        dbFileShm.delete();
      }
    }
  }
}
