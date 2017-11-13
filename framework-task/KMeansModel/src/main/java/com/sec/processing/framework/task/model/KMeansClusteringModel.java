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
package com.sec.processing.framework.task.model;

import com.sec.processing.framework.task.AbstractTaskModel;
import com.sec.processing.framework.task.DataSet;
import com.sec.processing.framework.task.TaskModelParam;
import com.sec.processing.framework.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class KMeansClusteringModel extends AbstractTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(KMeansClusteringModel.class);

    private static final String KEY_CLUSTERNUM = "cluster_num";
    private static final String KEY_CLUSTERNAME = "cluster_name";
    private static final String KEY_TRAININSTATNCE = "trainInstances";
    private static final String KEY_CENTROIDS = "centroids";
    private static final String KEY_OUTPUT_CLUSTERNAME = "/cluster_name";

    private List<List<Number>> trainInstances;
    private int clusterNum;
    private List<Cluster> clusters;
    private List<List<Number>> centroids;

    public KMeansClusteringModel() {
        trainInstances = new ArrayList<>();
        clusters = new ArrayList<>();
        centroids = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.CLUSTERING;
    }

    @Override
    public String getName() {
        return "KmeansClustering";
    }

    private Double getUclideanDistance(List<Number> first, List<Number> second) {
        double distance = 0;
        for (int i = 0; i < first.size() && i < second.size(); i++) {
            LOGGER.info(i + ", first : " + first.get(i) + "type : " + first.get(i).getClass());
            LOGGER.info(i + ", first : " + first.get(i) + "type : " + first.get(i).getClass());
            distance += Math.pow((Double) first.get(i) - (Double) second.get(i), 2);
        }
        distance = Math.sqrt(distance);
        return distance;
    }

    private List<List<Number>> getCentroids() {
        List<List<Number>> centroids = new ArrayList<>();
        for (Cluster cluster : this.clusters) {
            centroids.add(cluster.getCentroid());
        }
        return centroids;
    }

    private void assignCluster() {
        int cluster = 0;

        for (List<Number> point : trainInstances) {
            double min = Double.MAX_VALUE;
            int i = 0;
            for (Cluster c : this.clusters) {
                Double distance = getUclideanDistance(point, c.getCentroid());
                if (distance <= min) {
                    min = distance;
                    cluster = i;
                }
                i++;
            }
            clusters.get(cluster).addMember(point);
        }
    }

    private void calculateCentroids() {
        for (Cluster cluster : this.clusters) {
            List<List<Number>> list = cluster.getMembers();
            int pointNum = list.size();
            int featureNum = list.get(0).size();
            Double[] sum = new Double[featureNum];
            for (int i = 0; i < featureNum; i++) {
                sum[i] = 0.0;
            }


            for (List<Number> point : list) {
                for (int i = 0; i < point.size(); i++) {
                    sum[i] += (Double) point.get(i);
                }
            }

            List<Number> centroid = new ArrayList<>();
            if (pointNum > 0) {
                for (int i = 0; i < sum.length; i++) {
                    centroid.add(i, sum[i] / pointNum);
                }
            }
            cluster.setCentroid(centroid);
        }
    }

    private void clearClusterMembers() {
        for (Cluster cluster : this.clusters) {
            cluster.clear();
        }
    }

    private void trainClustering() {
        boolean finish = false;
        List<List<Number>> lastCentroids = null;
        List<List<Number>> curCentroids = null;

        while (!finish) {
            clearClusterMembers();
            lastCentroids = getCentroids();

            assignCluster();
            calculateCentroids();

            curCentroids = getCentroids();

            Double distance = 0.0;
            for (int i = 0; i < lastCentroids.size(); i++) {
                distance += getUclideanDistance(lastCentroids.get(i), curCentroids.get(i));
            }

            if (distance == 0.0) {
                finish = true;
            }
        }
    }

    public void makeClusters(List<String> clusterNames) {
        this.clusters = new ArrayList<>();

        for (int i = 0; i < this.clusterNum; i++) {
            String clusterName;
            if (null != clusterNames) {
                clusterName = clusterNames.get(i);
            } else {
                clusterName = String.format("%d", i); //defalut ID : "1", "2", ...
            }

            Cluster c = new Cluster(clusterName);
            if (null != this.centroids && this.clusterNum == centroids.size()) {
                c.setCentroid(this.centroids.get(i));
            } else {
                int random = (int) (Math.random() * (this.trainInstances.size() + 1) % this.trainInstances.size());
                c.setCentroid(this.trainInstances.get(random));
            }
            this.clusters.add(c);
        }


        if (this.centroids.size() == 0) {
            List<List<Number>> curCentroids = getCentroids();

            for (int i = 0; i < curCentroids.size(); i++) {
                this.centroids.add(i, curCentroids.get(i));
            }
        }


        if (null != this.trainInstances && this.trainInstances.size() > 0) {
            trainClustering();
        }
    }

    public void setTrainInstances(List<List<Number>> trainInstances) {
        for (List<Number> d : trainInstances) {
            this.trainInstances.add(d);
        }
    }


    public void setCentroids(List<List<Number>> centroids) {
        for (List<Number> d : centroids) {
            this.centroids.add(d);
        }
    }

    @Override
    public void setParam(TaskModelParam params) throws NullPointerException {

        if (params.containsKey(KEY_TRAININSTATNCE)) {
            setTrainInstances((List<List<Number>>) params.get(KEY_TRAININSTATNCE));
        }

        if (params.containsKey(KEY_CENTROIDS)) {
            params.get(KEY_CENTROIDS);
            setCentroids((List<List<Number>>) params.get(KEY_CENTROIDS));
        }

        if (params.containsKey(KEY_CLUSTERNUM)) {
            this.clusterNum = (int) params.get(KEY_CLUSTERNUM);
        }

        if (params.containsKey(KEY_CLUSTERNAME)) {
            List<String> clusterNames = (List<String>) params.get(KEY_CLUSTERNAME);
            makeClusters(clusterNames);
        }

    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam param = new TaskModelParam();

        String[] arrNames = {"a", "b", "c"};
        List<String> names = new ArrayList<>();
        for (String name : arrNames) {
            names.add(name);
        }
        param.put(KEY_CLUSTERNAME, names);
        param.put(KEY_CLUSTERNUM, 3);

        List<List<Number>> centroidList = new ArrayList<>();
        List<Number> centroids1 = new ArrayList<>();
        centroids1.add(6.5);
        centroids1.add(3.2);
        centroids1.add(5.1);
        centroids1.add(2.0);
        List<Number> centroids2 = new ArrayList<>();
        centroids2.add(6.4);
        centroids2.add(2.7);
        centroids2.add(5.3);
        centroids2.add(1.9);
        List<Number> centroids3 = new ArrayList<>();
        centroids3.add(6.8);
        centroids3.add(3.0);
        centroids3.add(5.5);
        centroids3.add(2.1);
        centroidList.add(centroids1);
        centroidList.add(centroids2);
        centroidList.add(centroids3);

        param.put(KEY_CENTROIDS, centroidList);

        return param;
    }


    public final class Cluster {
        private String id;
        private List<List<Number>> members;
        private List<Number> centroid;

        private Cluster(String id) {
            this.id = id;
            this.members = new ArrayList();
        }

        public String getId() {
            return id;
        }

        public void addMember(List<Number> member) {
            members.add(member);
        }

        public List<List<Number>> getMembers() {
            return members;
        }

        public List<Number> getCentroid() {
            return centroid;
        }

        public void setCentroid(List<Number> centroid) {
            this.centroid = centroid;
        }

        public void clear() {
            members.clear();
        }
    }

    private List<Number> clustering(List<Number> dataInstance) {
        List<Number> result = new ArrayList<>();

        //find the closest centroid
        for (int cNum = 0; cNum < this.clusterNum; cNum++) {
            result.add(cNum, getUclideanDistance(dataInstance, this.centroids.get(cNum)));
        }

        String cluster = findCluster(result);
        return result;
    }

//    private String findCluster(List<Number> values) {
//        Double min = Double.MAX_VALUE;
//        int clusterIdx = 0;
//
//        for (int cNum = 0; cNum < values.size(); cNum++) {
//            if (min > values.get(cNum)) {
//                min = values.get(cNum);
//                clusterIdx = cNum;
//            }
//        }
//
//        return this.clusters.get(clusterIdx).getId();
//    }

    private String findCluster(List<Number> value) {
        if (this.centroids == null) {
            return null;
        }

        if (value.size() < this.centroids.get(0).size()) {
            return null;
        }

        Double min = Double.MAX_VALUE;
        int clusterNum = 0;

        for (int i = 0; i < this.clusters.size(); i++) {
            Cluster c = this.clusters.get(i);
            Double distance = getUclideanDistance(value, c.getCentroid());
            if (distance <= min) {
                min = distance;
                clusterNum = i;
            }
        }
        return this.clusters.get(clusterNum).getId();
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        List<DataSet.Record> records = in.getRecords();

        List<Number> value = new ArrayList();
        if (null == inRecordKeys || inRecordKeys.size() == 0) {
            throw new UnsupportedOperationException("there is no inRecordKeys");
        } else {
            for (String feature : inRecordKeys) {
//                value.add(in.getValue(feature, Double.class));
                value.addAll(in.getValue(feature, ArrayList.class));
            }
        }

        String clusterName = new String();

        if (null != value && 0 != value.size()) {
            clusterName = findCluster(value);
        }

        if (null == outRecordKeys || outRecordKeys.size() == 0) {
            if (null != clusterName && clusterName.length() > 0) {
                in.setValue(outRecordKeys.get(0), clusterName);
            }
        } else {
            if (null != clusterName && clusterName.length() > 0) {
                in.setValue(KEY_OUTPUT_CLUSTERNAME, clusterName);
            }
        }

        return in;
    }
}
