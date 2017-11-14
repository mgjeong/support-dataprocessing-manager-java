package org.edgexfoundry.processing.runtime.connection;

/*
import com.google.api.client.testing.http;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
*/
/***
 * This test is performed on httpbin.org site.
 *
 * More details on httpbin can be found at: http://httpbin.org/
 *
 */
public class HTTPTest {
    private static final String HTTP_BIN_HOST = "httpbin.org";
    private static final int HTTP_BIN_PORT = 80;
    private static final String HTTP_SCHEME = "http";

    private static String proxyHost = "TEST_BADHOST";
    private static int proxyPort = 12345;
    private static String proxyScheme = "BADSCHEME";
    private static boolean proxyAvailable = false;

    private static HTTP httpClient = new HTTP();

    //FIXLATER:
    /*
    @BeforeClass
    public static void setProxy() {
        String httpProxy = System.getenv("http_proxy");
        if (httpProxy == null || httpProxy.isEmpty()) {
            System.out.println("No http_proxy found.");
            proxyAvailable = false;
            return;
        }

        String[] fields = httpProxy.split("://");
        if (fields.length > 1) {
            int afterColon = fields[1].lastIndexOf(':') + 1;

            if (afterColon > 0) {
                proxyScheme = fields[0];
                proxyHost = fields[1].substring(0, afterColon - 1);
                String port = fields[1].substring(afterColon, fields[1].length()).replace("/", "");
                proxyPort = Integer.parseInt(port);
                proxyAvailable = true;
            }
        }
        System.out.println("System proxy configuration = " + proxyScheme + "://"
                + proxyHost + ":" + proxyPort);
    }

    @After
    public void nullifyClient() {
        httpClient = new HTTP();
    }

    @Test
    public void requestGetWithNoArgument() {
        try {
            httpClient.get("/get");
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);
            JsonElement jsonResponse = httpClient.get("/get");
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);
            Assert.assertNull(httpClient.get(null));
            return;
        }
        Assert.fail();
    }

    @Test(expected = RuntimeException.class)
    public void badInitialization() {
        String wrongHost = " ";
        int wrongPort = 0;
        try {
            httpClient.initialize(wrongHost, HTTP_BIN_PORT, HTTP_SCHEME);
        } catch (RuntimeException hostException) {
            httpClient.initialize(HTTP_BIN_HOST, wrongPort, HTTP_SCHEME);
        }
        Assert.fail();
    }

    @Test
    public void testTimeout() {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable task = new Callable() {
                public Object call() throws Exception {
                    return makeTimeoutRequest();
                }
            };

            Future future = executor.submit(task);
            future.get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e1) {
            return;
        } catch (Exception e2) {
            Assert.fail();
        }
        Assert.fail();

    }

    private int makeTimeoutRequest() throws Exception {
        httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
        if (!proxyAvailable) {
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);
        }
        JsonElement jsonResponse = httpClient.get("/get");
        Assert.assertNotNull(jsonResponse);
        Assert.assertTrue(jsonResponse instanceof JsonObject);
        Assert.assertNull(httpClient.get(null));
        return 1;
    }

    @Test
    public void requestGetWithOneArgument() {
        JsonElement jsonResponse = null;
        Map<String, String> args = new HashMap<>();
        args.put("first", "3");
        try {
            httpClient.get("/get");
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);
            jsonResponse = httpClient.get("/testbadget", args);

            jsonResponse = httpClient.get("/get", args);
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);

            JsonObject json = jsonResponse.getAsJsonObject();
            JsonElement firstArg = json.get("args").getAsJsonObject().get("first");
            Assert.assertNotNull(firstArg);
            Assert.assertEquals("3", firstArg.getAsString());
            Assert.assertNull(httpClient.get(null, args));

            return;
        }
        Assert.fail();
    }

    @Test
    public void requestGetWithMultipleArguments() {
        JsonElement jsonResponse = null;
        Map<String, String> args = new HashMap<>();
        String[] keys = {"first", "second", "third", "fourth", "5"};
        String[] values = {"3", "3.14", "pi", null, "!@#$%"};

        for (int i = 0; i < keys.length; i++) {
            args.put(keys[i], values[i]);
        }

        try {
            httpClient.get("/get", args);
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);
            jsonResponse = httpClient.get("/get", args);
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);

            JsonObject json = jsonResponse.getAsJsonObject();
            for (int i = 0; i < keys.length; i++) {
                JsonElement arg = json.get("args").getAsJsonObject().get(keys[i]);
                Assert.assertNotNull(arg);
                // null returns empty string
                if (values[i] != null) {
                    Assert.assertEquals(values[i], arg.getAsString());
                } else {
                    //null returns empty string
                    Assert.assertEquals("", arg.getAsString());
                }
            }

            Assert.assertNull(httpClient.get(null, args));
            return;
        }
        Assert.fail();
    }

    @Test
    public void requestDelete() {
        try {
            httpClient.delete("/delete");
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);
            JsonElement jsonResponse = httpClient.delete("/delete");
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);
            Assert.assertNull(httpClient.delete(null));
            return;
        }
        Assert.fail();
    }

    @Test
    public void requestPostFile() {
        File testInput = new File("./HTTPTest_requestPostFile.txt");
        FileOutputStream fos = null;
        if (!testInput.exists()) {
            try {
                fos = new FileOutputStream(testInput);
                fos.write(0);
            } catch (Exception e) {
                Assert.fail();
            } finally {
                try {
                    fos.close();
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        }

        try {
            httpClient.post("/post", testInput);
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);

            JsonElement jsonResponse = httpClient.post("/post", testInput);
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);
            Assert.assertNull(httpClient.post(null, testInput));
        }
        testInput.deleteOnExit();
    }

    @Test
    public void requestPostForm() {

        Map<String, String> args = new HashMap<>();
        String[] keys = {"first", "second", "third", "fourth", "5"};
        String[] values = {"3", "3.14", "pi", null, "!@#$%"};

        for (int i = 0; i < keys.length; i++) {
            args.put(keys[i], values[i]);
        }

        try {
            httpClient.post("/post", args);
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);

            JsonElement jsonResponse = httpClient.post("/post", args);
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);

            JsonObject json = jsonResponse.getAsJsonObject();

            for (int i = 0; i < keys.length; i++) {
                JsonElement arg = json.get("form").getAsJsonObject().get(keys[i]);
                Assert.assertNotNull(arg);

                if (values[i] != null) {
                    Assert.assertEquals(values[i], arg.getAsString());
                } else {
                    //null returns empty string
                    Assert.assertEquals("", arg.getAsString());
                }

            }

            Assert.assertNull(httpClient.post(null, args));
            return;
        }
        Assert.fail();
    }

    @Test
    public void requestPostFormUsingArguments() {

        Map<String, String> args = new HashMap<>();
        String[] keys = {"first", "second", "third", "fourth", "5"};
        String[] values = {"3", "3.14", "pi", null, "!@#$%"};

        for (int i = 0; i < keys.length; i++) {
            args.put(keys[i], values[i]);
        }

        try {
            httpClient.post("/post", args, true);
        } catch (RuntimeException e) {
            httpClient.initialize(HTTP_BIN_HOST, HTTP_BIN_PORT, HTTP_SCHEME);
            httpClient.setProxy(proxyHost, proxyPort, proxyScheme);

            JsonElement jsonResponse = httpClient.post("/post", args, true);
            Assert.assertNotNull(jsonResponse);
            Assert.assertTrue(jsonResponse instanceof JsonObject);

            JsonObject json = jsonResponse.getAsJsonObject();

            for (int i = 0; i < keys.length; i++) {
                JsonElement arg = json.get("args").getAsJsonObject().get(keys[i]);
                Assert.assertNotNull(arg);

                if (values[i] != null) {
                    Assert.assertEquals(values[i], arg.getAsString());
                } else {
                    //null returns empty string
                    Assert.assertEquals("", arg.getAsString());
                }

            }

            Assert.assertNull(httpClient.post(null, args, true));
            return;
        }
        Assert.fail();
    }
    */

//    @Test
//    public void requestPostJar() {
//        File jarFile = new File("./resource/flinktest-1.0-SNAPSHOT.jar");
//        JsonElement jsonResponse = httpClient.post("/post", jarFile);
//
//        Assert.assertNotNull(jsonResponse);
//        Assert.assertTrue(jsonResponse instanceof JsonObject);
//
//        JsonObject json = jsonResponse.getAsJsonObject();
//        System.out.println(json);
//    }
//
//    private String flinkUploadJar(HTTP httpClientFlink){
//        File jarFile = new File("./resource/flinktest-1.0-SNAPSHOT.jar");
//
//        JsonElement jsonResponse = httpClientFlink.post("/jars/upload", jarFile);
//        Assert.assertNotNull(jsonResponse);
//        Assert.assertTrue(jsonResponse instanceof JsonObject);
//
//        JsonObject json = jsonResponse.getAsJsonObject();
//
//        Assert.assertEquals(json.get("status").getAsString(), "success");
//        return json.get("filename").getAsString();
//    }
//
//    private String flinkExecuteJob(HTTP httpClientFlink, String uploadedFilename){
//        Map<String, String> args = new HashMap<>();
//        args.put("program-args", "--port 9000");
//        args.put("entry-class", "SocketWindowWordCount");
//        args.put("parallelism", "1");
//
//        JsonElement jsonResponse = httpClientFlink.post("/jars/" + uploadedFilename + "/run", args, true);
//        Assert.assertNotNull(jsonResponse);
//        Assert.assertTrue(jsonResponse instanceof JsonObject);
//
//        return jsonResponse.getAsJsonObject().get("jobid").getAsString();
//    }
//
//    @Test
//    public void flinkUploadJarAndExecuteThenDestroy() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        HTTP httpClientFlink = new HTTP();
//        httpClientFlink.initialize("localhost", 8081, "http");
//
//        // Upload Jar
//        String uploadedFilename = flinkUploadJar(httpClientFlink);
//
//        // Start execution
//        String jobId = flinkExecuteJob(httpClientFlink, uploadedFilename);
//
//        // Retrieved jobId
//        System.out.println("Job Id: " + jobId);
//
//        // Get job detail
//        JsonElement jobOverview = httpClientFlink.get("/jobs").getAsJsonObject();
//        System.out.println("Job Overview: "+gson.toJson(jobOverview));
//
//        // Wait before termination
//        try {
//            Thread.sleep(1500L);
//        } catch (InterruptedException e) {
//        }
//
//        // Destroy job
//        JsonElement jobDestroyed = httpClientFlink.delete("/jobs/"+jobId+"/cancel");
//        System.out.println("Job Destroyed: " + gson.toJson(jobDestroyed));
//
//        // Get job detail
//        jobOverview = httpClientFlink.get("/jobs").getAsJsonObject();
//        System.out.println("Job Overview: "+gson.toJson(jobOverview));
//    }
}
