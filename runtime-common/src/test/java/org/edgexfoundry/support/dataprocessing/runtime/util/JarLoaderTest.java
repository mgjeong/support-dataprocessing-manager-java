package org.edgexfoundry.support.dataprocessing.runtime.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JarLoader.class, Class.class, URLClassLoader.class, File.class, URL.class})
public class JarLoaderTest {

  @Test
  public void jarLoaderTest() throws Exception {

    mockStatic(URL.class, Mockito.RETURNS_DEEP_STUBS);
    mockStatic(File.class);
    mockStatic(URLClassLoader.class, Mockito.RETURNS_DEEP_STUBS);

    URL url = mock(URL.class);
    URI uri = mock(URI.class);
    File file = mock(File.class);
    Method method = mock(Method.class);
    URLClassLoader classLoader = mock(URLClassLoader.class, Mockito.RETURNS_DEEP_STUBS);

    whenNew(URL.class).withAnyArguments().thenReturn(url);
    whenNew(File.class).withAnyArguments().thenReturn(file);
    whenNew(URLClassLoader.class).withAnyArguments().thenReturn(classLoader);

    when(file.exists()).thenReturn(true);
    when(file.length()).thenReturn((long) 1);
    when(file.toURI()).thenReturn(uri);
    when(uri.toURL()).thenReturn(url);
    when(URLClassLoader.class.getDeclaredMethod(anyString(), any())).thenReturn(method);

    try {
      JarLoader jarloader = new JarLoader("Test.jar", classLoader);
      Assert.assertNotNull(jarloader);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void unloadJarTest() throws Exception {

    mockStatic(URL.class, Mockito.RETURNS_DEEP_STUBS);
    mockStatic(File.class);
    mockStatic(URLClassLoader.class, Mockito.RETURNS_DEEP_STUBS);

    URL url = mock(URL.class);
    URI uri = mock(URI.class);
    File file = mock(File.class);
    Method method = mock(Method.class);
    URLClassLoader classLoader = mock(URLClassLoader.class, Mockito.RETURNS_DEEP_STUBS);

    whenNew(URL.class).withAnyArguments().thenReturn(url);
    whenNew(File.class).withAnyArguments().thenReturn(file);
    whenNew(URLClassLoader.class).withAnyArguments().thenReturn(classLoader);

    when(file.exists()).thenReturn(true);
    when(file.length()).thenReturn((long) 1);
    when(file.toURI()).thenReturn(uri);
    when(uri.toURL()).thenReturn(url);
    when(URLClassLoader.class.getDeclaredMethod(anyString(), any())).thenReturn(method);

    try {
      JarLoader jarloader = new JarLoader("Test.jar", classLoader);
      Assert.assertNotNull(jarloader);
      jarloader.unloadJar();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}