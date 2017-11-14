/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import io.swagger.annotations.ApiModelProperty;

public class DataFormat extends Format {
    @ApiModelProperty(required = true)
    private String dataType = null;
    @ApiModelProperty(required = true)
    private String dataSource = null;

    public DataFormat() {
        this(null, null);
    }

    public DataFormat(String dataType, String dataSource) {
        setDataType(dataType);
        setDataSource(dataSource);
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
