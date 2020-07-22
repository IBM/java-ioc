/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.UUID;

import javax.crypto.Cipher;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PropertyResolverFactoryTest {
  private final Mockery context = new Mockery();

  private PropertiesProvider runProv;

  @Before
  public void setup() {
    this.runProv = this.context.mock(PropertiesProvider.class, "run");
  }

  @Test
  public void fullTest() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {

    System.setProperty("host", "localhost");
    System.setProperty("bootstrap_uri", "test://localhost");
    System.setProperty("layer.communication.network.tcp-buffer-size", "99");

    final PropertiesResolver propFact = new PropertiesResolverFactory().getResolver();

    propFact.registerPropertiesProvider("run", this.runProv);
    this.context.checking(new Expectations() {
      {
        allowing(PropertyResolverFactoryTest.this.runProv).isSet("java.class.path");
        will(returnValue(false));
        // No prefixes as it starts with java
        allowing(PropertyResolverFactoryTest.this.runProv).isSet("host");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("org.cleversafe.host");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("com.cleversafe.host");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("bindings.host");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "org.cleversafe.layer.cache-enabled");
        will(returnValue(false));
        allowing(PropertyResolverFactoryTest.this.runProv).isSet("layer.cache-enabled");
        will(returnValue(false));
        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "com.cleversafe.layer.cache-enabled");
        will(returnValue(false));
        allowing(PropertyResolverFactoryTest.this.runProv).isSet("bindings.layer.cache-enabled");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("layer.max-dirty-blocks");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "org.cleversafe.layer.max-dirty-blocks");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("some-delay");
        will(returnValue(true));
        allowing(PropertyResolverFactoryTest.this.runProv).getProperty("some-delay");
        will(returnValue("-1"));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "org.cleversafe.layer.communication.network.acceptor-threads");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "layer.communication.network.tcp-buffer-size");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "org.cleversafe.layer.communication.network.tcp-buffer-size");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "com.cleversafe.layer.communication.network.tcp-buffer-size");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet(
            "bindings.layer.communication.network.tcp-buffer-size");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("fun.test");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("org.cleversafe.fun.test");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("com.cleversafe.fun.test");
        will(returnValue(false));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("no-fun.test");
        will(returnValue(true));
        allowing(PropertyResolverFactoryTest.this.runProv).getProperty("no-fun.test");
        will(returnValue("Run-time-value"));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("uuid-property");
        will(returnValue(true));
        allowing(PropertyResolverFactoryTest.this.runProv).getProperty("uuid-property");
        will(returnValue(new UUID(100, 100).toString()));

        allowing(PropertyResolverFactoryTest.this.runProv).isSet("cipher-property");
        will(returnValue(true));
        allowing(PropertyResolverFactoryTest.this.runProv).getProperty("cipher-property");
        will(returnValue("DES/CBC/PKCS5Padding"));

      }

    });

    Assert.assertArrayEquals(new String[] {"", "deploy", "system", "run"},
        propFact.getAnnouncedProperties());

    // Check deployment properties
    // Assert.assertEquals("localhost", propFact.getProperty("host"));

    Assert.assertTrue(propFact.resolveBoolean("layer.cache-enabled"));

    Assert.assertEquals(512, propFact.resolveInt("layer.max-dirty-blocks"));

    Assert.assertEquals(-1, propFact.resolveInt("some-delay"));

    // TODO: FIXME
    // Assert.assertEquals(250,
    // propFact.resolveInt("org.cleversafe.layer.communication.network.acceptor-threads"));

    // Set to 99 in system settings, overwrites 512 in devel settings
    Assert.assertEquals(99, propFact.resolveInt("layer.communication.network.tcp-buffer-size"));

    // 2-nd test settings.xml file
    // Assert.assertEquals(999.9, propFact.resolveDouble("fun.test"), 0.00001);

    Assert.assertNotNull(propFact.resolveString("no-fun.test"));
    Assert.assertEquals("Run-time-value", propFact.resolveString("no-fun.test"));

    Assert.assertEquals(new UUID(100, 100), propFact.resolve("uuid-property", UUID.class));

    Assert.assertNotNull(propFact.resolve("cipher-property", Cipher.class));
  }
}
