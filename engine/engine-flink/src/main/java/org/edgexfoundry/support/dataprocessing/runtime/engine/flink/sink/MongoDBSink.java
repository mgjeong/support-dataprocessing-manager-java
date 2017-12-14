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

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.util.JSON;

import java.util.ArrayList;
import java.util.List;

public class MongoDBSink extends RichSinkFunction<DataSet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBSink.class);

    private final String mIP;
    private final int mPort;
    private final String mDBName;
    private final String mTableName;

    private MongoClient mClinet = null;
    private MongoDatabase mDB = null;
    private MongoCollection<Document> mTable = null;

    public MongoDBSink(String source, String name) {

        String[] dataSource = source.split(":");
        String[] names = name.split(":");
        this.mIP = new String(dataSource[0]);
        this.mPort = Integer.parseInt(dataSource[1]);
        this.mDBName = new String(names[0]);
        this.mTableName = new String(names[1]);

        LOGGER.info("NAME : {}", name);
    }


    @Override
    public void invoke(DataSet dataSet) throws Exception {

        // You could loop through records like this:
        List<Document> list = new ArrayList<>();
        for (DataSet.Record record : dataSet.getRecords()) {
            LOGGER.info("Writing to {}:{}. DataSet: {}", this.mIP,this.mPort, record.toString());

            list.add(Document.parse(record.toString()));
        }
        if(list.size() > 0) {
            mTable.insertMany(list);
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        mClinet = new MongoClient(mIP, this.mPort);
        mDB = mClinet.getDatabase(this.mDBName);
        mTable = mDB.getCollection(this.mTableName);

        LOGGER.info("Initiate MongoDB Connection Intialization : "+this.mIP +" : " +this.mPort);
        LOGGER.info("DB Name {}, Table Name {} : ", this.mDBName, this.mTableName);

    }

    @Override
    public void close() throws Exception {

        LOGGER.info("Close MongoDB Connection");

        super.close();
    }

}
