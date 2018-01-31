package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import com.google.gson.annotations.SerializedName;

public class FlinkException {

  @SerializedName("root-exception")
  private String rootException;


  public String getRootException() {
    return rootException;
  }

  public void setRootException(String rootException) {
    this.rootException = rootException;
  }
}
