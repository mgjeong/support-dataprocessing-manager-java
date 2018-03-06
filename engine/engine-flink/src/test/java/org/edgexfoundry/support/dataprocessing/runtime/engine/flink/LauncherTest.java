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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;


import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraphBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Launcher.class, StreamExecutionEnvironment.class,
    JobGraphBuilder.class, JobGraph.class})
public class LauncherTest {

  private static final String TEST_EXT_DIR = System.getProperty("user.dir") + "/LauncherTestClass";
  private static final String TEST_INT_DIR = Launcher.class.getResource("/").getPath();
  private static final String TEST_JSON_ID = "launcher_test_job";
  private static final String TEST_JSON_EXT_PATH = TEST_EXT_DIR + "/" + TEST_JSON_ID + ".json";
  private static final String TEST_JSON_INT_PATH = TEST_INT_DIR + TEST_JSON_ID + ".json";
  private static final String JSON_CONTENT = "{}";
  private static final String[] ARGS_NO_JSON = {"--internal", TEST_JSON_EXT_PATH};
  private static final String[] ARGS_INVALID = {"--internal", "--json", "unreachableFile"};
  private static final String[] ARGS_FOR_EXTERNAL_CONFIG = {"--json", TEST_JSON_EXT_PATH};
  private static final String[] ARGS_FOR_INTERNAL_CONFIG = {"--internal", "--json", TEST_JSON_ID};
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void createTestFiles() throws Exception {
    File jsonFile = new File(TEST_JSON_INT_PATH);
    if (!jsonFile.exists()) {
      jsonFile.getParentFile().mkdirs();
      jsonFile.createNewFile();
    }
    FileUtils.writeStringToFile(jsonFile, JSON_CONTENT);

    jsonFile = new File(TEST_JSON_EXT_PATH);
    if (!jsonFile.exists()) {
      jsonFile.getParentFile().mkdirs();
      jsonFile.createNewFile();
    }
    FileUtils.writeStringToFile(jsonFile, JSON_CONTENT);
  }

  @AfterClass
  public static void deleteTestFiles() {
    File testJsonFile = new File(TEST_JSON_EXT_PATH);

    if (testJsonFile.exists()) {
      testJsonFile.getParentFile().deleteOnExit();
      testJsonFile.delete();
    }

    testJsonFile = new File(TEST_JSON_INT_PATH);

    if (testJsonFile.exists()) {
      testJsonFile.delete();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testWithoutAnyCommandLineArguments() throws Exception {
    Launcher.main(new String[]{});
    Assert.fail("Failed: Not any exception on a job without specifications");
  }

  @Test(expected = RuntimeException.class)
  public void testWithImproperArgs() throws Exception {
    Launcher.main(ARGS_NO_JSON);
    Assert.fail("Failed: Not any exception on a job with invalid specifications");
  }

  @Test(expected = RuntimeException.class)
  public void testWithInputUnavailable() throws Exception {
    Launcher.main(ARGS_INVALID);
    Assert.fail("Failed: Not any exception on an invalid job configuration file");
  }

  @Test
  public void testWithInternalJsonFile() throws Exception {
    JobGraph mockGraph = Mockito.mock(JobGraph.class);
    Mockito.doNothing().when(mockGraph).initialize();
    Mockito.when(mockGraph.getJobId()).thenReturn("testId");

    JobGraphBuilder builder = Mockito.mock(JobGraphBuilder.class);
    Mockito.when(builder.getInstance(any(), any())).thenReturn(mockGraph);
    PowerMockito.mockStatic(JobGraphBuilder.class);
    PowerMockito.whenNew(JobGraphBuilder.class).withNoArguments().thenReturn(builder);

    StreamExecutionEnvironment env = Mockito.mock(StreamExecutionEnvironment.class);
    Mockito.when(env.execute(any())).thenReturn(null);

    ExecutionConfig config = Mockito.mock(ExecutionConfig.class);
    Mockito.doNothing().when(config).setGlobalJobParameters(any());
    Mockito.when(env.getConfig()).thenReturn(config);
    PowerMockito.mockStatic(StreamExecutionEnvironment.class);
    PowerMockito.when(StreamExecutionEnvironment.getExecutionEnvironment()).thenReturn(env);

    Launcher.main(ARGS_FOR_INTERNAL_CONFIG);
  }

  @Test
  public void testWithExternalJsonFile() throws Exception {
    JobGraph mockGraph = Mockito.mock(JobGraph.class);
    Mockito.doNothing().when(mockGraph).initialize();
    Mockito.when(mockGraph.getJobId()).thenReturn("testId");

    JobGraphBuilder builder = Mockito.mock(JobGraphBuilder.class);
    Mockito.when(builder.getInstance(any(), any())).thenReturn(mockGraph);
    PowerMockito.mockStatic(JobGraphBuilder.class);
    PowerMockito.whenNew(JobGraphBuilder.class).withNoArguments().thenReturn(builder);

    StreamExecutionEnvironment env = Mockito.mock(StreamExecutionEnvironment.class);
    Mockito.when(env.execute(any())).thenReturn(null);

    ExecutionConfig config = Mockito.mock(ExecutionConfig.class);
    Mockito.doNothing().when(config).setGlobalJobParameters(any());
    Mockito.when(env.getConfig()).thenReturn(config);
    PowerMockito.mockStatic(StreamExecutionEnvironment.class);
    PowerMockito.when(StreamExecutionEnvironment.getExecutionEnvironment()).thenReturn(env);
    Launcher.main(ARGS_FOR_EXTERNAL_CONFIG);
  }
}
