package com.sec.processing.framework.util;


import org.edgexfoundry.processing.runtime.task.TaskModel;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;


public class TaskLoaderTest {

    @Test
    public void testTaskLoader() {
        ClassLoader classLoader = this.getClass().getClassLoader();

        try {
            TaskModelLoader loader = new TaskModelLoader("/jar/path.jar", classLoader);
            Assert.assertNotNull(loader);

            TaskModel tm = loader.newInstance("svm");
            Assert.assertNotNull(tm);

        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException handling");
        } catch (Exception e) {
            System.err.println("Exception handling");
        }
    }
}
