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

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.util.StringUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Settings.class})
public class ApplicationTest {

  private static final String DOCKER_PATH = "./application_test/";
  private static final String DB_PATH = "application.db";
  private static final String CUSTOM_JAR_PATH = "./application_test/";
  private static final String FW_JAR_PATH = "./application_test/";
  private static final File testDir = new File(DOCKER_PATH);

  @BeforeClass
  public static void setup() throws Exception {
    if (!testDir.exists()) {
      testDir.mkdirs();
    }

    Settings settings = spy(Settings.getInstance());
    WhiteboxImpl.setInternalState(Settings.class, "instance", settings);

    doReturn(DOCKER_PATH).when(settings).getDockerPath();
    doReturn(DB_PATH).when(settings).getDbPath();
    doReturn(CUSTOM_JAR_PATH).when(settings).getCustomJarPath();
    doReturn(FW_JAR_PATH).when(settings).getFwJarPath();
    doReturn("jdbc:sqlite:" + DOCKER_PATH + DB_PATH).when(settings).getJdbcPath();

    Field settingField = Application.class.getDeclaredField("settings");
    settingField.setAccessible(true);
    settingField.set(null, settings);
  }

  @AfterClass
  public static void cleanup() throws Exception {
    if (testDir.exists()) {
      FileUtils.deleteDirectory(testDir);
    }
  }

  @Test
  public void testInitialization() throws Exception {
    Method initialize = Application.class.getDeclaredMethod("initialize");
    initialize.setAccessible(true);
    initialize.invoke(null);

    Method terminate = Application.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  public void testMakeDirectory() throws Exception {
    Method makeDirectory = Application.class.getDeclaredMethod("makeDirectory", File.class);
    makeDirectory.setAccessible(true);
    File dir = new File("./sample_dir_" + System.currentTimeMillis());
    try {
      makeDirectory.invoke(Application.class, dir);
    } finally {
      if (dir.exists()) {
        dir.delete();
      }
    }
  }

  @Test
  public void testMain() throws Exception {
    try {
      Application.main(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e){

    }
  }

  @Test
  public void testConstructor() {
    Application application = new Application();
    Assert.assertNotNull(application);
  }
}
