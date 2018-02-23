/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
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
package org.edgexfoundry.support.dataprocessing.runtime.controller;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AbstractControllerTest {

  @Test
  public void testRespond() throws Exception {
    Method method = AbstractController.class
        .getDeclaredMethod("respond", Object.class, HttpStatus.class);
    method.setAccessible(true);

    ResponseEntity response = (ResponseEntity) method
        .invoke(AbstractController.class, "hello", HttpStatus.OK);
    Assert.assertNotNull(response);
  }

  @Test
  public void testRespondEntity() throws Exception {
    Method method = AbstractController.class
        .getDeclaredMethod("respondEntity", Object.class, HttpStatus.class);
    method.setAccessible(true);

    ResponseEntity response = (ResponseEntity) method
        .invoke(AbstractController.class, "hello", HttpStatus.OK);
    Assert.assertNotNull(response);
    Assert.assertTrue(response.getBody().toString().contains("entities")); // entities
  }

  @Test
  public void testConstructor() {
    AbstractController controller = new AbstractController() {
    };
    Assert.assertNotNull(controller);
  }
}
