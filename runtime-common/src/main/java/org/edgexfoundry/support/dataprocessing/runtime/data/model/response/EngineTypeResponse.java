package org.edgexfoundry.support.dataprocessing.runtime.data.model.response;

import io.swagger.annotations.ApiModel;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@ApiModel(value = "EngineTypeResponse", description = "EngineTypeResponse")
public class EngineTypeResponse extends Format {
    private String engineType;

    public EngineTypeResponse(String engineType) {
        this.engineType = engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineType() {
        return this.engineType;
    }
}