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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.operator;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskFactory;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.util.Collector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;

public class TaskFlatMapTest {
    @Before
    public void initialize() {
        mockitoSession().initMocks(this);
    }

    @Test
    public void testCreateValidFlatMap() throws Exception {
        TaskFormat taskFormat = mock(TaskFormat.class);
        when(taskFormat.getType()).thenReturn(TaskType.INVALID);
        when(taskFormat.getName()).thenReturn(MockTaskModel.class.getSimpleName());

        ClassLoader classLoader = this.getClass().getClassLoader();
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(runtimeContext.getUserCodeClassLoader()).thenReturn(classLoader);

        TaskFactory taskFactory = mock(TaskFactory.class);
        when(taskFactory.createTaskModelInst(any(), anyString(), any()))
                .thenReturn(new MockTaskModel());

        TaskFlatMap flatMap = new TaskFlatMap(taskFormat);
        flatMap.setRuntimeContext(runtimeContext);
        Field fieldTaskFactory = TaskFlatMap.class.getDeclaredField("taskFactory");
        fieldTaskFactory.setAccessible(true);
        fieldTaskFactory.set(flatMap, taskFactory);
        flatMap.open(null);
    }

    @Test
    public void testCreateInvalidFlatMap() throws Exception {
        TaskFormat taskFormat = mock(TaskFormat.class);
        when(taskFormat.getType()).thenReturn(TaskType.INVALID);
        when(taskFormat.getName()).thenReturn("ShouldNotWork");

        ClassLoader classLoader = this.getClass().getClassLoader();
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(runtimeContext.getUserCodeClassLoader()).thenReturn(classLoader);

        TaskFactory taskFactory = mock(TaskFactory.class);
        when(taskFactory.createTaskModelInst(any(), anyString(), any()))
                .thenThrow(new Exception("Failed to create new model."));

        TaskFlatMap flatMap = new TaskFlatMap(taskFormat);
        flatMap.setRuntimeContext(runtimeContext);
        Field fieldTaskFactory = TaskFlatMap.class.getDeclaredField("taskFactory");
        fieldTaskFactory.setAccessible(true);
        fieldTaskFactory.set(flatMap, taskFactory);
        try {
            flatMap.open(null);
            Assert.fail("Should not reach here.");
        } catch (Exception e) {

        }
    }

    @Test
    public void testFlatMapProcess() throws Exception {
        TaskFormat taskFormat = mock(TaskFormat.class);
        TaskFlatMap map = new TaskFlatMap(taskFormat);
        Field taskField = TaskFlatMap.class.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(map, new MockTaskModel());

        Collector<DataSet> collector = mock(Collector.class);
        DataSet streamData = DataSet.create();
        map.flatMap(streamData, collector);
    }

    private static class MockTaskModel implements TaskModel {

        @Override
        public TaskType getType() {
            return TaskType.INVALID;
        }

        @Override
        public String getName() {
            return MockTaskModel.class.getSimpleName();
        }

        @Override
        public TaskModelParam getDefaultParam() {
            return new TaskModelParam();
        }

        @Override
        public void setParam(TaskModelParam param) {

        }

        @Override
        public void setInRecordKeys(List<String> inRecordKeys) {

        }

        @Override
        public void setOutRecordKeys(List<String> outRecordKeys) {

        }

        @Override
        public DataSet calculate(DataSet in) {
            return in;
        }
    }
}
