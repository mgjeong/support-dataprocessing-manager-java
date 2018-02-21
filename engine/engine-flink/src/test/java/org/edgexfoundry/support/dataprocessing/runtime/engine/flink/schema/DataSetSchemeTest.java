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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema;

import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataSetSchemeTest {

  @Test
  public void testSerialize() {
    DataSet dataSet = DataSet.create("testId", "{}");
    DataSetSchema schema = new DataSetSchema();
    byte[] b = schema.serialize(dataSet);
    Assert.assertNotNull(b);
    Assert.assertTrue(b.length > 0);
  }

  @Test
  public void testDeserialize() throws IOException {
    DataSet dataSet = DataSet.create("testId", "{}");
    DataSetSchema schema = new DataSetSchema();
    byte[] b = schema.serialize(dataSet);
    Assert.assertNotNull(b);
    Assert.assertTrue(b.length > 0);

    DataSet newDataSet = schema.deserialize(b);
    Assert.assertNotNull(newDataSet);
    Assert.assertEquals(newDataSet.toString().length(),
        dataSet.toString().length());
    Assert.assertEquals(newDataSet.values().size(), dataSet.values().size());
  }

  @Test
  public void testTypeInformation() {
    DataSetSchema schema = new DataSetSchema();
    TypeInformation<DataSet> producedType = schema.getProducedType();
    Assert.assertNotNull(producedType);
    Assert.assertEquals(producedType.getTypeClass(), DataSet.class);
  }

  @Test
  public void testIsEndOfStream() {
    DataSet streamData = DataSet.create("testId", "{}");
    DataSetSchema schema = new DataSetSchema();
    Assert.assertFalse(schema.isEndOfStream(streamData));
  }

  @Test
  public void testDeserializeFail() {
    DataSetSchema schema = new DataSetSchema();
    Assert.assertNull(schema.deserialize(null));
  }

  @Test(expected = IllegalStateException.class)
  public void testSerializeWithNullDataSet() {
    DataSetSchema schema = new DataSetSchema();
    schema.serialize(null);
  }
}
