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

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class DatabaseTest {

  protected static File testDB = new File("./test.db");
  protected static File testDBwal = new File("./test.db-wal");
  protected static File testDBshm = new File("./test.db-shm");

  @BeforeClass
  public static void createFiles() {
    if (testDB.exists() && !testDB.delete()) {
      throw new RuntimeException(testDB.getAbsolutePath() + " already exists.");
    } else if (testDBwal.exists() && !testDBwal.delete()) {
      throw new RuntimeException(testDBwal.getAbsolutePath() + " already exists.");
    } else if (testDBshm.exists() && !testDBshm.delete()) {
      throw new RuntimeException(testDBshm.getAbsolutePath() + " already exists.");
    }
  }

  @AfterClass
  public static void deleteFiles() {
    if (testDB.exists()) {
      testDB.delete();
    }
    if (testDBwal.exists()) {
      testDBwal.delete();
    }
    if (testDBshm.exists()) {
      testDBshm.delete();
    }
  }
}
