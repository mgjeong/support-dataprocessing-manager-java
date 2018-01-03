package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentUISpecification extends Format{

    private List<UIField> fields;

    public ComponentUISpecification() {
        this.fields = new ArrayList<>();
    }

    public List<UIField> getFields() {
        return fields;
    }

    public void setFields(List<UIField> fields) {
        this.fields = fields;
    }

    public void addUIField(UIField uiField) {
        if (uiField == null) {
            return;
        }
        this.fields.add(uiField);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UIField {
        private String uiName;
        private String fieldName;
        private Boolean isUserInput;
        private String tooltip;
        private Boolean isOptional;
        private String type;
        private String defaultValue;

        public UIField() {

        }

        public String getUiName() {
            return uiName;
        }

        public void setUiName(String uiName) {
            this.uiName = uiName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public boolean isUserInput() {
            return isUserInput;
        }

        public void setUserInput(boolean userInput) {
            isUserInput = userInput;
        }

        public String getTooltip() {
            return tooltip;
        }

        public void setTooltip(String tooltip) {
            this.tooltip = tooltip;
        }

        public boolean isOptional() {
            return isOptional;
        }

        public void setOptional(boolean optional) {
            isOptional = optional;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
