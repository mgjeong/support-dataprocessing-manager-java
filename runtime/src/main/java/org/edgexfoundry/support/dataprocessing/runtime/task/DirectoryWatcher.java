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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);

  private final String watchingDirectory = Settings.DOCKER_PATH;
  private WatchService watchService = null;
  private DirectoryChangeEventListener listener = null;

  private Path path = null;

  private boolean isRun = false;

  public DirectoryWatcher() {
    this(null, null);
  }

  public DirectoryWatcher(String watchingPath) {
    this(watchingPath, null);
  }

  public DirectoryWatcher(DirectoryChangeEventListener listener) {
    this(null, listener);
  }

  public void setDirectoryChangeEventListener(DirectoryChangeEventListener listener) {
    this.listener = listener;
  }

  public DirectoryWatcher(String watchingPath, DirectoryChangeEventListener listener) {

    if (null != watchingPath) {
      path = Paths.get(watchingPath);
    } else {
      path = Paths.get(this.watchingDirectory);
    }

    try {

      watchService = path.getFileSystem().newWatchService();
      path.register(watchService, ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
      isRun = true;
      setDirectoryChangeEventListener(listener);

    } catch (IOException e) {

      watchService = null;
      isRun = false;

      LOGGER.error(e.getMessage(), e);
    }
  }

  public ArrayList<String> scanFile(String absPath) {

    ArrayList<String> fileList = new ArrayList<String>();
    File directory = null;

    if (null == absPath) {
      absPath = this.watchingDirectory;
    }

    directory = new File(absPath);

    for (final File file : directory.listFiles()) {
      if (file.isDirectory()) {
        continue;
      } else {
        fileList.add(file.getAbsolutePath());
      }
    }

    return fileList;
  }

  public void stopWatcher() {

    isRun = false;

    super.stop();

  }

  public void run() {

    if (null != watchService) {
      WatchKey watchKey = null;
      while (isRun) {
        try {
          sleep(500);

          watchKey = watchService.poll(1, TimeUnit.MINUTES);

          if (null != watchKey) {
            for (WatchEvent event : watchKey.pollEvents()) {
              WatchEvent.Kind kind = event.kind();

              Path name = ((WatchEvent<Path>) event).context();

              if (null != listener) {

                if (kind == ENTRY_CREATE) {
                  listener.fileCreatedEventReceiver(name.toAbsolutePath().toString());
                } else if (kind == ENTRY_DELETE) {
                  listener.fileRemovedEventReceiver(name.toAbsolutePath().toString());
                }

              }
            }
          }

          watchKey.reset();
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    }
  }
}
