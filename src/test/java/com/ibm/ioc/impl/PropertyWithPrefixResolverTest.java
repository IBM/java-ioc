/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.PropertiesProvider;
import com.ibm.ioc.PropertiesProvider.PropertiesModificationListener;

public class PropertyWithPrefixResolverTest {
  private final Mockery context = new Mockery();
  private OrderedPropertiesResolver resolver;
  private PropertiesProvider develProv;
  private PropertiesProvider deployProv;
  private PropertiesProvider systemProv;
  private PropertiesProvider runProv;

  @Before
  public void setup() {
    this.resolver = new OrderedPropertiesResolver();

    this.develProv = this.context.mock(PropertiesProvider.class, "");
    this.deployProv = this.context.mock(PropertiesProvider.class, "deploy");
    this.systemProv = this.context.mock(PropertiesProvider.class, "system");
    this.runProv = this.context.mock(PropertiesProvider.class, "run");

    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.develProv).addModificationListener(with(
            aNonNull(PropertiesModificationListener.class)));
        allowing(PropertyWithPrefixResolverTest.this.deployProv).addModificationListener(with(
            aNonNull(PropertiesModificationListener.class)));
        allowing(PropertyWithPrefixResolverTest.this.systemProv).addModificationListener(with(
            aNonNull(PropertiesModificationListener.class)));
        allowing(PropertyWithPrefixResolverTest.this.runProv).addModificationListener(with(
            aNonNull(PropertiesModificationListener.class)));
      }
    });

    this.resolver.announcePropertiesProvider("", 0);
    this.resolver.announcePropertiesProvider("deploy", 1);
    this.resolver.announcePropertiesProvider("system", 2);
    this.resolver.announcePropertiesProvider("run", 3);

    this.resolver.registerPropertiesProvider("", this.develProv);
    this.resolver.registerPropertiesProvider("deploy", this.deployProv);
    this.resolver.registerPropertiesProvider("system", this.systemProv);
    this.resolver.registerPropertiesProvider("run", this.runProv);

  }

  @Test
  public void testAnnounceProperties() {
    final PropertiesProvider provider = this.context.mock(PropertiesProvider.class);

    final OrderedPropertiesResolver resolver = new OrderedPropertiesResolver();
    Assert.assertEquals(0, resolver.getAnnouncedProperties().length);
    resolver.announcePropertiesProvider("a", 1);
    Assert.assertEquals(1, resolver.getAnnouncedTypes().length);
    Assert.assertEquals("a", resolver.getAnnouncedTypes()[0]);
    Assert.assertEquals(2, resolver.getAnnouncedProperties().length);
    Assert.assertEquals("a", resolver.getAnnouncedProperties()[1]);
    Assert.assertEquals(1, resolver.findTypeOrder("a"));
    Assert.assertEquals(-1, resolver.findTypeOrder("b"));
    Assert.assertNull(resolver.getPropertiesProvider("a"));
    try {
      resolver.getPropertiesProvider("b");
      Assert.fail("'b' is not registered, should throw");
    } catch (final Exception ignore) {
    }

    this.context.checking(new Expectations() {
      {
        allowing(provider).addModificationListener(with(
            aNonNull(PropertiesModificationListener.class)));
      }
    });

    resolver.registerPropertiesProvider("a", provider);
    Assert.assertEquals(provider, resolver.getPropertiesProvider("a"));
    try {
      resolver.registerPropertiesProvider("b", provider);
      Assert.fail("'b' can't be registered - not announced, should throw");
    } catch (final IllegalArgumentException ignore) {
    }

    resolver.announcePropertiesProvider("a", 2);
    Assert.assertEquals(2, resolver.findTypeOrder("a"));
    Assert.assertEquals(3, resolver.getAnnouncedProperties().length);
    Assert.assertEquals("a", resolver.getAnnouncedProperties()[2]);
    Assert.assertNull(resolver.getAnnouncedProperties()[1]);
    Assert.assertNull(resolver.getAnnouncedProperties()[0]);

    resolver.announcePropertiesProvider("b", 1);
    Assert.assertEquals(2, resolver.getAnnouncedTypes().length);
    Assert.assertEquals(2, resolver.findTypeOrder("a"));
    Assert.assertEquals(3, resolver.getAnnouncedProperties().length);
    Assert.assertEquals("a", resolver.getAnnouncedProperties()[2]);
    Assert.assertEquals("b", resolver.getAnnouncedProperties()[1]);
    Assert.assertNull(resolver.getAnnouncedProperties()[0]);

    // Change order
    resolver.announcePropertiesProvider("a", 0);
    Assert.assertEquals(2, resolver.getAnnouncedTypes().length);
    Assert.assertEquals(0, resolver.findTypeOrder("a"));
    Assert.assertEquals(2, resolver.getAnnouncedProperties().length);
    Assert.assertEquals("a", resolver.getAnnouncedProperties()[0]);
    Assert.assertEquals("b", resolver.getAnnouncedProperties()[1]);

  }

  @Test
  public void testGetPropertyNoValue() {
    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.runProv).isSet(with(any(String.class)));
        will(returnValue(false));

        allowing(PropertyWithPrefixResolverTest.this.systemProv).isSet(with(any(String.class)));
        will(returnValue(false));

        allowing(PropertyWithPrefixResolverTest.this.deployProv).isSet(with(any(String.class)));
        will(returnValue(false));

        allowing(PropertyWithPrefixResolverTest.this.develProv).isSet(with(any(String.class)));
        will(returnValue(false));
      }

    });

    try {
      this.resolver.getProperty("maxDelay");
      Assert.fail("Exception should be thrown");
    } catch (final ConfigurationItemNotDefinedException e) {
    }
  }

  @Test
  public void testGetProperty1() throws ConfigurationItemNotDefinedException {

    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.runProv).isSet("maxDelay");
        will(returnValue(true));
        allowing(PropertyWithPrefixResolverTest.this.runProv).getProperty("maxDelay");
        will(returnValue("10"));
      }
    });
    Assert.assertEquals("10", this.resolver.getProperty("maxDelay"));
  }

  @Test
  public void testGetProperty2() throws ConfigurationItemNotDefinedException {
    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.runProv).isSet("maxDelay");
        will(returnValue(true));
        oneOf(PropertyWithPrefixResolverTest.this.runProv).getProperty("maxDelay");
        will(returnValue("99"));
      }
    });

    Assert.assertEquals("99", this.resolver.getProperty("maxDelay"));
  }

  @Test
  public void testResolveProperty1() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.runProv).isSet("maxDelay");
        will(returnValue(true));
        allowing(PropertyWithPrefixResolverTest.this.runProv).getProperty("maxDelay");
        will(returnValue("10.55"));
      }
    });
    Assert.assertEquals(10.55, this.resolver.resolveDouble("maxDelay"), 0.00001);
  }

  @Test
  public void testResolveProperty2() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    this.context.checking(new Expectations() {
      {
        allowing(PropertyWithPrefixResolverTest.this.runProv).isSet("maxDelay");
        will(returnValue(true));
        allowing(PropertyWithPrefixResolverTest.this.runProv).getProperty("maxDelay");
        will(returnValue("2 minutes"));
      }
    });
    Assert.assertEquals(new Integer(2 * 60 * 1000), this.resolver.resolve("maxDelay", int.class));
  }

}
