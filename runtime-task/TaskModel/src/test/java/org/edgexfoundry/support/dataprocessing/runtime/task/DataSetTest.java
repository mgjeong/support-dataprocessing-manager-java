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
package org.edgexfoundry.processing.runtime.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataSetTest {
    @Test
    public void testCreate() {
        DataSet dataSet = DataSet.create();
        System.out.println(dataSet.toString());

        dataSet = DataSet.create("{}");
        System.out.println(dataSet.toString());

        dataSet = DataSet.create("{\"A\":\"A1\"}");
        System.out.println(dataSet.toString());
        Assert.assertTrue(dataSet.toString().contains("A"));

        dataSet = DataSet.create("123", "{}");
        Assert.assertNotNull(dataSet.getId());
        Assert.assertEquals("123", dataSet.getId());
    }

    @Test
    public void testSerializeAndDeserializeToAndFromString() {
        DataSet dataSet = getSampleDataSet();
        System.out.println(dataSet.toString());

        String strDataSet = dataSet.toString();
        Assert.assertNotNull(strDataSet);

        DataSet recovered = DataSet.create(strDataSet);
        Assert.assertNotNull(recovered);

        System.out.println(recovered.toString());
        Assert.assertEquals(strDataSet.length(), recovered.toString().length());
    }

    @Test
    public void testSimpleStreamData() {
        /*
         * Sample JSON:
         *   { "age": 21, "height": 180.3, "name": "John" }
         */
        JsonObject sampleJson = makePerson("John", 21, 180.3);

        DataSet dataSet = DataSet.create(sampleJson.toString());
        /*
         * Expected DataSet (dpfw-dataset-id may differ):
         * {"records":[{"name":"John","age":21,"height":180.3}],"dpfw-dataset-id":"1505700380498"}
         */
        System.out.println(dataSet.toString());

        // Assertions
        JsonObject dataSetJson = new JsonParser().parse(dataSet.toString()).getAsJsonObject();
        Assert.assertNotNull(dataSetJson.get("dpfw-dataset-id"));
        Assert.assertNotNull(dataSetJson.get("records"));
        JsonArray records = (JsonArray) dataSetJson.get("records");
        JsonObject record = (JsonObject) records.get(0);
        Assert.assertEquals(sampleJson.get("name").getAsString(), record.get("name").getAsString());
        Assert.assertEquals(sampleJson.get("age").getAsInt(), record.get("age").getAsInt());
        Assert.assertEquals(sampleJson.get("height").getAsDouble(), record.get("height").getAsDouble(), 0.005);

        // Try getValue
        List<Object> names = dataSet.getValue("/records/name", List.class);
        // You could also do,
        //   List<Object> names = dataSet.getValue("/records/*/name", List.class);
        /*
         * Expected names: ["John"]
         */
        String john = (String) names.get(0);
        Assert.assertEquals(sampleJson.get("name").getAsString(), john);

        // Try setValue
        dataSet.setValue("/records/gender", "male");
        /*
         * Expected DataSet (dpfw-dataset-id may differ):
         * {"records":[{"gender": "male","name":"John","age":21,"height":180.3}],"dpfw-dataset-id":"1505700380498"}
         */
        System.out.println(dataSet.toString());
    }

    @Test
    public void testSimpleBatchData() {
        JsonObject john = makePerson("John", 21, 180.3);
        JsonObject ann = makePerson("Ann", 18, 162.5);
        JsonObject steve = makePerson("Steve", 25, 176.2);

        DataSet dataSet = DataSet.create();
        dataSet.addRecord(john.toString());
        dataSet.addRecord(ann.toString());
        // You could also add a new record like this:
        dataSet.addRecord(DataSet.Record.create(steve.toString()));
        System.out.println(dataSet.toString());

        // Try getValue
        List<String> names = dataSet.getValue("/records/name", List.class);
        System.out.println(names); // Expect "John, Ann, Steve" to be printed.
        List<Integer> ages = dataSet.getValue("/records/age", List.class);
        System.out.println(ages); // Expect "21, 18, 25" to be printed.

        // Try setValue
        List<String> genders = new ArrayList<>();
        genders.add("male"); // John
        genders.add("female"); // Ann
        genders.add("male"); // Steve
        dataSet.setValue("/records/*/gender", genders);
        System.out.println(dataSet.toString());

        // You could loop through records like this:
        for (DataSet.Record record : dataSet.getRecords()) {
            System.out.println("record: " + record.toString());
        }
    }

    @Test
    public void testComplexStreamData() {
        JsonObject john = makePerson("John", 21, 180.3);
        JsonObject scores = makeScores(80, 10, 20);
        john.add("scores", scores);

        DataSet dataSet = DataSet.create(john.toString());
        /*
         * Expected:
         *   {"records":[{"scores":{"math":80,"english":10,"history":20},
         *                "name":"John","age":21,"height":180.3}],"dpfw-dataset-id":"1505705715778"}
         */
        System.out.println(dataSet.toString());

        // Try getValue
        List<Map<String, Integer>> recordScores = dataSet.getValue("/records/scores", List.class);
        System.out.println(recordScores.toString());
        Map<String, Integer> johnScore = recordScores.get(0);
        System.out.println(johnScore.toString());
        Assert.assertEquals(80, johnScore.get("math").intValue());
        Assert.assertEquals(10, johnScore.get("english").intValue());
        Assert.assertEquals(20, johnScore.get("history").intValue());

        // Try setValue
        dataSet.setValue("/records/scores/physics", 40); // Add physics score
        // or, you could do:
        // dataSet.setValue("/records/*/scores/physics", 40); // Add physics score
        System.out.println(dataSet.toString());
    }

    @Test
    public void testComplexBatchData() {
        JsonObject john = makePerson("John", 21, 180.3);
        JsonObject johnScores = makeScores(80, 10, 20);
        john.add("scores", johnScores);

        JsonObject ann = makePerson("Ann", 18, 162.5);
        JsonObject annScores = makeScores(30, 45, 12);
        ann.add("scores", annScores);

        DataSet dataSet = DataSet.create();
        dataSet.addRecord(john.toString());
        dataSet.addRecord(ann.toString());
        System.out.println(dataSet.toString());

        // Try getValue
        System.out.println("getValue(\"/records/scores\"): ");
        List<Map<String, Integer>> recordScores = dataSet.getValue("/records/scores", List.class);
        System.out.println(recordScores.toString());
        for (Map<String, Integer> recordScore : recordScores) {
            System.out.println(recordScore);
        }

        // You could just access record too
        System.out.println("You can just iterate through records:");
        for (DataSet.Record record : dataSet.getRecords()) {
            System.out.println(record.toString());
        }

        // Try setValue
        List<Integer> physicsScores = new ArrayList<>();
        physicsScores.add(30); // john's physics score
        physicsScores.add(23); // ann's physics score
        dataSet.setValue("/records/*/scores/physics", physicsScores);

        System.out.println(dataSet.toString());
    }

    private JsonObject makeScores(int mathScore, int englishScore, int historyScore) {
        JsonObject score = new JsonObject();
        score.addProperty("math", mathScore);
        score.addProperty("english", englishScore);
        score.addProperty("history", historyScore);
        return score;
    }

    private JsonObject makePerson(String name, int age, double height) {
        JsonObject sampleJson = new JsonObject();
        sampleJson.addProperty("age", age);
        sampleJson.addProperty("height", height);
        sampleJson.addProperty("name", name);
        return sampleJson;
    }

    @Test
    public void testGetValue() {
        DataSet dataSet = getSampleDataSet();
        dataSet.put("score", 3.44);
        List<String> s = dataSet.getValue("/records/d", List.class);
        System.out.println(Arrays.toString(s.toArray()));

        List<String> t = dataSet.getValue("/records/*/d", List.class);
        System.out.println(Arrays.toString(t.toArray()));

        Assert.assertEquals(s.size(), t.size());

        // Try primitive
        double score = dataSet.getValue("/score", double.class);
        System.out.println(score);

        // Try invalid key
        String invalid = dataSet.getValue("/invalid", String.class);
        Assert.assertNull(invalid);

        // Add records to mimic batch
        populateRecords(dataSet);
        Assert.assertTrue(dataSet.isBatch());
        s = dataSet.getValue("/records/d", List.class);
        System.out.println(Arrays.toString(s.toArray()));
        Assert.assertEquals(dataSet.getRecords().size(), s.size());

        // Try first, last
        int x = dataSet.getValue("/records/^/a", int.class);
        Assert.assertEquals(dataSet.getRecords().get(0).get("a"), x);
        x = dataSet.getValue("/records/$/a", int.class);
        Assert.assertEquals(dataSet.getRecords().get(dataSet.getRecords().size() - 1).get("a"), x);
    }

    @Test
    public void testSetValue() {
        DataSet dataSet = getSampleDataSet();

        // Set value at root
        dataSet.setValue("/score", 3.14);
        System.out.println(dataSet.toString());
        Assert.assertEquals(dataSet.get("score"), 3.14);

        // Set value inside record
        dataSet.setValue("/records/test", "helloworld");
        Assert.assertEquals(dataSet.getRecords().get(0).get("test"), "helloworld");

        // Set invalid key
        try {
            dataSet.setValue("/records/*", "should not work");
            Assert.fail();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        try {
            dataSet.setValue("/a/b/c/d/e/f", "should not work");
            Assert.fail();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // Populate records
        populateRecords(dataSet);
        Assert.assertTrue(dataSet.isBatch());
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < dataSet.getRecords().size(); i++) {
            values.add(i);
        }

        // Try *
        dataSet.setValue("/records/*/index", values);
        System.out.println(dataSet.toString());
        for (int i = 0; i < dataSet.getRecords().size(); i++) {
            Assert.assertEquals(dataSet.getRecords().get(i).get("index"), i);
        }

        // Try ^
        dataSet.setValue("/records/^/first", true);
        System.out.println(dataSet.toString());
        Assert.assertEquals(dataSet.getRecords().get(0).get("first"), true);

        // Try $
        dataSet.setValue("/records/$/last", true);
        System.out.println(dataSet.toString());
        Assert.assertEquals(dataSet.getRecords().get(dataSet.getRecords().size() - 1).get("last"), true);
    }

    @Test
    public void testRecord() {
        DataSet.Record record = DataSet.Record.create();
        record.put("a", 1);
        record.put("b", 2);
        System.out.println(record.toString());
        int a = (int) record.get("a");
        Assert.assertEquals(1, a);

        DataSet.Record newRecord = DataSet.Record.create("{\"A\":\"123\", \"B\":5}");
        System.out.println(newRecord.toString());
    }

    private void populateRecords(DataSet dataSet) {
        DataSet.Record record = dataSet.getRecords().get(0);
        for (int i = 0; i < 5; i++) {
            DataSet.Record newRecord = (DataSet.Record) record.clone();
            newRecord.put("c", Math.random());
            newRecord.put("a", (int) Math.random() * 10000);
            newRecord.put("d", UUID.randomUUID().toString());
            dataSet.addRecord(newRecord);
        }
    }

    private DataSet getSampleDataSet() {
        DataSet dataSet = DataSet.create();
        DataSet.Record record = DataSet.Record.create();
        record.put("a", 1);
        record.put("b", 100L);
        record.put("c", 3.14);
        record.put("d", "Hello");
        dataSet.addRecord(record);
        return dataSet;
    }
}
