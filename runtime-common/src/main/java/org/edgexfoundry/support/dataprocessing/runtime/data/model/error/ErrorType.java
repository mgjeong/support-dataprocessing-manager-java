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

import java.io.Serializable;

public enum ErrorType implements Serializable {
  DPFW_ERROR_NONE,
  DPFW_ERROR_INVALID_PARAMS,
  DPFW_ERROR_PERMISSION,
  DPFW_ERROR_DB,
  DPFW_ERROR_ENGINE_FLINK,
  DPFW_ERROR_FULL_JOB,
  DPFW_ERROR_CONNECTION_ERROR,
  DPFW_ERROR_ENGINE_TYPE
}

