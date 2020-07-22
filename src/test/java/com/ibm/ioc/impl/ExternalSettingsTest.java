/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.PropertiesResolver;
import org.junit.Assert;
import org.junit.Test;

public class ExternalSettingsTest {
  private final PropertiesResolver resolver = new OrderedPropertiesResolver();

  public ExternalSettingsTest() {
    final SystemPropertiesProvider p = new SystemPropertiesProvider();

    p.addProperty("string-value", new Object[] {"Hello world!"});
    p.addProperty("int-value", new Object[] {"99"});
    p.addProperty("boolean-value", new Object[] {"true"});
    p.addProperty("double-value", new Object[] {"55.5"});

    p.addProperty("string-array", new Object[] {"Hello world!", "Good bye world!"});
    p.addProperty("int-array", new Object[] {"99", "100", "101", "102"});
    p.addProperty("boolean-array", new Object[] {"true", "false"});
    p.addProperty("double-array", new Object[] {"55.55", "66.66"});

    p.addProperty("int-value1", new Object[] {"3 KiB"});
    p.addProperty("int-value2", new Object[] {"5 sec"});
    p.addProperty("int-value3", new Object[] {"2 day"});

    this.resolver.announcePropertiesProvider("test", 1);
    this.resolver.registerPropertiesProvider("test", p);
  }

  @Test
  public void setPropertyTestString() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final String sv = this.resolver.resolveString("string-value");
    Assert.assertEquals("Hello world!", sv);
  }

  @Test
  public void setPropertyTestInt() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final int iv = this.resolver.resolveInt("int-value");
    Assert.assertEquals(99, iv);
  }

  @Test
  public void setPropertyTestInt1() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final int iv = this.resolver.resolveInt("int-value1");
    Assert.assertEquals(3 * 1024, iv);
  }

  @Test
  public void setPropertyTestInt2() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final int iv = this.resolver.resolveInt("int-value2");
    Assert.assertEquals(5 * 1000, iv);
  }

  @Test
  public void setPropertyTestInt3() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final int iv = this.resolver.resolveInt("int-value3");
    Assert.assertEquals(2 * 24 * 60 * 60 * 1000, iv);
  }

  @Test
  public void setPropertyTestBoolean() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    final boolean bv = this.resolver.resolveBoolean("boolean-value");
    Assert.assertTrue(bv);
  }

  @Test
  public void setPropertyTestDouble() throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {

    final double dv = this.resolver.resolveDouble("double-value");
    Assert.assertEquals(55.5, dv, 0.001);
  }

}
