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
package org.edgexfoundry.support.dataprocessing.runtime.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTP implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTP.class);

    private HttpClient client = null;
    private HttpHost proxy = null;
    private URIBuilder uriBuilder = null;

    private boolean initialized = false;

    private JsonParser jsonParser = null;

    public HTTP() {

    }

    /***
     * Initialize HTTP.
     *
     * @param host hostname
     * @param port port number
     * @param scheme    e.g.: http, https
     */
    public void initialize(String host, int port, String scheme) {
        if (host == null || port <= 0) {
            throw new RuntimeException("Invalid host or port entered.");
        }

        host = host.trim();
        if (host.isEmpty()) {
            throw new RuntimeException("Invalid host or port entered.");
        }

        this.client = HttpClients.createDefault();

        this.jsonParser = new JsonParser();

        this.uriBuilder = new URIBuilder();
        this.uriBuilder.setScheme(scheme).setHost(host).setPort(port);

        this.initialized = true;
    }

    public void setProxy(String host, int port, String scheme) {
        this.proxy = new HttpHost(host, port, scheme);
    }

    private boolean isProxyAvailable() {
        return this.proxy != null;
    }

    private URI createUri(String path, Map<String, String> args) throws URISyntaxException {
        this.uriBuilder.clearParameters();
        this.uriBuilder.setPath(path);

        if (args != null && args.size() > 0) {
            for (Map.Entry<String, String> entry : args.entrySet()) {
                this.uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        return this.uriBuilder.build();
    }

    private HttpResponse executeRequest(HttpRequestBase request) throws IOException {
        HttpResponse response;

        if (isProxyAvailable()) {
            response = this.client.execute(this.proxy, request);
        } else {
            response = this.client.execute(request);
        }
        return response;
    }

    public JsonElement get(String path) {
        return get(path, null);
    }

    public JsonElement get(String path, Map<String, String> args) {
        throwExceptionIfNotInitialized();

        try {
            URI uri = createUri(path, args);
            HttpGet request = new HttpGet(uri);
            HttpResponse response = executeRequest(request);

            int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(httpStatusCode, String.format("Bad HTTP status: %d", httpStatusCode));
            }

            String rawJson = EntityUtils.toString(response.getEntity());

            return this.jsonParser.parse(rawJson);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public JsonElement delete(String path) {
        throwExceptionIfNotInitialized();

        try {
            URI uri = createUri(path, null);
            HttpDelete request = new HttpDelete(uri);
            HttpResponse response = executeRequest(request);

            int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(httpStatusCode, String.format("Bad HTTP status: %d", httpStatusCode));
            }

            String rawJson = EntityUtils.toString(response.getEntity());

            return this.jsonParser.parse(rawJson);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public JsonElement post(String path, File fileToUpload) {
        throwExceptionIfNotInitialized();
        try {
            URI uri = createUri(path, null);
            HttpPost request = new HttpPost(uri);
            //request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-java-archive");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", fileToUpload);

            request.setEntity(builder.build());

            HttpResponse response = executeRequest(request);

            int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(httpStatusCode, String.format("Bad HTTP status: %d", httpStatusCode));
            }

            String rawJson = EntityUtils.toString(response.getEntity());

            return this.jsonParser.parse(rawJson);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public JsonElement post(String path, Map<String, String> args) {
        return post(path, args, false);
    }

    public JsonElement post(String path, Map<String, String> args, boolean useArgAsParam) {
        throwExceptionIfNotInitialized();

        try {
            URI uri;
            if (useArgAsParam) {
                uri = createUri(path, args);
            } else {
                uri = createUri(path, null);
            }

            HttpPost request = new HttpPost(uri);

            if (!useArgAsParam) {
                List<NameValuePair> entity = new ArrayList<>();
                if (args != null && args.size() > 0) {
                    for (Map.Entry<String, String> arg : args.entrySet()) {
                        entity.add(new BasicNameValuePair(arg.getKey(), arg.getValue()));
                    }
                }
                request.setEntity(new UrlEncodedFormEntity(entity));
            }

            HttpResponse response = executeRequest(request);

            int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(httpStatusCode, String.format("Bad HTTP status: %d", httpStatusCode));
            }

            String rawJson = EntityUtils.toString(response.getEntity());

            return this.jsonParser.parse(rawJson);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    private void throwExceptionIfNotInitialized() {
        if (!this.initialized) {
            throw new RuntimeException(HTTP.class.getSimpleName() + " is not initialized.");
        }
    }

}