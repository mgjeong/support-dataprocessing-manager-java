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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class DirectoryWatcherTest {

  private static boolean isCalled = false;

  private CountDownLatch lock = new CountDownLatch(1);

  private static DirectoryWatcher watcher = null;

  private static DirectoryChangeEventListener listener = new DirectoryChangeEventListener() {
    @Override
    public ErrorFormat fileRemovedEventReceiver(String fileName) {
      System.out.println("REMOVED : " + fileName);
      isCalled = true;
      return new ErrorFormat();
    }

    @Override
    public ErrorFormat fileCreatedEventReceiver(String fileName) {
      System.out.println("CREATED : " + fileName);
      isCalled = true;
      return new ErrorFormat();
    }
  };

  @AfterClass
  public static void dDestroyWatcher() {
    try {
      watcher.stopWatcher();
      watcher.join(2000);
      watcher = null;
    } catch (InterruptedException e) {
      System.out.println("InterruptedException occured during unit test");
    }

    Assert.assertNull(watcher);
  }

  @Test
  public void bFileCreateEventReceive() {
    watcher = new DirectoryWatcher(".");
    watcher.setDirectoryChangeEventListener(listener);
    watcher.start();
    Assert.assertNotNull(watcher);

    isCalled = false;

    File file = new File("./TEST.TXT");
    try {
      file.createNewFile();
      lock.await(2000, TimeUnit.MILLISECONDS);
    } catch (IOException e) {
      System.out.println("IOException on opening file " + file.toString());
      Assert.fail();
    } catch (InterruptedException e) {
      System.out.println("InterruptedException on opening file " + file.toString());
      Assert.fail();

    } finally {
      file.delete();
    }

    Assert.assertTrue(isCalled);
  }

  @Test
  public void cFileRemoveEventReceive() {
    // Force to generate IOException by setting the wrong path.
    watcher = new DirectoryWatcher(".");
    watcher.setDirectoryChangeEventListener(listener);
    watcher.start();
    Assert.assertNotNull(watcher);

    isCalled = false;

    File file = new File("./TEST2.TXT");
    try {
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    } finally {
      file.delete();

    }

    try {
      lock.await(2000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Assert.assertTrue(isCalled);

    // Exception test
    watcher.stopWatcher();
  }

  @Test
  public void scanFileOnDefaultDirectory() {
    watcher = new DirectoryWatcher(listener);
    watcher.start();
    watcher.interrupt();

    Assert.assertNotNull(watcher);

    ArrayList<String> list = watcher.scanFile(".");

    for (String fname : list) {
      System.out.println(fname);
    }

    Assert.assertTrue(0 < list.size());
  }
}
