/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.ibm.ioc.PropertiesProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class INIPropertiesProviderTest {
  protected PropertiesProvider provider;

  @Before
  public void setup() throws Exception {
    this.provider = getProvider();
  }

  protected PropertiesProvider getProvider() throws IOException, URISyntaxException {
    return new INIPropertiesProvider("org.cleversafe.",
        INIPropertiesProviderTest.class.getClassLoader().getResource(
            "com/ibm/ioc/properties.conf").toURI(),
        false);
  }

  @Test
  public void testWUIniFileNotFound() throws Exception {
    final INIPropertiesProvider ini =
        new INIPropertiesProvider("", new File(System.getProperty("java.io.tmpdir"),
            "this-is-a-nonexistent-file.ini"), false);
    Assert.assertEquals(0, ini.getQualifiedNames().size());
  }

  @Test
  public void testWUIniDirectoryNotFound() throws Exception {
    final INIPropertiesProvider ini =
        new INIPropertiesProvider("", new File(new File(System.getProperty("java.io.tmpdir"),
            "nonexistent-dir"),
            "this-is-a-nonexistent-file.ini"), false);
    Assert.assertEquals(0, ini.getQualifiedNames().size());
  }

  @Test
  public void testWUIniURLNotFound() throws Exception {
    final URI previous = INIPropertiesProviderTest.class.getClassLoader().getResource(
        "com/ibm/ioc/properties.conf").toURI();
    final URI nonexistent = previous.resolve("nonexistent");
    final INIPropertiesProvider ini =
        new INIPropertiesProvider("", nonexistent, false);
    Assert.assertEquals(0, ini.getQualifiedNames().size());
  }

  @Test
  public void testGetUnderscoreProperty() throws Exception {
    Assert.assertEquals("1000",
        this.provider.getProperty("org.cleversafe.stuff.i-like-this"));
    Assert.assertEquals("1000",
        this.provider.getProperty("org.cleversafe.stuff.i_like_this"));
    Assert.assertEquals("4444",
        this.provider.getProperty("org.cleversafe.stuff.i-hate-this"));
    Assert.assertEquals("4444",
        this.provider.getProperty("org.cleversafe.stuff.i_hate_this"));
  }

  @Test
  public void testGetProperty() throws Exception {
    Assert.assertEquals("this is great",
        this.provider.getProperty("org.cleversafe.string.funtest"));
    Assert.assertEquals("this is just a single string",
        this.provider.getProperty("org.cleversafe.string.nofuntest"));
    Assert.assertEquals("1325",
        this.provider.getProperty("org.cleversafe.int.funtest"));
    Assert.assertEquals("1883",
        this.provider.getProperty("org.cleversafe.int.nofuntest"));
    Assert.assertEquals("true",
        this.provider.getProperty("org.cleversafe.boolean.funtest"));
    Assert.assertEquals("TRUE",
        this.provider.getProperty("org.cleversafe.boolean.nofuntest"));
    Assert.assertEquals("false",
        this.provider.getProperty("org.cleversafe.boolean.offtest"));
    Assert.assertEquals("13.25",
        this.provider.getProperty("org.cleversafe.double.funtest"));
    Assert.assertEquals("1.883",
        this.provider.getProperty("org.cleversafe.double.nofuntest"));
  }

  @Test
  public void testIsSet() {
    Assert.assertFalse(this.provider.isSet("org.example.bad.novalue"));
    Assert.assertFalse(this.provider.isSet("org.cleversafe.bad.novalue"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.string.funtest"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.string.nofuntest"));
    Assert.assertFalse(this.provider.isSet("org.cleversafe.string.novalue"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.int.funtest"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.int.nofuntest"));
    Assert.assertFalse(this.provider.isSet("org.cleversafe.int.novalue"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.boolean.funtest"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.boolean.nofuntest"));
    Assert.assertFalse(this.provider.isSet("org.cleversafe.boolean.novalue"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.double.funtest"));
    Assert.assertTrue(this.provider.isSet("org.cleversafe.double.nofuntest"));
    Assert.assertFalse(this.provider.isSet("org.cleversafe.double.novalue"));

  }
}
