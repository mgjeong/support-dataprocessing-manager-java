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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class DataSetSchema implements SerializationSchema<DataSet>, DeserializationSchema<DataSet> {

  @Override
  public DataSet deserialize(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return null;
    } else {
      return DataSet.create(new String(bytes));
    }
  }

  @Override
  public boolean isEndOfStream(DataSet dataSet) {
    return false;
  }

  @Override
  public TypeInformation<DataSet> getProducedType() {
    return TypeExtractor.getForClass(DataSet.class);
  }

  @Override
  public byte[] serialize(DataSet dataSet) {
    if (dataSet == null) {
      throw new IllegalStateException("DataSet is null.");
    }
    String str = dataSet.toString();
    if (str == null || str.isEmpty()) {
      throw new IllegalStateException("DataSet in string is null.");
    } else {
      return str.getBytes();
    }
  }
}
