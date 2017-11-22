package org.edgexfoundry.support.dataprocessing.runtime;

import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;

public class RuntimeHost {

        private String hostIP;
        private int port;
        private String schema = "http";


        private HTTP httpClient;

        private static RuntimeHost instance;

        private RuntimeHost() {

        }

        public static RuntimeHost getInstance() {

            if(null == instance) {
                if(null == instance) {
                    instance = new RuntimeHost();
                }
            }

            return instance;
        }

        public void open() {
            httpClient = new HTTP();
            httpClient.initialize(getHostIP(), getPort(), getSchema());
        }

        public HTTP getHttpClient() {
            return this.httpClient;
        }

        public void setHostIP(String host) {
            this.hostIP = host;
        }
        public String getHostIP() {
            return this.hostIP;
        }

        public void setPort(int port) {
            this.port = port;
        }
        public int getPort() {
            return this.port;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
        public String getSchema() {
            return this.schema;
        }
    }