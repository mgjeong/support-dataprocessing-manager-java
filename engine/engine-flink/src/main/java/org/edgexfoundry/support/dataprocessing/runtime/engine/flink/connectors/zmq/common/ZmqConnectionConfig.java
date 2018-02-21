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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common;

import java.io.Serializable;

public class ZmqConnectionConfig implements Serializable {

  private String host;
  private int port;
  private int ioThreads;

  public ZmqConnectionConfig(String host, int port, int ioThreads) {
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

    public Builder setIoThreads(int ioThreads) {
      this.ioThreads = ioThreads;
      return this;
    }

    public ZmqConnectionConfig build() {
      return new ZmqConnectionConfig(this.host, this.port, this.ioThreads);
    }
  }
}
