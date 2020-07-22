/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.JavaBindings;
import com.ibm.ioc.JavaBindingsProvider;
import com.ibm.ioc.JavaBindingsProvider.BindingsFactory;
import com.ibm.ioc.PropertiesResolverFactory;
import com.ibm.ioc.Size;
import com.ibm.ioc.impl.MutableObjectsTest.TestObj.Builder;

public class MutableObjectsTest {
  public static class TestObj {
    public static class Builder {
      public String testString;

      public TestObj build() {
        return new TestObj(this.testString);
      }
    }

    final String testString;
    String testMutableString;
    int testMutableInteger;

    public TestObj(final String testString) {
      this.testString = testString;
    }

    public void setTestMutableString(final String newVal) {
      this.testMutableString = newVal;
    }

    public void setTestMutableInteger(final int newVal) {
      this.testMutableInteger = newVal;
    }
  }

  public static class Bindings implements JavaBindings {
    @Override
    public void register(final BindingsFactory def) {
      def.builder(Builder.class)
          .prop("test-string", "test-string")
          .prop("test-mutable-string", "test-mutable-string")
          .prop("test-mutable-integer", "test-mutable-integer");
    }
  }

  private BindingsProvider bindingsProvider;
  private MemoryPropertiesProvider propertiesProvider;

  @Before
  public void setUp() {
    final List<Class<?>> bindingsClassList = new ArrayList<>();
    bindingsClassList.add(Bindings.class);
    this.bindingsProvider = new JavaBindingsProvider(bindingsClassList);

    this.propertiesProvider = new MemoryPropertiesProvider();

    PropertiesResolverFactory.getInstance().announcePropertiesProvider("memory", 5);
    PropertiesResolverFactory.getInstance().registerPropertiesProvider("memory",
        this.propertiesProvider);
  }

  @After
  public void tearDown() {
    this.bindingsProvider = null;
    this.propertiesProvider.clear();
    this.propertiesProvider = null;
    PropertiesResolverFactory.getInstance().registerPropertiesProvider("memory",
        null);
  }

  @Test
  public void testMutableString() throws Exception {
    this.propertiesProvider.addProperty("test-string", "this is a test string");
    this.propertiesProvider.addProperty("test-mutable-string", "mutable test string");
    this.propertiesProvider.addProperty("test-mutable-integer", "5");

    final TestObj testObj =
        this.bindingsProvider.getDefaultImplementation(TestObj.class)
            .initialize();

    Assert.assertEquals("this is a test string", testObj.testString);
    Assert.assertEquals("mutable test string", testObj.testMutableString);

    this.propertiesProvider.addProperty("test-string", "test string 2");
    this.propertiesProvider.addProperty("test-mutable-string", "test string 2");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals("this is a test string", testObj.testString);

    // modifiable changes
    Assert.assertEquals("test string 2", testObj.testMutableString);

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals("this is a test string", testObj.testString);

    // modifiable is cleared
    Assert.assertNull(testObj.testMutableString);
  }

  @Test
  public void testMutableInteger() throws Exception {
    this.propertiesProvider.addProperty("test-mutable-integer", "5");
    final TestObj testObj =
        this.bindingsProvider.getDefaultImplementation(TestObj.class)
            .initialize();
    Assert.assertEquals(5, testObj.testMutableInteger);

    this.propertiesProvider.addProperty("test-mutable-integer", "100 MiB");
    Assert.assertEquals(100 * Size.MiB, testObj.testMutableInteger);

    this.propertiesProvider.clear();
    // No change when property is removed because null cannot be set on an integer
    Assert.assertEquals(100 * Size.MiB, testObj.testMutableInteger);
  }

}
