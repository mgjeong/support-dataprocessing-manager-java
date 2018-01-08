package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream.Field;

public class Stream {

  public enum Grouping {
    SHUFFLE, FIELDS
  }

  private String id;
  private Schema schema;

  public Stream() {

  }

  public Stream(String id, List<Field> fields) {
    //TODO: fields is not used!
    this.id = id;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }
}
