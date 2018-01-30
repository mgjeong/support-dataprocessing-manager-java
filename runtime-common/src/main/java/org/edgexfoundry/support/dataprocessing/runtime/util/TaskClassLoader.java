package org.edgexfoundry.support.dataprocessing.runtime.util;

import java.net.URL;
import java.net.URLClassLoader;

public class TaskClassLoader extends URLClassLoader {

  public TaskClassLoader(URL[] urls) {
    super(urls);
  }

  public void addURL(URL url) {
    if (url != null) {
      super.addURL(url);
    }
  }
}
