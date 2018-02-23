package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileInputSource extends RichSourceFunction<DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileInputSource.class);

  private String path = null;
  private String type = null;
  private String delimiter = null;
  private long interval = 100L; // 100 msec
  private boolean isFirstlineKey = false;

  //private BufferedReader mBR = null;
  private transient volatile boolean running;

  /**
   * Constructs an FileInputSource object to read a file specified.
   * The path argument must specify reachable file path in String. The type argument must describe
   * how the contents of a file is formatted, i.e., csv, tsv, and others.
   *
   * @param path file path of an input file
   * @param type formatted type of an input file
   */
  public FileInputSource(String path, String type) {
    this.path = path;
    this.type = type;

    if (this.type.equals("csv")) {
      this.delimiter = new String(",");
    } else if (this.type.equals("tsv")) {
      this.delimiter = new String("\t");
    } else {
      this.delimiter = new String(",");
    }
    LOGGER.debug("Path {}, Type {}", path, type);
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

  public void readFirstLineAsKeyValues(boolean option) {
    this.isFirstlineKey = option;
  }

  @Override
  public void open(Configuration parameters) throws Exception {

    File file = new File(this.path);
    if (!file.exists() || file.isDirectory()) {
      LOGGER.error("File not exist {}", this.path);
      throw new Exception("File not exist " + this.path);
    } else {
      this.running = true;
    }
  }

  @Override
  public void run(SourceContext<DataSet> ctx) throws Exception {

    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(this.path));

      while (this.running) {

        if (this.type.equals("csv") || this.type.equals("tsv")) {
          String line = br.readLine();
          // first line is array of keys
          String[] keys = null;
          if (this.isFirstlineKey) {
            keys = line.split(this.delimiter);
            if (keys.length < 1) {
              // Parsing json formatted string line
              LOGGER.error("Error During Extracting Keys from first line {}", line);
              this.running = false;
            }
          }
          // other lines are for values
          while (this.running && ((line = br.readLine()) != null)) {
            LOGGER.info("Line : {}", line);
            String[] values = line.split(this.delimiter);

            if (values != null && values.length > 0) {

              if (this.isFirstlineKey) {
                if (keys.length != values.length) {
                  LOGGER
                      .error("Length Not Match - keys {} , values {}", keys.length, values.length);
                  this.running = false;
                  break;
                }
              }

              DataSet streamData = DataSet.create();
              for (int index = 0; index < values.length; index++) {
                if (this.isFirstlineKey) {
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
            Thread.sleep(this.interval);
          }
          LOGGER.info("File Reading Done");
          this.running = false;
        } else {
          LOGGER.error(this.type + " file type is not supported");
          this.running = false;
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.error("File Reading Error : {}", e.toString());
    } finally {
      if (br != null) {
        br.close();
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
