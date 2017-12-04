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
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskFlatMap extends RichFlatMapFunction<DataSet, DataSet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFlatMap.class);

    private TaskModel task;

    private TaskFormat taskFormat;

    private transient TaskFactory taskFactory;

    private String host = null;

    public TaskFlatMap(TaskFormat taskFormat) {
        this(taskFormat, null);
    }

    public TaskFlatMap(TaskFormat taskFormat, String host) {
        this.taskFormat = taskFormat;
        this.host = host;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        LOGGER.info("Attempting to create task for {} / {}", taskFormat.getType(), taskFormat.getName());
        // Create task using TaskFactory which is made up by factory pattern.
        this.task = getTaskFactory().createTaskModelInst(taskFormat.getType(), taskFormat.getName(),
                getRuntimeContext().getUserCodeClassLoader(), this.host);
        if (null != this.task) {
            this.task.setParam(taskFormat.getParams());
            this.task.setInRecordKeys(taskFormat.getInrecord());
            this.task.setOutRecordKeys(taskFormat.getOutrecord());
        }
    }

    @Override
    public void flatMap(DataSet dataSet, Collector<DataSet> collector) throws Exception {
        dataSet = this.task.calculate(dataSet);
        if (dataSet != null) {
            collector.collect(dataSet);
        }
    }

    private synchronized TaskFactory getTaskFactory() {
        if (this.taskFactory == null) {
            this.taskFactory = TaskFactory.getInstance();
        }
        return this.taskFactory;
    }
}
