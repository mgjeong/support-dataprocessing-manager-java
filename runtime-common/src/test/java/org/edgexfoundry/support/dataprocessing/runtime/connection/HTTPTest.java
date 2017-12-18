package org.edgexfoundry.support.dataprocessing.runtime.connection;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javassist.bytecode.annotation.MemberValue;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
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

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


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
        try {

            HTTP httpClinet = new HTTP().initialize(hostIPAddress + ":" + hostPort, schema);
            Assert.assertNotNull(httpClinet);

            httpClinet = new HTTP().initialize(hostIPAddress, hostPort, schema);
            Assert.assertNotNull(httpClinet);

        } catch (RuntimeException e) {

            e.printStackTrace();
            Assert.fail();

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

        http.get(anyString());
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

        Map<String, String> map = new HashMap<>();
        http.get(anyString(), map, "/runtime/ha", "test.jar");
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
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
