package org.edgexfoundry.support.dataprocessing.runtime;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class SettingsTest {
    @Test
    public void testConstructorIsPrivate() {
        Constructor<Settings> constructor;
        try {
            constructor = Settings.class.getDeclaredConstructor();
            Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
