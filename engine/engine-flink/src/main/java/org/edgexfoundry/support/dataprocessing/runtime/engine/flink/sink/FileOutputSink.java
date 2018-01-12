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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink;

import java.io.File;
import java.io.PrintWriter;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class FileOutputSink extends RichSinkFunction<DataSet> {

  private File outputFile = null;

  private PrintWriter writer = null;

  public FileOutputSink(String outputFilePath) {
    this.outputFile = new File(outputFilePath);
  }

  @Override
  public void invoke(DataSet dataSet) throws Exception {
    if (this.writer == null) {
      this.writer = new PrintWriter(this.outputFile);
    }

    this.writer.println(dataSet.toString());
    this.writer.flush();
  }

  @Override
  public void close() throws Exception {
    if (this.writer != null) {
      this.writer.close();
    }
    super.close();
  }
}
