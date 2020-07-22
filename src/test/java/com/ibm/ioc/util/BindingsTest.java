/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.util;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.JavaBindingsProvider;
import com.ibm.ioc.RemotePropertiesLookup;

public class BindingsTest {
  @Before
  public void setUp() {
    System.setProperty(RemotePropertiesLookup.class.getName(), "null");
  }

  @After
  public void tearDown() {
    System.getProperties().remove(RemotePropertiesLookup.class.getName());
  }

  @Test
  public void getBindingsProviderTest() throws Exception {

    final BindingsProvider provider = new JavaBindingsProvider();

    final Map<Class<?>, List<String>> failures = provider.selfTest();
    Assert.assertTrue("Found invalid bindings" + failures.toString(), failures.isEmpty());

  }
}
