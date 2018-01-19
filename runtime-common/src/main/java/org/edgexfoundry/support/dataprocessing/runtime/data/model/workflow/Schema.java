package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Schema {

  public enum Type {
    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    BINARY(byte[].class),
    NESTED(Map.class),
    ARRAY(List.class),
    BLOB(InputStream.class);

    private final Class<?> javaType;

    Type(Class<?> javaType) {
      this.javaType = javaType;
    }

    public Class<?> getJavaType() {
      return this.javaType;
    }

    public static Schema.Type getTypeOfVal(String val) {
      Schema.Type type = null;
      Schema.Type[] types = values();
      if (val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false"))) {
        type = BOOLEAN;
      }

      for (int i = 1; type == null && i < STRING.ordinal(); ++i) {
        Class clazz = types[i].getJavaType();

        try {
          Object result = clazz.getMethod("valueOf", String.class).invoke((Object) null, val);
          if (!(result instanceof Float) || !((Float) result).isInfinite()) {
            type = types[i];
            break;
          }
        } catch (Exception var6) {
          ;
        }
      }

      if (type == null) {
        type = STRING;
      }

      return type;
    }
  }
}
