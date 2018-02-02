package org.edgexfoundry.support.dataprocessing.runtime.task;

import com.fasterxml.jackson.annotation.JsonValue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskParam {

  String uiName();

  UiFieldType uiType();

  String key();

  String defaultValue() default "";

  String tooltip() default "";

  boolean isOptional() default false;

  enum UiFieldType {
    STRING("string"),
    ENUMSTRING("enumstring"),
    ARRAYSTRING("array.string"),
    ARRAYENUMSTRING("array.enumstring"),
    NUMBER("number"),
    ARRAYNUMBER("array.number"),
    BOOLEAN("boolean"),
    ARRAYBOOLEAN("array.boolean"),
    OBJECT("object"),
    ENUMOBJECT("enumobject"),
    ARRAYOBJECT("array.object"),
    ARRAYENUMOBJECT("array.enumobject"),
    FILE("file");

    private String uiFieldTypeText;

    UiFieldType(String uiFieldTypeText) {
      this.uiFieldTypeText = uiFieldTypeText;
    }

    @JsonValue
    public String getUiFieldTypeText() {
      return this.uiFieldTypeText;
    }
  }
}
