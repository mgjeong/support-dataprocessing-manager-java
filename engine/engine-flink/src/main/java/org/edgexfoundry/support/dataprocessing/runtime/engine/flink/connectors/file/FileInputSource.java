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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileInputSource extends RichSourceFunction<DataSet> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileInputSource.class);

  private String mPath = null;
  private String mType = null;
  private String mDelimiter = null;
  private long mInterval = 100L; // 100 msec
  private boolean mFirstLineIsKeys = false;

  //private BufferedReader mBR = null;
  private transient volatile boolean running;

  public FileInputSource(String path, String type) {
    this.mPath = path;
    this.mType = type;

    if (this.mType.equals("csv")) {
      this.mDelimiter = ",";
    } else if (this.mType.equals("tsv")) {
      this.mDelimiter = "\t";
    } else {
      this.mDelimiter = ",";
    }
    LOGGER.debug("Path {}, Type {}", path, type);
  }

  public void readFirstLineAsKeyValues(boolean option) {
    this.mFirstLineIsKeys = option;
  }

  @Override
  public void open(Configuration parameters) throws Exception {

    File file = new File(this.mPath);
    if (!file.exists() || file.isDirectory()) {
      LOGGER.error("File not exist {}", this.mPath);
      throw new Exception("File not exist " + this.mPath);
    } else {
      this.running = true;
    }
  }

  static boolean isNumber(String s) {
    try {
      Double.parseDouble(s);
      LOGGER.debug("It is a number~!! {}", s);
      return true;
    } catch (NumberFormatException e) {
      LOGGER.error("It is not a number~!! {}", s);
      return false;
    }
  }

  @Override
  public void run(SourceContext<DataSet> ctx) throws Exception {

    BufferedReader mBR = null;
    try {
      mBR = new BufferedReader(
          new InputStreamReader(new FileInputStream(this.mPath), Charset.defaultCharset()));

      while (this.running) {

        if (this.mType.equals("csv") || this.mType.equals("tsv")) {
          String line = mBR.readLine();
          if (line == null) {
            LOGGER.error("No header found.");
            break;
          }
          // first line is array of keys
          String[] keys = null;
          if (this.mFirstLineIsKeys) {
            keys = line.split(this.mDelimiter);
            if (keys.length < 1) {
              // Parsing json formatted string line
              LOGGER.error("Error During Extracting Keys from first line {}", line);
              this.running = false;
            }
          }
          // other lines are for values
          while (this.running && ((line = mBR.readLine()) != null)) {
            LOGGER.info("Line : {}", line);
            String[] values = line.split(this.mDelimiter);

            if (values != null && values.length > 0) {

              if (this.mFirstLineIsKeys) {
                if (keys.length != values.length) {
                  LOGGER
                      .error("Length Not Match - keys {} , values {}", keys.length, values.length);
                  this.running = false;
                  break;
                }
              }

              DataSet streamData = DataSet.create();
              for (int index = 0; index < values.length; index++) {
                if (this.mFirstLineIsKeys) {
                  LOGGER.info("Value  Key {} : Value {}", keys[index], values[index]);
                  if (isNumber(values[index])) {
                    streamData.setValue("/" + keys[index],
                        Double.valueOf(values[index]));
                  } else {
                    streamData.setValue("/" + keys[index],
                        values[index].replace("\"", ""));
                  }
                } else {
                  LOGGER.info("Value  Key {} : Value {}", index, values[index]);

                  if (isNumber(values[index])) {
                    streamData.setValue("/" + index,
                        Double.valueOf(values[index]));
                  } else {
                    streamData.setValue("/" + index,
                        values[index].replace("\"", ""));
                  }
                }
              }
              ctx.collect(streamData);
            }
            Thread.sleep(this.mInterval);
          }
          LOGGER.info("File Reading Done");
          this.running = false;
        } else {
          LOGGER.error(this.mType + " file type is not supported");
          this.running = false;
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.error("File Reading Error : {}", e.toString());
    } finally {
      if (mBR != null) {
        mBR.close();
      }
    }
  }

  @Override
  public void cancel() {
    this.running = false;
  }

  @Override
  public void close() throws Exception {
    super.close();

    this.running = false;
  }
}
