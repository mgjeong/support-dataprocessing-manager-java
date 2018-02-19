package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.google.gson.annotations.SerializedName;

public class KapacitorJob {

  private String id;
  private String status;
  private boolean executing;
  private String error;

  private String created;
  private String modified;

  @SerializedName("last-enabled")
  private String lastEnabled;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isExecuting() {
    return executing;
  }

  public void setExecuting(boolean executing) {
    this.executing = executing;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getLastEnabled() {
    return lastEnabled;
  }

  public void setLastEnabled(String lastEnabled) {
    this.lastEnabled = lastEnabled;
  }
}
