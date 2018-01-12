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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.bson.Document;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDbSink extends RichSinkFunction<DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbSink.class);

  private final String ip;
  private final int port;
  private final String dbName;
  private final String tableName;

  private MongoClient client = null;
  private MongoDatabase db = null;
  private MongoCollection<Document> table = null;

  public MongoDbSink(String source, String name) {

    String[] dataSource = source.split(":");
    String[] names = name.split(":");
    this.ip = new String(dataSource[0]);
    this.port = Integer.parseInt(dataSource[1]);
    this.dbName = new String(names[0]);
    this.tableName = new String(names[1]);

    LOGGER.info("NAME : {}", name);
  }


  @Override
  public void invoke(DataSet dataSet) throws Exception {

    // You could loop through records like this:
    List<Document> list = new ArrayList<>();
    for (DataSet.Record record : dataSet.getRecords()) {
      LOGGER.info("Writing to {}:{}. DataSet: {}", this.ip, this.port, record.toString());

      list.add(Document.parse(record.toString()));
    }
    if (list.size() > 0) {
      table.insertMany(list);
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    client = new MongoClient(ip, this.port);
    db = client.getDatabase(this.dbName);
    table = db.getCollection(this.tableName);

    LOGGER.info("Initiate MongoDB Connection Intialization : " + this.ip + " : " + this.port);
    LOGGER.info("DB Name {}, Table Name {} : ", this.dbName, this.tableName);

  }

  @Override
  public void close() throws Exception {

    LOGGER.info("Close MongoDB Connection");

    super.close();
  }

}
