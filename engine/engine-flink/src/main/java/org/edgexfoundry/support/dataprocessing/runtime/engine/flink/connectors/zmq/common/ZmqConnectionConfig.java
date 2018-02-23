package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common;

import java.io.Serializable;

public class ZmqConnectionConfig implements Serializable {

  private String host;
  private int port;
  private int ioThreads;

  /**
   * Class constructor specifying ZeroMQ configurations.
   *
   * @param host hostname or IP address of target ZeroMQ
   * @param port port number of target ZeroMQ
   * @param ioThreads the number of IO threads to read from/write to ZeroMQ
   */
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
