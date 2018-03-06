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

package org.edgexfoundry.support.dataprocessing.runtime.connection;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/***
 * This test is performed on httpbin.org site.
 *
 * More details on httpbin can be found at: http://httpbin.org/
 *
 */
public class HTTPTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HTTP.class);


  private String hostIPAddress = "localhost";
  private int hostPort = 8080;

  private String schema = "http";


  @PrepareForTest
  public void initTest() {

    mockStatic(HttpClient.class);
    mockStatic(EntityUtils.class);

  }

  @Test
  public void initializeHttp() {
    HTTP httpClinet;

    try {

      httpClinet = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      Assert.assertNotNull(httpClinet);

      httpClinet = new HTTP().initialize(hostIPAddress, hostPort, schema);
      Assert.assertNotNull(httpClinet);

      httpClinet.setProxy(hostIPAddress, hostPort, schema);

    } catch (RuntimeException e) {

      e.printStackTrace();
      Assert.fail();
    }

    // Negative Test cases.
    try {
      httpClinet = new HTTP().initialize(null, 1234, null);
      Assert.assertNull(httpClinet);
    } catch (RuntimeException e) {
      e.printStackTrace();
    }

    try {
      httpClinet = new HTTP().initialize("", 1234, null);
      Assert.assertNull(httpClinet);
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }


  @Test
  public void testHttpGet() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void nagTestHttpGet() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(400);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testHttpDownload() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void nagTestHttpDownload() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(400);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testHttpDelete() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }

    try {
      http.delete(anyString());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void NagtestHttpDelete() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }

  }

  @Test
  public void testHttpPostWithFlag() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testHttpPost() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testHttpPostToFileUpload() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testHttpPostData() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testHttpPatchData() {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    HttpResponse httpResponse = Mockito.mock(HttpResponse.class,
        Mockito.RETURNS_DEEP_STUBS);
    StatusLine statusLine = Mockito.mock(StatusLine.class);

//        httpResponse.

    JsonElement json = new JsonParser().parse("{}");

    HTTP http = null;
    HttpEntity entity = EntityBuilder.create()
        .setText("{}")
        .setContentType(ContentType.APPLICATION_JSON)
        .build();

    try {
      when(statusLine.getStatusCode()).thenReturn(200);
      when(httpResponse.getStatusLine()).thenReturn(statusLine);
      when(httpResponse.getEntity()).thenReturn(entity);
      when(httpClient.execute(any(HttpUriRequest.class)))
          .thenReturn(httpResponse);

      http = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
      MemberModifier.field(HTTP.class, "client")
          .set(http, httpClient);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
