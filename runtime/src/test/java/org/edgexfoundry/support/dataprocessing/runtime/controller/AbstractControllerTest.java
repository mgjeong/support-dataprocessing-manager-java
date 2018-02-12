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
