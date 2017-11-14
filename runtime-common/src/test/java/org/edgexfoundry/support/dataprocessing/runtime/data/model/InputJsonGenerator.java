package org.edgexfoundry.processing.runtime.data.model;

import org.edgexfoundry.processing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.processing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.processing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.processing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.processing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InputJsonGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputJsonGenerator.class);

    
//    private static final String IP_ADDR = getCurrentIp();
    private static final String IP_ADDR = "127.0.0.1";
//    private static final String IP_ADDR = "10.113.64.225";

    private static final String ZMQ_DATA_TYPE = "ZMQ";
    private static final String FILE_DATA_TYPE = "F";
    private static final String WEBSOCKET_DATA_TYPE = "WS";
    private static final String EMF_DATA_TYPE = "EMF";

    private static final String ZMQ_DATA_SOURCE_INPUT = IP_ADDR + ":5555:topic";
    private static final String EMF_DATA_SOURCE_INPUT = "edgex-device-file:5562:PROTOBUF_MSG";
    private static final String ZMQ_DATA_SOURCE_OUTPUT = IP_ADDR + ":5556:topic";
    private static final String FILE_DATA_SOURCE_OUPUT = "output";
    private static final String WEBSOCKET_DATA_SOURCE_OUTPUT = IP_ADDR + ":8083";


    private static final String TEMP_ZMQ1 = "127.0.0.1:5557:topic";
    private static final String TEMP_ZMQ2 = "127.0.0.1:5558:topic";
    private static final String TEMP_ZMQ3 = "127.0.0.1:5559:topic";
    private static final String TEMP_ZMQ4 = "127.0.0.1:5560:topic";
    private static final String TEMP_ZMQ5 = "127.0.0.1:5561:topic";

    private static final String LINEARREGRESSION = "LinearRegression";
    private static final String LOGISTICREGRESSION = "LogisticRegression";
    private static final String CSVPARSER = "CsvParser";
    private static final String JSONGENERATOR = "JsonGenerator";
    private static final String AGGREGATOR = "Aggregator";
    private static final String TREND = "sma";

    private static final String LINEAR_BAD_WEIGHT_VALUE
            = "{\"error\": \"1\", \"weights\": "
            + "\"1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 "
            + "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\"}";
    private static final String LINEAR_GOOD_WEIGHT_VALUE
            = "{\"error\": \"1.235593\", \"weights\": "
            + "\"0.228758 -0.156367 -0.076053 4.438103 -0.013772 -0.057729 -0.064135 -0.091184 "
            + "0.079168 0.232852 0.184895 -0.162730 -0.143529 -0.197610 0.013371 0.055744 0.063586 "
            + "0.132661 0.110600 -0.137898 0.167547 0.108086 0.106888 -0.024476 -0.086525 -0.092372 "
            + "-0.066006 -0.056369 0.058882 0.041577 -0.084595 -0.169737 -0.044816 -0.098127 -0.077932 "
            + "-0.009962 -0.028224 -0.021372 -0.048000 -0.002622 0.046431 -0.191930 0.201900 -0.079681 "
            + "-0.114898 -0.025332 -0.147640 0.050427 0.027520 -0.190859 0.037761 -0.186507 -0.149085 "
            + "-0.031291 0.099267 -0.166712 0.197141 -0.096736 -0.823573 -0.063322 0.303512 -0.139935 "
            + "0.214752 -0.245382 0.188524 0.427104 0.088042 0.672953 -0.171236 -0.429767 0.179425 "
            + "-0.316761 0.379640 0.375668 0.056403 -0.466760 0.360497 -0.466893 0.249074 2.198648 "
            + "0.525941 0.006336 0.143716 -0.741466\"}";
    private static final String LINEAR_PARSER_VALUE
            = "{\"delimiter\": \"\\t\", \"index\": "
            + "\"1 2 3 4 6 7 8 9 11 12 13 14 15 17 18 19 21 22 23 24 26 27 28 29 31 32 33 34 36 37 38 39 "
            + "41 42 43 44 46 47 48 49 51 52 53 54 56 57 58 59 61 62 63 64 65 66 67 68 71 72 73 74 76 77 "
            + "78 79 81 82 83 84 86 87 88 89 91 92 93 94 96 97 98 99 101 102 103 104\"}";
    private static final String LOGISTIC_BAD_WEIGHT_VALUE
            = "{\"error\": \"1\", \"weights\": \"1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\"}";
    private static final String LOGISTIC_GOOD_WEIGHT_VALUE
            = "{\"error\": \"-1.704e16\", \"weights\": "
            + "\"-4.508e15 -2.681e13 5.592e14 6.374e12 4.804e16 -2.931e16 3.110e14 8.171e15 -8.564e15 "
            + "-1.030e16 8.161e15 -4.617e14 -8.989e14 6.591e12 -1.037e16 4.729e16 -2.689e16 1.993e17 "
            + "-4.806e16 -4.364e17 1.102e15 1.390e14 6.421e13 -9.950e12 -6.379e15 -7.493e15 6.282e15 "
            + "-7.308e15 1.098e16 4.755e16\"}";
    private static final String LOGISTIC_PARSER_VALUE
            = "{\"delimiter\": \"\\t\", \"index\": "
            + "\"1 2 3 5 6 7 9 10 11 13 14 15 17 18 19 21 22 23 25 26 27 29 30 31 33 34 35 37 38 39\"}";
    private static final String NAIVEBAYES_VALUE
            = "{ \"means\": [ [ 5.855, 176.25, 11.25 ], [ 5.4175, 132.5, 7.5 ] ], "
            + "\"kernel\": { \"bandwidth\": 5.5, \"type\": \"gaussian\" }, "
            + "\"class_info\": { \"possibilities\": [ 0.5, 0.5 ], \"names\": [ \"male\", \"female\" ] }, "
            + "\"variances\": [ [ 0.035033, 122.92, 0.91667 ], [ 0.097225, 558.33, 1.6667 ] ], "
            + "\"randomVectors\": [ [ 6, 175, 11 ], [ 4, 132, 7 ] ], \"error\": [ \"1.1\", \"2.2\" ] }";

    private String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    private static TaskFormat createTask(TaskType type, String name, String params) {
        return new TaskFormat(type, name, params);
    }

    private static TaskFormat createTrendTask(String params) {
        return createTask(TaskType.TREND, TREND, params);
    }

    private static TaskFormat createJsonGeneratorTask(String params) {
        return createTask(TaskType.PREPROCESSING, JSONGENERATOR, params);
    }

    private static TaskFormat createAggregatorTask(String params) {
        return createTask(TaskType.PREPROCESSING, AGGREGATOR, params);
    }

    private static TaskFormat createParserTask(String params) {
        return createTask(TaskType.PREPROCESSING, CSVPARSER, params);
    }

    private static TaskFormat createLinearRegressionTask(String params) {
        return createTask(TaskType.REGRESSION, LINEARREGRESSION, params);
    }

    private static TaskFormat createLogisticRegressionTask(String params) {
        return createTask(TaskType.REGRESSION, LOGISTICREGRESSION, params);
    }

    private static TaskFormat createLinearParserTask() {
        return createParserTask(LINEAR_PARSER_VALUE);
    }

    private static TaskFormat createLogisticParserTask() {
        return createParserTask(LOGISTIC_PARSER_VALUE);
    }

    private static String getCurrentIp() {
        String interfaceName = "eth0";
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName(interfaceName);

            Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
            InetAddress currentAddress;
            currentAddress = inetAddress.nextElement();
            while (inetAddress.hasMoreElements()) {
                currentAddress = inetAddress.nextElement();
                if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                    return currentAddress.toString().substring(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    @Test
    public void aLogisticRegressionBadWeight() {
        JobGroupFormat jobs = new JobGroupFormat();
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(FILE_DATA_TYPE, FILE_DATA_SOURCE_OUPUT));
        job.addTask(createLogisticRegressionTask(LOGISTIC_BAD_WEIGHT_VALUE));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void bLogisticRegressionGoodWeight() {
        JobGroupFormat jobs = new JobGroupFormat();
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(FILE_DATA_TYPE, FILE_DATA_SOURCE_OUPUT));
        job.addTask(createLogisticRegressionTask(LOGISTIC_GOOD_WEIGHT_VALUE));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void cParserLogisticRegressionGoodWeight() {
        JobGroupFormat jobs = new JobGroupFormat();
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(FILE_DATA_TYPE, FILE_DATA_SOURCE_OUPUT));
        job.addTask(createLogisticParserTask());
        job.addTask(createLogisticRegressionTask(LOGISTIC_GOOD_WEIGHT_VALUE));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void dWebClientJson() {
        JobGroupFormat jobs = new JobGroupFormat();

        // alias Flinck Job A
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addTask(createParserTask("{\"delimiter\":\"\\t\",\"index\":\"0\"}}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"y\"}"));
        jobs.addJob(job);

        // alias Flinck Job B
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ3));
        job.addTask(createLinearParserTask());
        jobs.addJob(job);

        // alias Flinck Job C
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ4));
        job.addTask(createLinearRegressionTask(LINEAR_GOOD_WEIGHT_VALUE));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"Good\"}"));
        jobs.addJob(job);

        // alias Flinck Job D
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ3));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ5));
        job.addTask(createLinearRegressionTask(LINEAR_BAD_WEIGHT_VALUE));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"Bad\"}"));
        jobs.addJob(job);

        // alias Flinck Job E
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ4));
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ5));
        job.addOutput(new DataFormat(WEBSOCKET_DATA_TYPE, WEBSOCKET_DATA_SOURCE_OUTPUT));
        job.addTask(createAggregatorTask("{\"keys\": \"y Good Bad\", \"aggregateBy\": \"Id\"}"));
        job.addTask(createJsonGeneratorTask("{\"keys\": \"Actual Good-weight Bad-weight\"}"));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }


    @Test
    public void eSimpleMovingAverage() {
        String inputMQ = IP_ADDR + ":5555:topic";

        JobGroupFormat jobs = new JobGroupFormat();
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, inputMQ));
        job.addOutput(new DataFormat(WEBSOCKET_DATA_TYPE, WEBSOCKET_DATA_SOURCE_OUTPUT));
        job.addOutput(new DataFormat(FILE_DATA_TYPE, FILE_DATA_SOURCE_OUPUT));
        job.addTask(createTrendTask("{\"interval\":{\"data\":3}}"));
        job.addTask(createJsonGeneratorTask("{\"keys\": \"Result\"}"));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void fComplexMovingAverage() {
        JobGroupFormat jobs = new JobGroupFormat();

        // alias Flinck Job A
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addTask(createTask(TaskType.TREND, "ema", "{\"interval\":{\"data\":1}}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"1\"}"));
        jobs.addJob(job);

       // alias Flinck Job C
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addTask(createTask(TaskType.TREND, "ema", "{\"interval\":{\"data\":10}}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"10\"}"));
        jobs.addJob(job);

        // alias Flinck Job D
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ3));
        job.addTask(createTask(TaskType.TREND, "ema", "{\"interval\":{\"data\":20}}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"20\"}"));
        jobs.addJob(job);

        // alias Flinck Job E
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ3));
        job.addOutput(new DataFormat(WEBSOCKET_DATA_TYPE, WEBSOCKET_DATA_SOURCE_OUTPUT));
        job.addTask(createAggregatorTask("{\"keys\": \"1 10 20\", \"aggregateBy\": \"Id\"}"));
        job.addTask(createJsonGeneratorTask("{\"keys\": \"1 10 20\"}"));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void gCNCDemo() {
        JobGroupFormat jobs = new JobGroupFormat();

        // alias Flinck Job A
        JobInfoFormat job = new JobInfoFormat();
//        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addInput(new DataFormat(EMF_DATA_TYPE, EMF_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addTask(createParserTask("{\"delimiter\":\"\\t\",\"index\":\"1 2\"}"));
        job.addTask(createLogisticRegressionTask(
                "{\"weights\":\"0.0227 0.7314\",\"error\":\"-10.8868\",\"type\":\"logistic\"}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"mu\"}"));
        jobs.addJob(job);

       // alias Flinck Job B
        job = new JobInfoFormat();
//        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addInput(new DataFormat(EMF_DATA_TYPE, EMF_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addTask(createParserTask("{\"delimiter\":\"\\t\",\"index\":\"0\"}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"loadsp\"}"));
        jobs.addJob(job);

        // alias Flinck Job C
        job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ1));
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, TEMP_ZMQ2));
        job.addOutput(new DataFormat(WEBSOCKET_DATA_TYPE, WEBSOCKET_DATA_SOURCE_OUTPUT));
        job.addTask(createAggregatorTask("{\"keys\": \"loadsp mu\", \"aggregateBy\": \"Id\"}"));
        job.addTask(createLinearRegressionTask(
                "{\"weights\":\"0.6293 -9.4399\",\"error\":\"0\",\"type\":\"linear\"}"));
        job.addTask(createJsonGeneratorTask("{\"keys\":\"Z-Score\"}"));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }

    @Test
    public void gGformDemo() {
        JobGroupFormat jobs = new JobGroupFormat();

        // alias Flinck Job A
        JobInfoFormat job = new JobInfoFormat();
        job.addInput(new DataFormat(ZMQ_DATA_TYPE, ZMQ_DATA_SOURCE_INPUT));
        job.addOutput(new DataFormat(FILE_DATA_TYPE, "output"));
        job.addTask(createTask(TaskType.CUSTOM, "GformDemo", "{}"));
        jobs.addJob(job);

        String result = jobs.toString();
        Assert.assertNotNull(result);
        LOGGER.debug(getMethodName() + "\n" + result);
    }
}
