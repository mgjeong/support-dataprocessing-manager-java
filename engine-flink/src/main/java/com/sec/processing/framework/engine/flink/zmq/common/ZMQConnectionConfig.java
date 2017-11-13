package com.sec.processing.framework.engine.flink.zmq.common;

import java.io.Serializable;

public class ZMQConnectionConfig implements Serializable {
    private String host;
    private int port;
    private int ioThreads;

    public ZMQConnectionConfig(String host, int port, int ioThreads) {
        this.host = host;
        this.port = port;
        this.ioThreads = ioThreads;
    }

    public int getIoThreads() {
        return this.ioThreads;
    }

    public String getConnectionAddress() {
        return String.format("tcp://%s:%d", this.host, this.port);
    }

    public static class Builder {
        private String host;
        private int port;
        private int ioThreads;

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setIOThreads(int ioThreads){
            this.ioThreads = ioThreads;
            return this;
        }

        public ZMQConnectionConfig build() {
            return new ZMQConnectionConfig(this.host, this.port, this.ioThreads);
        }
    }
}
