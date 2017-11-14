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
package org.edgexfoundry.processing.runtime.data.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Format implements Serializable, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Format.class);

    public static <T> T create(String data, Class<T> classType) {
        T object = null;
        try {
            object = new ObjectMapper().readValue(data, classType);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return object;
    }

    @Override
    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return obj;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String string = null;
        try {
            string = mapper.writeValueAsString(this);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return string;
    }
}