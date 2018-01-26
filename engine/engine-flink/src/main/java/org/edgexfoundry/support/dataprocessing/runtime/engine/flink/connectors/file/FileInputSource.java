package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.file;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileInputSource extends RichSourceFunction<DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileInputSource.class);

  private String mPath = null;
  private String mType = null;
  private String mDelimiter = null;
  private long mInterval = 100L; // 100 msec
  private boolean mFirstLineIsKeys = false;

  private BufferedReader mBR = null;
  private transient volatile boolean running;

  public FileInputSource(String path, String type) {
    this.mPath = path;
    this.mType = type;

    if (this.mType.equals("csv")) {
      this.mDelimiter = new String(",");
    } else if (this.mType.equals("tsv")) {
      this.mDelimiter = new String("\t");
    } else {
      this.mDelimiter = new String(",");
    }
    LOGGER.debug("Path {}, Type {}", path, type);
  }

  public void readFirstLineAsKeyValues(boolean option) {
    this.mFirstLineIsKeys = option;
  }
  @Override
  public void open(Configuration parameters) throws Exception {

    File file = new File(this.mPath);
    if (file.exists() && !file.isDirectory()) {
      mBR = new BufferedReader(new FileReader(this.mPath));

    } else {
      LOGGER.error("File not exist {}", this.mPath);
    }

    this.running = true;
  }

  static boolean isNumber(String s) {
    final int len = s.length();
    if (len == 0) {
      return false;
    }
    int dotCount = 0;
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        if (i == len - 1) {//last character must be digit
          return false;
        } else if (c == '.') {
          if (++dotCount > 1) {
            return false;
          }
        } else if (i != 0 || c != '+' && c != '-') {//+ or - allowed at start
          return false;
        }

      }
    }
    return true;
  }

  @Override
  public void run(SourceContext<DataSet> ctx) throws Exception {

    while (this.running) {

      if (this.mType.equals("csv") || this.mType.equals("tsv")) {
        String line = mBR.readLine();
        // first line is array of keys
        String[] Keys = null;
        if(this.mFirstLineIsKeys) {
          Keys = line.split(this.mDelimiter);
          if (Keys.length < 1) {
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
              if (Keys.length != values.length) {
                LOGGER
                    .error("Length Not Match - keys {} , values {}", Keys.length, values.length);
                this.running = false;
                break;
              }
            }

            DataSet streamData = DataSet.create();
            for (int index = 0; index < values.length; index++) {
              if (this.mFirstLineIsKeys) {
                LOGGER.info("Value  Key {} : Value {}", Keys[index], values[index]);
                if (isNumber(values[index])) {
                  streamData.setValue("/" + Keys[index],
                      Double.valueOf(values[index]));
                } else {
                  streamData.setValue("/" + Keys[index],
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
        LOGGER.error(this.mType+" file type is not supported");
        this.running = false;
        break;
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
