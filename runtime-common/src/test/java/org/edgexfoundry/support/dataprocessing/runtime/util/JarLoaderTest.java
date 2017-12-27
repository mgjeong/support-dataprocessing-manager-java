package org.edgexfoundry.support.dataprocessing.runtime.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JarLoader.class, Class.class, URLClassLoader.class, File.class, URL.class})
public class JarLoaderTest {

  @Test
  public void jarLoaderTest() throws Exception {

    mockStatic(URL.class);
    mockStatic(File.class);
    mockStatic(URLClassLoader.class);

    URL url = mock(URL.class);
    URI uri = mock(URI.class);
    File file = mock(File.class);
    Method method = mock(Method.class);
    URLClassLoader classLoader = mock(URLClassLoader.class);

    whenNew(URL.class).withAnyArguments().thenReturn(url);
    whenNew(File.class).withAnyArguments().thenReturn(file);
    whenNew(URLClassLoader.class).withAnyArguments().thenReturn(classLoader);

    when(file.exists()).thenReturn(true);
    when(file.length()).thenReturn((long) 1);
    when(file.toURI()).thenReturn(uri);
    when(uri.toURL()).thenReturn(url);
    when(URLClassLoader.class.getDeclaredMethod(anyString(), URL.class)).thenReturn(method);
//    when(method.setAccessible(true));
//    method.setAccessible(true);
//    method.invoke(this.classLoader, new Object[]{file.toURI().toURL()});

    try {
      JarLoader jarloader = new JarLoader("Test.jar", classLoader);
      Assert.assertNotNull(jarloader);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}