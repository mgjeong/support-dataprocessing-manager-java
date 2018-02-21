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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task.FlatMapTask;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class FlatMapTaskVertex implements Vertex {

  private DataStream<DataSet> influx = null;
  private WorkflowProcessor taskInfo;

  public FlatMapTaskVertex(WorkflowProcessor taskInfo) {
    this.taskInfo = taskInfo;
  }

  @Override
  public Long getId() {
    return this.taskInfo.getId();
  }

  @Override
  public DataStream<DataSet> serve() {
    return influx.flatMap(new FlatMapTask(taskInfo.getConfig().getProperties()));
  }

  @Override
  public void setInflux(DataStream<DataSet> influx) {
    if (this.influx == null) {
      this.influx = influx;
    } else {
      if (this.influx != influx) {
        this.influx = this.influx.union(influx);
      }
    }
  }
}
