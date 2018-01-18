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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@ApiModel(value = "Result", description = "Result")
public class ErrorFormat extends Format {

  @ApiModelProperty(required = true)
  private ErrorType errorCode;
  @ApiModelProperty(required = true)
  private String responseMessage;

  public ErrorFormat() {
    this(ErrorType.DPFW_ERROR_NONE, "Success.");
  }

  public ErrorFormat(ErrorType errorCode) {
    this(errorCode, "Success.");
  }

  public ErrorFormat(ErrorType errorCode, String responseMessage) {
    setErrorCode(errorCode);
    setResponseMessage(responseMessage);
  }

  public ErrorType getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ErrorType errorCode) {
    this.errorCode = errorCode;
  }

  public String getResponseMessage() {
    return responseMessage;
  }

  public void setResponseMessage(String responseMessage) {
    this.responseMessage = responseMessage;
  }

  @JsonIgnore
  public Boolean isError() {
    return (this.errorCode != ErrorType.DPFW_ERROR_NONE);
  }
}
