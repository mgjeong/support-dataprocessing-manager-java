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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

public class FlinkTask {

  private int total;
  private int pending;
  private int running;
  private int finished;
  private int canceling;
  private int canceled;
  private int failed;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getPending() {
    return pending;
  }

  public void setPending(int pending) {
    this.pending = pending;
  }

  public int getRunning() {
    return running;
  }

  public void setRunning(int running) {
    this.running = running;
  }

  public int getFinished() {
    return finished;
  }

  public void setFinished(int finished) {
    this.finished = finished;
  }

  public int getCanceling() {
    return canceling;
  }

  public void setCanceling(int canceling) {
    this.canceling = canceling;
  }

  public int getFailed() {
    return failed;
  }

  public void setFailed(int failed) {
    this.failed = failed;
  }

  public int getCanceled() {
    return canceled;
  }

  public void setCanceled(int canceled) {
    this.canceled = canceled;
  }
}
