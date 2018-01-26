package org.edgexfoundry.support.dataprocessing.runtime.data.model.error;


import org.junit.Assert;
import org.junit.Test;

public class ErrorFormatTest {

  @Test
  public void initError() {

    ErrorFormat error1 = new ErrorFormat();
    Assert.assertNotNull(error1);

    ErrorFormat error2 = new ErrorFormat(ErrorType.DPFW_ERROR_NONE);
    Assert.assertNotNull(error2);

    ErrorFormat error3 = new ErrorFormat(ErrorType.DPFW_ERROR_NONE, "Test Cases");
    Assert.assertNotNull(error3);

  }

  @Test
  public void errorTypeTest() {
    ErrorFormat error = new ErrorFormat();
    for (ErrorType errorType : ErrorType.values()) {
      error.setErrorCode(errorType);
      Assert.assertEquals(error.getErrorCode(), errorType);
    }
  }

  @Test
  public void getErrorCodeTest() {

    ErrorFormat errorFormat = new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS);
    Assert.assertEquals(ErrorType.DPFW_ERROR_INVALID_PARAMS, errorFormat.getErrorCode());

  }

  @Test
  public void setErrorCodeTest() {

    ErrorFormat errorFormat = new ErrorFormat(ErrorType.DPFW_ERROR_NONE);
    errorFormat.setErrorCode(ErrorType.DPFW_ERROR_INVALID_PARAMS);

    Assert.assertEquals(ErrorType.DPFW_ERROR_INVALID_PARAMS, errorFormat.getErrorCode());

  }

  @Test
  public void getErrorMessageTest() {

    String message = "Test Case";
    ErrorFormat errorFormat = new ErrorFormat(ErrorType.DPFW_ERROR_NONE, message);

    Assert.assertEquals(message, errorFormat.getResponseMessage());

  }

  @Test
  public void setErrorMessageTest() {

    String message = "Test Case";
    ErrorFormat errorFormat = new ErrorFormat();
    errorFormat.setResponseMessage(message);

    Assert.assertEquals(message, errorFormat.getResponseMessage());

  }

  @Test
  public void isErrorTest() {
    ErrorFormat errorFormat = new ErrorFormat(ErrorType.DPFW_ERROR_NONE);
    Assert.assertFalse(errorFormat.isError());

    errorFormat.setErrorCode(ErrorType.DPFW_ERROR_INVALID_PARAMS);
    Assert.assertTrue(errorFormat.isError());
  }

  @Test
  public void isNoErrorTest() {
    ErrorFormat errorFormat = new ErrorFormat(ErrorType.DPFW_ERROR_NONE);
    Assert.assertFalse(errorFormat.isError());

    errorFormat.setErrorCode(ErrorType.DPFW_ERROR_INVALID_PARAMS);
    Assert.assertTrue(errorFormat.isError());
  }


}
