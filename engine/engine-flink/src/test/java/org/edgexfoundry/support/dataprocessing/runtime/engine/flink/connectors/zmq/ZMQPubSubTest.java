package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq;

import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.Assert;
import org.junit.Test;
import org.zeromq.ZMQ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZMQPubSubTest {
    private static final String ZMQ_ADDRESS = "tcp://localhost:5555";
    private static final String ZMQ_TOPIC = "topic";

    private static long sentAt = 0L;

    @Test
    public void testPubSubListOfStreamData() {
        ZMQSubscriber subscriber = new ZMQSubscriber();
        ZMQPublisher publisher = new ZMQPublisher();
        try {
            publisher.start();
            subscriber.start();

            subscriber.join();
            publisher.join();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            subscriber.terminate();
            publisher.terminate();
        }
    }

    private static List<DataSet> deserialize(byte[] b) throws Exception {
        ByteArrayInputStream bis = null;
        ObjectInput in = null;
        try {
            bis = new ByteArrayInputStream(b);
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return (List<DataSet>) o;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] serialize(List<DataSet> data) throws Exception {
        ByteArrayOutputStream bos = null;
        ObjectOutput out = null;
        try {
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.flush();

            byte[] b = bos.toByteArray();
            return b;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ZMQSubscriber extends Thread {
        private ZMQ.Context context = null;
        private ZMQ.Socket socket = null;

        @Override
        public void run() {
            // Make ZMQ connection
            context = ZMQ.context(1);
            socket = context.socket(ZMQ.SUB);

            try {
                socket.connect(ZMQ_ADDRESS);
                socket.subscribe(ZMQ_TOPIC.getBytes());

                System.out.println("Subscriber connected.");
                String topic = socket.recvStr();
                System.out.println("Topic received: " + topic);
                byte[] bDummy = socket.recv(); // data

                long elapsed = System.currentTimeMillis() - sentAt;
                System.out.println("Received! size: " + bDummy.length + " elapsed: " + elapsed + " ms");

                List<DataSet> dummyData = deserialize(bDummy);
                Assert.assertNotNull(dummyData);
                for (DataSet s : dummyData) {
                    System.out.println(s.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void terminate() {
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
            if (this.context != null) {
                this.context.close();
                this.context = null;
            }
        }
    }

    private static class ZMQPublisher extends Thread {
        private ZMQ.Context context = null;
        private ZMQ.Socket socket = null;

        @Override
        public void run() {
            // Make ZMQ connection
            context = ZMQ.context(1);
            socket = context.socket(ZMQ.PUB);

            try {
                socket.bind(ZMQ_ADDRESS);

                System.out.println("Waiting 1.5 seconds for subscribers to connect...");
                Thread.sleep(1500L);

                // Make a list of dummy StreamData
                List<DataSet> dummyData = makeDummyData();
                System.out.println("Dummy data set generated. Size=" + dummyData.size());
                byte[] bDummy = serialize(dummyData);

                sentAt = System.currentTimeMillis();
                socket.sendMore(ZMQ_TOPIC.getBytes());
                socket.send(bDummy);
                System.out.println("Sent! size: " + bDummy.length);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void terminate() {
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
            if (this.context != null) {
                this.context.close();
                this.context = null;
            }
        }

        private List<DataSet> makeDummyData() {
            Random random = new Random();
            List<DataSet> dummyDataSet = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                // make DataSet
                DataSet streamData = DataSet.create();
                for (int j = 0; j < 20; j++) {
                    streamData.setValue("/" + i, random.nextDouble());
                }

                dummyDataSet.add(streamData);
            }
            return dummyDataSet;
        }
    }
}
