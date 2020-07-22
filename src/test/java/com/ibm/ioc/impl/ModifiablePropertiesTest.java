/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

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
import com.ibm.ioc.parsers.LongParser;

public class ModifiablePropertiesTest {

  public static class TestImpl {
    public String testString;
    public Modifiable<String> testModifiableString;
    public int testInteger;
    public Modifiable<Integer> testModifiableInteger;
    public long testLong;
    public Modifiable<Long> testModifiableLong;
    public double testDouble;
    public Modifiable<Double> testModifiableDouble;
    public boolean testBoolean;
    public Modifiable<Boolean> testModifiableBoolean;
    public UUID testUUID;
    public Modifiable<UUID> testModifiableUUID;
    public TestJSONObject testJSONObject;
    public Modifiable<TestJSONObject> testModifiableJSONObject;
    public LocalTime testLocalTime;
    public Modifiable<LocalTime> testModifiableLocalTime;
    public LocalDateTime testLocalDateTime;
    public Modifiable<LocalDateTime> testModifiableLocalDateTime;

    public List<Integer> testIntegerList;
    public Modifiable<List<Integer>> testModifiableIntegerList;

    public Map<String, Integer> testMap;
    public Modifiable<Map<String, Integer>> testModifiableMap;

    public List<Long> testComplexLongList;
    public Modifiable<List<Long>> testModifiableComplexLongList;

    public List<LocalTime> testLocalTimeList;
    public Modifiable<List<LocalTime>> testModifiableLocalTimeList;

    public Map<LocalTime, UUID> testLocalTimeMap;
    public Modifiable<Map<LocalTime, UUID>> testModifiableLocalTimeMap;

    public Map<String, LocalDateTime> testLocalDateTimeMap;
    public Modifiable<Map<String, LocalDateTime>> testModifiableLocalDateTimeMap;
  }

  public static class Bindings implements JavaBindings {
    @Override
    public void register(final BindingsFactory def) {
      def.def(TestImpl.class)
          .prop("test-string", "test-string")
          .prop("test-modifiable-string", "test-modifiable-string")
          .prop("test-integer", "test-integer")
          .prop("test-modifiable-integer", "testModifiableInteger")
          .prop("test-long", "test-long")
          .prop("test-modifiable-long", "test-modifiable-long")
          .prop("test-boolean", "test-boolean")
          .prop("test-modifiable-boolean", "test-modifiable-boolean")
          .prop("test-double", "test-double")
          .prop("test-modifiable-double", "test-modifiable-double")
          .prop("test-UUID", "test-UUID")
          .prop("test-modifiable-UUID", "test-modifiable-UUID")
          .prop("test-JSON-object", "test-JSON-object")
          .prop("test-modifiable-JSON-object", "test-modifiable-JSON-object")
          .prop("test-local-time", "test-local-time")
          .prop("test-modifiable-local-time", "test-modifiable-local-time")
          .prop("test-local-date-time", "test-local-date-time")
          .prop("test-modifiable-local-date-time", "test-modifiable-local-date-time")
          .prop("test-integer-list", "testIntegerList")
          .prop("test-modifiable-integer-list", "test-modifiable-integer-list")
          .prop("test-map", "test-map")
          .prop("test-modifiable-map", "test-modifiable-map")
          .prop("test-complex-long-list", "test-complex-long-list")
          .prop("test-modifiable-complex-long-list", "test-modifiable-complex-long-list")
          .prop("test-local-time-list", "test-local-time-list")
          .prop("test-modifiable-local-time-list", "test-modifiable-local-time-list")
          .prop("test-local-time-map", "test-local-time-map")
          .prop("test-modifiable-local-time-map", "test_modifiable_local_time_map")
          .prop("test-local-date-time-map", "test-local-date-time-map")
          .prop("test-modifiable-local-date-time-map", "test-modifiable-local-date-time-map");

      def.builder(BuiltTestImplBuilder.class)
          .prop("test-string-1", "test-string-1")
          .prop("test-string-2", "test-string-2")
          .prop("test-string-3", "test-string-3")
          .prop("test-local-time-1", "test-local-time-1")
          .prop("test-local-date-time-1", "test-local-date-time-1");

      def.def(ClassWithObservables.class)
          .prop("my-int", "test-int-value")
          .prop("my-long", "test-long-value")
          .prop("long-list", "test-long-value-list")
          .prop("my-local-time-c-w-o", "my-local-time-c-w-o");

      def.builder(ImmutableClassWithObservables.Builder.class, ImmutableClassWithObservables.class)
          .prop("my-int", "test-int-value")
          .prop("my-long", "test-long-value")
          .prop("long-list", "test-long-value-list")
          .prop("my-local-time-i-c-w-o", "my-local-time-i-c-w-o");

      def.def(AlreadyIntSupplier.class, AlreadyIntSupplier.class);
      def.def(ClassWithSuppliers.class)
          .prop("my-int", "test-int-value")
          .prop("my-long", "test-long-value")
          .prop("long-list", "test-long-value-list")
          .prop("my-local-time-c-w-s", "my-local-time-c-w-s")
          .prop("primitive-int", "test-primitive-int-value")
          .prop("primitive-long", "test-primitive-long-value")
          .prop("primitive-double", "test-primitive-double-value")
          .prop("primitive-boolean", "test-primitive-boolean-value")
          .ref("already-a-supplier", AlreadyIntSupplier.class);

      def.builder(ImmutableClassWithSuppliers.Builder.class, ImmutableClassWithSuppliers.class)
          .prop("my-int", "test-int-value")
          .prop("my-long", "test-long-value")
          .prop("long-list", "test-long-value-list")
          .prop("my-local-time-i-c-w-s", "my-local-time-i-c-w-s")
          .prop("primitive-int", "test-primitive-int-value")
          .prop("primitive-long", "test-primitive-long-value")
          .prop("primitive-double", "test-primitive-double-value")
          .prop("primitive-boolean", "test-primitive-boolean-value")
          .ref("already-a-supplier", AlreadyIntSupplier.class);
    }
  }

  public static class BuiltTestImpl {
    private String testString1;
    private String testString2;
    private final String testString3;
    private final LocalTime testLocalTime1;
    private LocalDateTime testLocalDateTime1;

    public BuiltTestImpl(final String testString1, final String testString3, final LocalTime testLocalTime1) {
      this.testString1 = testString1;
      this.testString3 = testString3;
      this.testLocalTime1 = testLocalTime1;
    }

    public void setTestString1(final String testString1) {
      this.testString1 = testString1;
    }

    public void setTestString2(final String testString2) {
      this.testString2 = testString2;
    }

    public String getTestString1() {
      return this.testString1;
    }

    public String getTestString2() {
      return this.testString2;
    }

    public String getTestString3() {
      return this.testString3;
    }

    public void setTestLocalDateTime1(final LocalDateTime testLocalDateTime1) {
      this.testLocalDateTime1 = testLocalDateTime1;
    }

    public LocalTime getTestLocalTime1() {
      return this.testLocalTime1;
    }

    public LocalDateTime getTestLocalDateTime1() {
      return this.testLocalDateTime1;
    }
  }

  public static class BuiltTestImplBuilder {
    public String testString1;
    public String testString3;
    public LocalTime testLocalTime1;

    public BuiltTestImpl build() {
      return new BuiltTestImpl(this.testString1, this.testString3, this.testLocalTime1);
    }
  }

  public static class TestJSONObject {
    private String firstValue;
    private String secondValue;

    private Long myLongValue;

    private LocalTime myLocalTime;
    private LocalDateTime myLocalDateTime;

    public String getFirstValue() {
      return this.firstValue;
    }

    public void setFirstValue(final String firstValue) {
      this.firstValue = firstValue;
    }

    public String getSecondValue() {
      return this.secondValue;
    }

    public void setSecondValue(final String secondValue) {
      this.secondValue = secondValue;
    }

    public Long getMyLongValue() {
      return this.myLongValue;
    }

    public void setMyLongValue(final Long myLongValue) {
      this.myLongValue = myLongValue;
    }

    public void setMyLocalTime(final LocalTime myLocalTime) {
      this.myLocalTime = myLocalTime;
    }

    public void setMyLocalDateTime(final LocalDateTime myLocalDateTime) {
      this.myLocalDateTime = myLocalDateTime;
    }

    public LocalTime getMyLocalTime() {
      return this.myLocalTime;
    }

    public LocalDateTime getMyLocalDateTime() {
      return this.myLocalDateTime;
    }

    @Override
    public String toString() {
      return "TestJSONObject [firstValue=" + this.firstValue + ", secondValue="
          + this.secondValue + ", myLongValue=" + this.myLongValue + ", myLocalTime=" + this.myLocalTime
          + ", myLocalDateTime=" + this.myLocalDateTime + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.firstValue == null) ? 0 : this.firstValue.hashCode());
      result = prime * result + ((this.myLongValue == null) ? 0 : this.myLongValue.hashCode());
      result = prime * result + ((this.secondValue == null) ? 0 : this.secondValue.hashCode());
      result = prime * result + ((this.myLocalTime == null) ? 0 : this.myLocalTime.hashCode());
      result = prime * result + ((this.myLocalDateTime == null) ? 0 : this.myLocalDateTime.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final TestJSONObject other = (TestJSONObject) obj;
      if (this.firstValue == null) {
        if (other.firstValue != null) {
          return false;
        }
      } else if (!this.firstValue.equals(other.firstValue)) {
        return false;
      }
      if (this.myLongValue == null) {
        if (other.myLongValue != null) {
          return false;
        }
      } else if (!this.myLongValue.equals(other.myLongValue)) {
        return false;
      }
      if (this.secondValue == null) {
        if (other.secondValue != null) {
          return false;
        }
      } else if (!this.secondValue.equals(other.secondValue)) {
        return false;
      }
      if (this.myLocalTime == null) {
        if (other.myLocalTime != null) {
          return false;
        }
      } else if (!this.myLocalTime.equals(other.myLocalTime)) {
        return false;
      }
      if (this.myLocalDateTime == null) {
        if (other.myLocalDateTime != null) {
          return false;
        }
      } else if (!this.myLocalDateTime.equals(other.myLocalDateTime)) {
        return false;
      }
      return true;
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
  public void testNullModifiable() throws Exception {
    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testString);
    Assert.assertNull(testObj.testModifiableString.get());
    Assert.assertNull(testObj.testModifiableInteger.get());
    Assert.assertNull(testObj.testModifiableBoolean.get());
    Assert.assertNull(testObj.testModifiableJSONObject.get());
    Assert.assertNull(testObj.testModifiableLong.get());
    Assert.assertNull(testObj.testModifiableUUID.get());
    Assert.assertNull(testObj.testModifiableDouble.get());
    Assert.assertNull(testObj.testModifiableLocalTime.get());
    Assert.assertNull(testObj.testModifiableLocalDateTime.get());
    Assert.assertNull(testObj.testModifiableLocalTimeList.get());
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());
    Assert.assertNull(testObj.testModifiableLocalDateTimeMap.get());

    this.propertiesProvider.addProperty("test-modifiable-long", "5000");

    Assert.assertEquals(5000L, testObj.testModifiableLong.get().longValue());
  }

  @Test
  public void testModifiableString() throws Exception {
    this.propertiesProvider.addProperty("test-string", "this is a test string");
    this.propertiesProvider.addProperty("test-modifiable-string", "modifiable test string");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals("this is a test string", testObj.testString);
    Assert.assertEquals("modifiable test string", testObj.testModifiableString.get());

    this.propertiesProvider.addProperty("test-string", "test string 2");
    this.propertiesProvider.addProperty("test-modifiable-string", "test string 2");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals("this is a test string", testObj.testString);

    // modifiable changes
    Assert.assertEquals("test string 2", testObj.testModifiableString.get());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals("this is a test string", testObj.testString);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableString.get());
  }

  @Test
  public void testModifiableEmptyStringValue() throws Exception {
    this.propertiesProvider.addProperty("test-string", "string");
    this.propertiesProvider.addProperty("test-modifiable-string", "");
    this.propertiesProvider.addProperty("test-integer", "");
    this.propertiesProvider.addProperty("test-modifiable-integer", "");
    this.propertiesProvider.addProperty("test-long", "");
    this.propertiesProvider.addProperty("test-modifiable-long", "");
    this.propertiesProvider.addProperty("test-boolean", "");
    this.propertiesProvider.addProperty("test-modifiable-boolean", "");
    this.propertiesProvider.addProperty("test-double", "");
    this.propertiesProvider.addProperty("test-modifiable-double", "");
    this.propertiesProvider.addProperty("test-UUID", "");
    this.propertiesProvider.addProperty("test-modifiable-UUID", "");
    this.propertiesProvider.addProperty("test-JSON-object", "");
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", "");
    this.propertiesProvider.addProperty("test-local-time", "");
    this.propertiesProvider.addProperty("test-modifiable-local-time", "");
    this.propertiesProvider.addProperty("test-local-date-time", "");
    this.propertiesProvider.addProperty("test-modifiable-local-date-time", "");
    this.propertiesProvider.addProperty("test-integer-list", "");
    this.propertiesProvider.addProperty("test-modifiable-integer-list", "[1, 1, 2, 3, 5]");
    this.propertiesProvider.addProperty("test-map", "");
    this.propertiesProvider.addProperty("test-modifiable-map", "");
    this.propertiesProvider.addProperty("test-complex-long-list", "");
    this.propertiesProvider.addProperty("test-modifiable-complex-long-list", "");
    this.propertiesProvider.addProperty("test-local-time-list", "");
    this.propertiesProvider.addProperty("test-modifiable-local-time-list", "");
    this.propertiesProvider.addProperty("test-local-time-map", "");
    this.propertiesProvider.addProperty("test-modifiable-local-time-map", "");
    this.propertiesProvider.addProperty("test-local-date-time-map", "");
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map", "");
    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNotNull(testObj.testString);
    Assert.assertEquals("string", testObj.testString);
    Assert.assertNotNull(testObj.testModifiableString.get());
    Assert.assertTrue(testObj.testModifiableString.get().isEmpty());
    Assert.assertNull(testObj.testModifiableInteger.get());
    Assert.assertNull(testObj.testModifiableBoolean.get());
    Assert.assertNull(testObj.testModifiableLong.get());
    Assert.assertNull(testObj.testModifiableDouble.get());
    Assert.assertNull(testObj.testUUID);
    Assert.assertNull(testObj.testModifiableUUID.get());
    Assert.assertNull(testObj.testJSONObject);
    Assert.assertNull(testObj.testModifiableJSONObject.get());
    Assert.assertNull(testObj.testLocalTime);
    Assert.assertNull(testObj.testModifiableLocalTime.get());
    Assert.assertNull(testObj.testLocalDateTime);
    Assert.assertNull(testObj.testModifiableLocalDateTime.get());
    Assert.assertNull(testObj.testLocalTimeList);
    Assert.assertNull(testObj.testModifiableLocalTimeList.get());
    Assert.assertNull(testObj.testLocalTimeMap);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());
    Assert.assertNull(testObj.testLocalDateTimeMap);
    Assert.assertNull(testObj.testModifiableLocalDateTimeMap.get());
    Assert.assertNull(testObj.testIntegerList);
    Assert.assertEquals(Arrays.asList(1, 1, 2, 3, 5), testObj.testModifiableIntegerList.get());
    Assert.assertNull(testObj.testMap);
    Assert.assertNull(testObj.testModifiableMap.get());
    Assert.assertNull(testObj.testComplexLongList);
    Assert.assertNull(testObj.testModifiableComplexLongList.get());


    this.propertiesProvider.addProperty("test-modifiable-string", "modified");
    this.propertiesProvider.addProperty("test-modifiable-long", "5");
    this.propertiesProvider.addProperty("test-modifiable-integer-list", "");

    Assert.assertNotNull(testObj.testModifiableString.get());
    Assert.assertEquals("modified", testObj.testModifiableString.get());
    Assert.assertEquals(Long.valueOf(5L), testObj.testModifiableLong.get());
    Assert.assertNull(testObj.testModifiableIntegerList.get());

    this.propertiesProvider.addProperty("test-modifiable-string", "");
    this.propertiesProvider.addProperty("test-modifiable-long", "");
    this.propertiesProvider.addProperty("test-modifiable-integer-list", "[8, 13, 21]");

    Assert.assertNotNull(testObj.testModifiableString.get());
    Assert.assertTrue(testObj.testModifiableString.get().isEmpty());
    Assert.assertNull(testObj.testModifiableLong.get());
    Assert.assertEquals(Arrays.asList(8, 13, 21), testObj.testModifiableIntegerList.get());
    this.propertiesProvider.clear();
  }

  @Test
  public void testModifiableInteger() throws Exception {
    this.propertiesProvider.addProperty("test-integer", "4355");
    this.propertiesProvider.addProperty("test-modifiable-integer", "9999");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(4355, testObj.testInteger);
    Assert.assertEquals(9999, testObj.testModifiableInteger.get().intValue());

    this.propertiesProvider.addProperty("test-integer", "13448");
    this.propertiesProvider.addProperty("test-modifiable-integer", "13448");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(4355, testObj.testInteger);

    // modifiable changes
    Assert.assertEquals(13448, testObj.testModifiableInteger.get().intValue());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(4355, testObj.testInteger);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableInteger.get());
  }

  @Test
  public void testModifiableLong() throws Exception {
    this.propertiesProvider.addProperty("test-long", "3 MB");
    this.propertiesProvider.addProperty("test-modifiable-long", "5 MB");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(LongParser.parse("3 MB").longValue(), testObj.testLong);
    Assert.assertEquals(LongParser.parse("5 MB").longValue(),
        testObj.testModifiableLong.get().longValue());

    this.propertiesProvider.addProperty("test-long", "80 GB");
    this.propertiesProvider.addProperty("test-modifiable-long", "100 TB");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(LongParser.parse("3 MB").longValue(), testObj.testLong);

    // modifiable changes
    Assert.assertEquals(LongParser.parse("100 TB").longValue(),
        testObj.testModifiableLong.get().longValue());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(LongParser.parse("3 MB").longValue(), testObj.testLong);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLong.get());
  }

  @Test
  public void testModifiableDouble() throws Exception {
    this.propertiesProvider.addProperty("test-double", "4.567");
    this.propertiesProvider.addProperty("test-modifiable-double", "9.99999");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(4.567, testObj.testDouble, 0);
    Assert.assertEquals(9.99999, testObj.testModifiableDouble.get(), 0);

    this.propertiesProvider.addProperty("test-double", "89.88888");
    this.propertiesProvider.addProperty("test-modifiable-double", "43.996");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(4.567, testObj.testDouble, 0);

    // modifiable changes
    Assert.assertEquals(43.996, testObj.testModifiableDouble.get(), 0);

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(4.567, testObj.testDouble, 0);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableDouble.get());
  }

  @Test
  public void testModifiableBoolean() throws Exception {
    this.propertiesProvider.addProperty("test-boolean", "false");
    this.propertiesProvider.addProperty("test-modifiable-boolean", "false");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertFalse(testObj.testBoolean);
    Assert.assertFalse(testObj.testModifiableBoolean.get());

    this.propertiesProvider.addProperty("test-boolean", "true");
    this.propertiesProvider.addProperty("test-modifiable-boolean", "true");

    // non-modifiable doesn't change after object construction
    Assert.assertFalse(testObj.testBoolean);

    // modifiable changes
    Assert.assertTrue(testObj.testModifiableBoolean.get());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertFalse(testObj.testBoolean);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableBoolean.get());
  }

  @Test
  public void testModifiableUUID() throws Exception {
    final UUID first = UUID.randomUUID();
    final UUID second = UUID.randomUUID();
    final UUID third = UUID.randomUUID();
    final UUID fourth = UUID.randomUUID();
    this.propertiesProvider.addProperty("test-UUID", first.toString());
    this.propertiesProvider.addProperty("test-modifiable-UUID", second.toString());

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(first, testObj.testUUID);
    Assert.assertEquals(second, testObj.testModifiableUUID.get());

    this.propertiesProvider.addProperty("test-UUID", third.toString());
    this.propertiesProvider.addProperty("test-modifiable-UUID", fourth.toString());

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(first, testObj.testUUID);

    // modifiable changes
    Assert.assertEquals(fourth, testObj.testModifiableUUID.get());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(first, testObj.testUUID);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableUUID.get());
  }

  @Test
  public void testModifiableJSONObject() throws Exception {
    final String first =
        "{\"first_value\":\"first\",\"second_value\":\"second\",\"my_long_value\":3500,\"my_local_time\":\"01:02:03.654\",\"my_local_date_time\":\"2011-11-11T09:08:07.456\"}";
    final String second =
        "{\"first_value\":\"modifiable-first\",\"second_value\":\"modifiable-second\",\"my_long_value\":9000,\"my_local_time\":\"02:03:04.754\",\"my_local_date_time\":\"2012-12-10T08:09:03.111\"}";
    final String third =
        "{\"first_value\":\"changed-first\",\"second_value\":\"changed-second\",\"my_long_value\":50000,\"my_local_time\":\"06:01:06.777\",\"my_local_date_time\":\"2000-09-29T15:59:59.123456789\"}";

    this.propertiesProvider.addProperty("test-JSON-object", first);
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(3500, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertEquals("01:02:03.654", testObj.testJSONObject.getMyLocalTime().toString());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testJSONObject.getMyLocalDateTime().toString());

    Assert.assertEquals("modifiable-first",
        testObj.testModifiableJSONObject.get().getFirstValue());
    Assert.assertEquals("modifiable-second",
        testObj.testModifiableJSONObject.get().getSecondValue());
    Assert.assertEquals(9000, testObj.testModifiableJSONObject.get().getMyLongValue().longValue());
    Assert.assertEquals("02:03:04.754", testObj.testModifiableJSONObject.get().getMyLocalTime().toString());
    Assert.assertEquals("2012-12-10T08:09:03.111",
        testObj.testModifiableJSONObject.get().getMyLocalDateTime().toString());

    this.propertiesProvider.addProperty("test-JSON-object", third);
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(3500, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertEquals("01:02:03.654", testObj.testJSONObject.getMyLocalTime().toString());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testJSONObject.getMyLocalDateTime().toString());

    // modifiable changes
    Assert.assertEquals("changed-first",
        testObj.testModifiableJSONObject.get().getFirstValue());
    Assert.assertEquals("changed-second",
        testObj.testModifiableJSONObject.get().getSecondValue());
    Assert.assertEquals(50000,
        testObj.testModifiableJSONObject.get().getMyLongValue().longValue());
    Assert.assertEquals("06:01:06.777", testObj.testModifiableJSONObject.get().getMyLocalTime().toString());
    Assert.assertEquals("2000-09-29T15:59:59.123456789",
        testObj.testModifiableJSONObject.get().getMyLocalDateTime().toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(3500, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertEquals("01:02:03.654", testObj.testJSONObject.getMyLocalTime().toString());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testJSONObject.getMyLocalDateTime().toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableJSONObject.get());
  }

  @Test
  public void testModifiableJSONObjectWithConversions() throws Exception {
    final String first =
        "{\"first_value\":\"first\",\"second_value\":\"second\",\"my_long_value\":\"30 min\"}";
    final String second =
        "{\"first_value\":\"modifiable-first\",\"second_value\":\"modifiable-second\",\"my_long_value\":\"100 weeks\"}";
    final String third =
        "{\"first_value\":\"changed-first\",\"second_value\":\"changed-second\",\"my_long_value\":\"3 TiB\"}";

    this.propertiesProvider.addProperty("test-JSON-object", first);
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(1800000, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertNull(testObj.testJSONObject.getMyLocalTime());
    Assert.assertNull(testObj.testJSONObject.getMyLocalDateTime());

    Assert.assertEquals("modifiable-first",
        testObj.testModifiableJSONObject.get().getFirstValue());
    Assert.assertEquals("modifiable-second",
        testObj.testModifiableJSONObject.get().getSecondValue());
    Assert.assertEquals(LongParser.parse("100 weeks").longValue(),
        testObj.testModifiableJSONObject.get().getMyLongValue().longValue());
    Assert.assertNull(testObj.testModifiableJSONObject.get().getMyLocalTime());
    Assert.assertNull(testObj.testModifiableJSONObject.get().getMyLocalDateTime());

    this.propertiesProvider.addProperty("test-JSON-object", third);
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(1800000, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertNull(testObj.testJSONObject.getMyLocalTime());
    Assert.assertNull(testObj.testJSONObject.getMyLocalDateTime());

    // modifiable changes
    Assert.assertEquals("changed-first",
        testObj.testModifiableJSONObject.get().getFirstValue());
    Assert.assertEquals("changed-second",
        testObj.testModifiableJSONObject.get().getSecondValue());
    Assert.assertEquals(LongParser.parse("3 TiB").longValue(),
        testObj.testModifiableJSONObject.get().getMyLongValue().longValue());
    Assert.assertNull(testObj.testModifiableJSONObject.get().getMyLocalTime());
    Assert.assertNull(testObj.testModifiableJSONObject.get().getMyLocalDateTime());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals("first", testObj.testJSONObject.getFirstValue());
    Assert.assertEquals("second", testObj.testJSONObject.getSecondValue());
    Assert.assertEquals(1800000, testObj.testJSONObject.getMyLongValue().longValue());
    Assert.assertNull(testObj.testJSONObject.getMyLocalTime());
    Assert.assertNull(testObj.testJSONObject.getMyLocalDateTime());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableJSONObject.get());
  }


  @Test
  public void testModifiableInvalidJSONObject() throws Exception {
    final String invalid =
        "{\"first_value\":\"first\",\"second_value\":\"second\",\"my_long_value\":3500,\"my_local_time\":\"01:02:03.654\",\"my_local_date_time\":\"201a1-11-11T09:08:07.456\"}";
    final String valid =
        "{\"first_value\":\"first\",\"second_value\":\"second\",\"my_long_value\":3500,\"my_local_time\":\"01:02:03.654\",\"my_local_date_time\":\"2011-11-11T09:08:07.456\"}";

    this.propertiesProvider.addProperty("test-JSON-object", invalid);
    this.propertiesProvider.addProperty("test-modifiable-JSON-object", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testJSONObject);
    Assert.assertNull(testObj.testModifiableJSONObject.get());

    this.propertiesProvider.addProperty("test-modifiable-JSON-object", valid);
    Assert.assertNotNull(testObj.testModifiableJSONObject.get());
  }

  @Test
  public void testModifiableLocalTime() throws Exception {
    final String first = "01:02:03.654";
    final String second = "02:03:04.754";
    final String third = "06:01:06.777";

    this.propertiesProvider.addProperty("test-local-time", first);
    this.propertiesProvider.addProperty("test-modifiable-local-time", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(first, testObj.testLocalTime.toString());
    Assert.assertEquals(second, testObj.testModifiableLocalTime.get().toString());

    this.propertiesProvider.addProperty("test-local-time", third);
    this.propertiesProvider.addProperty("test-modifiable-local-time", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(first, testObj.testLocalTime.toString());

    // modifiable changes
    Assert.assertEquals(third, testObj.testModifiableLocalTime.get().toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(first, testObj.testLocalTime.toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalTime.get());
  }

  @Test
  public void testModifiableInvalidLocalTime() throws Exception {
    final String first = "01:02:03.654";
    final String invalid = "06:01.06.777";

    this.propertiesProvider.addProperty("test-local-time", invalid);
    this.propertiesProvider.addProperty("test-modifiable-local-time", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testLocalTime);
    Assert.assertNull(testObj.testModifiableLocalTime.get());

    this.propertiesProvider.addProperty("test-modifiable-local-time", first);
    Assert.assertEquals(first, testObj.testModifiableLocalTime.get().toString());

    // incorrect value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-local-time", invalid);
    Assert.assertEquals(first, testObj.testModifiableLocalTime.get().toString());

    this.propertiesProvider.removeProperty("test-modifiable-local-time");
    Assert.assertNull(testObj.testModifiableLocalTime.get());
  }

  @Test
  public void testModifiableLocalDateTime() throws Exception {
    final String first = "2011-11-11T09:08:07.456";
    final String second = "2012-12-10T08:09:03.111";
    final String third = "2000-09-29T15:59:59.123456789";

    this.propertiesProvider.addProperty("test-local-date-time", first);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(first, testObj.testLocalDateTime.toString());
    Assert.assertEquals(second, testObj.testModifiableLocalDateTime.get().toString());

    this.propertiesProvider.addProperty("test-local-date-time", third);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(first, testObj.testLocalDateTime.toString());

    // modifiable changes
    Assert.assertEquals(third, testObj.testModifiableLocalDateTime.get().toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(first, testObj.testLocalDateTime.toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalDateTime.get());
  }

  @Test
  public void testModifiableInvalidLocalDateTime() throws Exception {
    final String first = "2011-11-11T09:08:07.456";
    final String invalid = "2000-09/29T06:01.06.777";

    this.propertiesProvider.addProperty("test-local-date-time", invalid);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testLocalDateTime);
    Assert.assertNull(testObj.testModifiableLocalDateTime.get());

    this.propertiesProvider.addProperty("test-modifiable-local-date-time", first);
    Assert.assertEquals(first, testObj.testModifiableLocalDateTime.get().toString());

    // incorrect value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-local-date-time", invalid);
    Assert.assertEquals(first, testObj.testModifiableLocalDateTime.get().toString());

    this.propertiesProvider.removeProperty("test-modifiable-local-date-time");
    Assert.assertNull(testObj.testModifiableLocalDateTime.get());
  }

  @Test
  public void testModifiableIntegerList() throws Exception {
    this.propertiesProvider.addProperty("test-integer-list", "[5800, 600]");
    this.propertiesProvider.addProperty("test-modifiable-integer-list", "[8899, 9000, 6033]");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(Arrays.asList(5800, 600),
        testObj.testIntegerList);
    Assert.assertEquals(Arrays.asList(8899, 9000, 6033),
        testObj.testModifiableIntegerList.get());

    this.propertiesProvider.addProperty("test-integer-list", "[1111, 5050]");
    this.propertiesProvider.addProperty("test-modifiable-integer-list", "[6060, 7, 123]");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(Arrays.asList(5800, 600),
        testObj.testIntegerList);

    // modifiable changes
    Assert.assertEquals(Arrays.asList(6060, 7, 123),
        testObj.testModifiableIntegerList.get());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(Arrays.asList(5800, 600),
        testObj.testIntegerList);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableIntegerList.get());

  }

  @Test
  public void testModifiableMap() throws Exception {
    this.propertiesProvider.addProperty("test-map",
        "{ \"key1\": \"4652\", \"key2\": \"88811\" }");
    this.propertiesProvider.addProperty("test-modifiable-map",
        "{ \"key1\": \"123\", \"key2\": \"456\" }");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(Integer.valueOf(4652), testObj.testMap.get("key1"));
    Assert.assertEquals(Integer.valueOf(88811), testObj.testMap.get("key2"));

    Assert.assertEquals(Integer.valueOf(123), testObj.testModifiableMap.get().get("key1"));
    Assert.assertEquals(Integer.valueOf(456), testObj.testModifiableMap.get().get("key2"));

    this.propertiesProvider.addProperty("test-map",
        "{ \"key1\": \"2223\", \"key2\": \"3336\" }");
    this.propertiesProvider.addProperty("test-modifiable-map",
        "{ \"key1\": \"789\", \"key2\": \"258\" }");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(Integer.valueOf(4652), testObj.testMap.get("key1"));
    Assert.assertEquals(Integer.valueOf(88811), testObj.testMap.get("key2"));

    // modifiable changes
    Assert.assertEquals(Integer.valueOf(789), testObj.testModifiableMap.get().get("key1"));
    Assert.assertEquals(Integer.valueOf(258), testObj.testModifiableMap.get().get("key2"));

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(Integer.valueOf(4652), testObj.testMap.get("key1"));
    Assert.assertEquals(Integer.valueOf(88811), testObj.testMap.get("key2"));

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableMap.get());

  }

  @Test
  public void testComplexModifiableLongList() throws Exception {
    this.propertiesProvider.addProperty("test-complex-long-list", "[5MiB, 1MiB]");
    this.propertiesProvider.addProperty("test-modifiable-complex-long-list", "[30GiB, 2TiB]");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(
        Arrays.asList(
            Size.getSizeInBytes("5MiB"),
            Size.getSizeInBytes("1MiB")),
        testObj.testComplexLongList);
    Assert.assertEquals(Arrays.asList(
        Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB")),
        testObj.testModifiableComplexLongList.get());

    this.propertiesProvider.addProperty("test-complex-long-list", "[1111, 5050]");
    this.propertiesProvider.addProperty("test-modifiable-complex-long-list", "[100GiB, 1TiB]");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(
        Arrays.asList(
            Size.getSizeInBytes("5MiB"),
            Size.getSizeInBytes("1MiB")),
        testObj.testComplexLongList);

    // modifiable changes
    Assert.assertEquals(Arrays.asList(
        Size.getSizeInBytes("100GiB"),
        Size.getSizeInBytes("1TiB")),
        testObj.testModifiableComplexLongList.get());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(
        Arrays.asList(
            Size.getSizeInBytes("5MiB"),
            Size.getSizeInBytes("1MiB")),
        testObj.testComplexLongList);

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableComplexLongList.get());

  }

  @Test
  public void testComplexModifiableInvalidLongList() throws Exception {
    this.propertiesProvider.addProperty("test-complex-long-list", "[5MiB, 1XiB]");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testComplexLongList);

    this.propertiesProvider.addProperty("test-modifiable-complex-long-list", "[30GiB, 2TiB]");
    Assert.assertEquals(Arrays.asList(
        Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB")),
        testObj.testModifiableComplexLongList.get());

    // incorrect value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-complex-long-list", "[100GiB, xxx]");
    Assert.assertEquals(Arrays.asList(
        Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB")),
        testObj.testModifiableComplexLongList.get());

    this.propertiesProvider.removeProperty("test-modifiable-complex-long-list");
    Assert.assertNull(testObj.testModifiableComplexLongList.get());

  }

  @Test
  public void testModifiableLocalTimeList() throws Exception {
    this.propertiesProvider.addProperty("test-local-time-list", "[\"01:02:03.654\", \"02:03:04.754\"]");
    this.propertiesProvider.addProperty("test-modifiable-local-time-list",
        "[\"06:01:06.777\", \"06:01:06.123456789\", \"07:01:06\"]");

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(2, testObj.testLocalTimeList.size());
    Assert.assertEquals("01:02:03.654", testObj.testLocalTimeList.get(0).toString());
    Assert.assertEquals("02:03:04.754", testObj.testLocalTimeList.get(1).toString());

    Assert.assertEquals(3, testObj.testModifiableLocalTimeList.get().size());
    Assert.assertEquals("06:01:06.777", testObj.testModifiableLocalTimeList.get().get(0).toString());
    Assert.assertEquals("06:01:06.123456789", testObj.testModifiableLocalTimeList.get().get(1).toString());
    Assert.assertEquals("07:01:06", testObj.testModifiableLocalTimeList.get().get(2).toString());

    this.propertiesProvider.addProperty("test-local-time-list", "[\"11:11:11.111\", \"12:12\"]");
    this.propertiesProvider.addProperty("test-modifiable-local-time-list", "[\"11:11:11.111\", \"12:12\"]");

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(2, testObj.testLocalTimeList.size());
    Assert.assertEquals("01:02:03.654", testObj.testLocalTimeList.get(0).toString());
    Assert.assertEquals("02:03:04.754", testObj.testLocalTimeList.get(1).toString());

    // modifiable changes
    Assert.assertEquals(2, testObj.testModifiableLocalTimeList.get().size());
    Assert.assertEquals("11:11:11.111", testObj.testModifiableLocalTimeList.get().get(0).toString());
    Assert.assertEquals("12:12", testObj.testModifiableLocalTimeList.get().get(1).toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(2, testObj.testLocalTimeList.size());
    Assert.assertEquals("01:02:03.654", testObj.testLocalTimeList.get(0).toString());
    Assert.assertEquals("02:03:04.754", testObj.testLocalTimeList.get(1).toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalTimeList.get());
  }

  @Test
  public void testModifiableInvalidLocalTimeList() throws Exception {
    // use invalid value
    final String invalid = "[\"09:01:06.777\", \"09:01:06.778\", \"09a01.06.778\"]";
    this.propertiesProvider.addProperty("test-local-time-list", invalid);
    this.propertiesProvider.addProperty("test-modifiable-local-time-list", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testLocalTimeList);
    Assert.assertNull(testObj.testModifiableLocalTimeList.get());

    // change to valid value
    this.propertiesProvider.addProperty("test-modifiable-local-time-list",
        "[\"06:01:06.777\", \"06:01:06.778\", \"07:01:06.778\"]");
    Assert.assertEquals(3, testObj.testModifiableLocalTimeList.get().size());
    Assert.assertEquals("06:01:06.777", testObj.testModifiableLocalTimeList.get().get(0).toString());
    Assert.assertEquals("06:01:06.778", testObj.testModifiableLocalTimeList.get().get(1).toString());
    Assert.assertEquals("07:01:06.778", testObj.testModifiableLocalTimeList.get().get(2).toString());

    // invalid value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-local-time-list", invalid);
    Assert.assertEquals(3, testObj.testModifiableLocalTimeList.get().size());
    Assert.assertEquals("06:01:06.777", testObj.testModifiableLocalTimeList.get().get(0).toString());
    Assert.assertEquals("06:01:06.778", testObj.testModifiableLocalTimeList.get().get(1).toString());
    Assert.assertEquals("07:01:06.778", testObj.testModifiableLocalTimeList.get().get(2).toString());

    this.propertiesProvider.removeProperty("test-modifiable-local-time-list");
    Assert.assertNull(testObj.testModifiableLocalTimeList.get());
  }

  @Test
  public void testModifiableLocalTimeMap() throws Exception {
    final String first =
        "{\"09:08:07.456\" : \"00000000-0000-0000-0000-000000000000\",  \"09:08:07\": \"00000000-0000-0000-0000-000000000001\"}";
    final String second =
        "{\"08:09:03.111\" : \"00000000-0000-0000-0000-000000000002\",  \"08:09:23.111\": \"00000000-0000-0000-0000-000000000003\"}";
    final String third =
        "{\"15:59:59.123456789\" : \"00000000-0000-0000-0000-000000000004\",  \"15:59\": \"00000000-0000-0000-0000-000000000005\",  \"08:59:03.222\": \"00000000-0000-0000-0000-000000000006\"}";

    this.propertiesProvider.addProperty("test-local-time-map", first);
    this.propertiesProvider.addProperty("test-modifiable-local-time-map", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    final LocalTime time1 = LocalTime.of(9, 8, 7, 456000000);
    final LocalTime time2 = LocalTime.of(9, 8, 7);
    final LocalTime time3 = LocalTime.of(8, 9, 3, 111000000);
    final LocalTime time4 = LocalTime.of(8, 9, 23, 111000000);
    final LocalTime time5 = LocalTime.of(15, 59, 59, 123456789);
    final LocalTime time6 = LocalTime.of(15, 59);
    final LocalTime time7 = LocalTime.of(8, 59, 3, 222000000);

    Assert.assertEquals(2, testObj.testLocalTimeMap.size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000000", testObj.testLocalTimeMap.get(time1).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000001", testObj.testLocalTimeMap.get(time2).toString());

    Assert.assertEquals(2, testObj.testModifiableLocalTimeMap.get().size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000002",
        testObj.testModifiableLocalTimeMap.get().get(time3).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000003",
        testObj.testModifiableLocalTimeMap.get().get(time4).toString());

    this.propertiesProvider.addProperty("test-local-time-map", third);
    this.propertiesProvider.addProperty("test-modifiable-local-time-map", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(2, testObj.testLocalTimeMap.size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000000", testObj.testLocalTimeMap.get(time1).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000001", testObj.testLocalTimeMap.get(time2).toString());

    // modifiable changes
    Assert.assertEquals(3, testObj.testModifiableLocalTimeMap.get().size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000004",
        testObj.testModifiableLocalTimeMap.get().get(time5).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000005",
        testObj.testModifiableLocalTimeMap.get().get(time6).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000006",
        testObj.testModifiableLocalTimeMap.get().get(time7).toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(2, testObj.testLocalTimeMap.size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000000", testObj.testLocalTimeMap.get(time1).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000001", testObj.testLocalTimeMap.get(time2).toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());
  }

  @Test
  public void testModifiableInvalidLocalTimeMap() throws Exception {
    // use invalid value
    final String invalid =
        "{\"09:08:07.456\" : \"00000000-0000-0000-0000-000000000001\",  \"09:08a:07\": \"00000000-0000-0000-0000-000000000002\"}";
    this.propertiesProvider.addProperty("test-local-time-map", invalid);
    this.propertiesProvider.addProperty("test-modifiable-local-time-map", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testLocalTimeMap);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());

    // change to valid value
    this.propertiesProvider.addProperty("test-modifiable-local-time-map",
        "{\"09:08:07.456\" : \"00000000-0000-0000-0000-000000000001\",  \"09:08:07\": \"00000000-0000-0000-0000-000000000002\"}");

    final LocalTime time1 = LocalTime.of(9, 8, 7, 456000000);
    final LocalTime time2 = LocalTime.of(9, 8, 7);

    Assert.assertEquals(2, testObj.testModifiableLocalTimeMap.get().size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000001",
        testObj.testModifiableLocalTimeMap.get().get(time1).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000002",
        testObj.testModifiableLocalTimeMap.get().get(time2).toString());

    // invalid value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-local-time-map", invalid);

    Assert.assertEquals(2, testObj.testModifiableLocalTimeMap.get().size());
    Assert.assertEquals("00000000-0000-0000-0000-000000000001",
        testObj.testModifiableLocalTimeMap.get().get(time1).toString());
    Assert.assertEquals("00000000-0000-0000-0000-000000000002",
        testObj.testModifiableLocalTimeMap.get().get(time2).toString());

    this.propertiesProvider.removeProperty("test-modifiable-local-time-map");
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());
  }

  @Test
  public void testModifiableLocalTimeMapOverride() throws Exception {
    // duplicated keys are not allowed
    final String first =
        "{\"09:08:07.456\" : \"00000000-0000-0000-0000-000000000001\",  \"12:18:17.123\": \"00000000-0000-0000-0000-000000000002\", \"09:08:07.456\" : \"00000000-0000-0000-0000-000000000003\"}";
    final String second =
        "{\"09:08:07.456\" : \"00000000-0000-0000-0000-000000000004\",  \"12:18:17.123\": \"00000000-0000-0000-0000-000000000005\", \"09:08:07.456\" : \"00000000-0000-0000-0000-000000000006\", \"09:08:07.456\" : \"00000000-0000-0000-0000-000000000007\"}";
    final String third =
        "{\"08:09:00.000\" : \"00000000-0000-0000-0000-000000000008\",  \"18:19:00\": \"00000000-0000-0000-0000-000000000009\", \"08:09:00\" : \"00000000-0000-0000-0000-000000000010\"}";
    final String forth =
        "{\"08:09:00\" : \"00000000-0000-0000-0000-000000000011\",  \"18:19:00\" : \"00000000-0000-0000-0000-000000000012\",  \"08:09:00.000000000\" : \"00000000-0000-0000-0000-000000000013\"}";
    final String fifth =
        "{\"08:09:00\" : \"00000000-0000-0000-0000-000000000014\",  \"18:19:00.000000000\" : \"00000000-0000-0000-0000-000000000015\",  \"08:09\" : \"00000000-0000-0000-0000-000000000016\", \"18:19\" : \"00000000-0000-0000-0000-000000000017\"}";

    this.propertiesProvider.addProperty("test-modifiable-local-time-map", first);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());

    this.propertiesProvider.addProperty("test-modifiable-local-time-map", second);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());

    this.propertiesProvider.addProperty("test-modifiable-local-time-map", third);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());

    this.propertiesProvider.addProperty("test-modifiable-local-time-map", forth);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());

    this.propertiesProvider.addProperty("test-modifiable-local-time-map", fifth);
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());


    // change to valid value
    this.propertiesProvider.addProperty("test-modifiable-local-time-map",
        "{\"09:08:07.456\" : \"10000000-0000-0000-0000-000000000001\",  \"12:18:17.123\": \"10000000-0000-0000-0000-000000000002\"}");

    final LocalTime time1 = LocalTime.of(9, 8, 7, 456000000);
    final LocalTime time2 = LocalTime.of(12, 18, 17, 123000000);

    Assert.assertEquals(2, testObj.testModifiableLocalTimeMap.get().size());
    Assert.assertEquals("10000000-0000-0000-0000-000000000001",
        testObj.testModifiableLocalTimeMap.get().get(time1).toString());
    Assert.assertEquals("10000000-0000-0000-0000-000000000002",
        testObj.testModifiableLocalTimeMap.get().get(time2).toString());

    this.propertiesProvider.clear();
    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalTimeMap.get());
  }

  @Test
  public void testModifiableLocalDateTimeMap() throws Exception {
    final String first =
        "{\"key1\": \"2011-11-11T09:08:07.456\", \"key2\": \"2019-01-01T09:08:17.456\"}";
    final String second =
        "{\"key1\": \"2012-12-10T08:09:03.111\", \"key2\": \"2019-02-19T08:09:03.111\"}";
    final String third =
        "{\"key1\": \"2000-09-29T15:59:59.123456789\", \"key2\": \"2006-06-11T23:59:59.999\"}";

    this.propertiesProvider.addProperty("test-local-date-time-map", first);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map", second);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertEquals(2, testObj.testLocalDateTimeMap.size());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testLocalDateTimeMap.get("key1").toString());
    Assert.assertEquals("2019-01-01T09:08:17.456", testObj.testLocalDateTimeMap.get("key2").toString());

    Assert.assertEquals(2, testObj.testModifiableLocalDateTimeMap.get().size());
    Assert.assertEquals("2012-12-10T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key1").toString());
    Assert.assertEquals("2019-02-19T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key2").toString());

    this.propertiesProvider.addProperty("test-local-date-time-map", third);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map", third);

    // non-modifiable doesn't change after object construction
    Assert.assertEquals(2, testObj.testLocalDateTimeMap.size());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testLocalDateTimeMap.get("key1").toString());
    Assert.assertEquals("2019-01-01T09:08:17.456", testObj.testLocalDateTimeMap.get("key2").toString());

    // modifiable changes
    Assert.assertEquals(2, testObj.testModifiableLocalDateTimeMap.get().size());
    Assert.assertEquals("2000-09-29T15:59:59.123456789",
        testObj.testModifiableLocalDateTimeMap.get().get("key1").toString());
    Assert.assertEquals("2006-06-11T23:59:59.999", testObj.testModifiableLocalDateTimeMap.get().get("key2").toString());

    this.propertiesProvider.clear();

    // non-modifiable doesn't change after property clearing
    Assert.assertEquals(2, testObj.testLocalDateTimeMap.size());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.testLocalDateTimeMap.get("key1").toString());
    Assert.assertEquals("2019-01-01T09:08:17.456", testObj.testLocalDateTimeMap.get("key2").toString());

    // modifiable is cleared
    Assert.assertNull(testObj.testModifiableLocalDateTimeMap.get());
  }

  @Test
  public void testModifiableInvalidLocalDateTimeMap() throws Exception {
    // use invalid value
    final String invalid = "{\"key1\": \"2032-12-10T18:09:03.111\", \"key2\": \"2029-02-19T18:09a:03.111\"}";
    this.propertiesProvider.addProperty("test-local-date-time-map", invalid);
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map", invalid);

    final TestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(TestImpl.class)
            .initialize();

    Assert.assertNull(testObj.testLocalDateTimeMap);
    Assert.assertNull(testObj.testModifiableLocalDateTimeMap.get());

    // change to valid value
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map",
        "{\"key1\": \"2012-12-10T08:09:03.111\", \"key2\": \"2019-02-19T08:09:03.111\"}");

    Assert.assertEquals(2, testObj.testModifiableLocalDateTimeMap.get().size());
    Assert.assertEquals("2012-12-10T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key1").toString());
    Assert.assertEquals("2019-02-19T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key2").toString());

    // invalid value doesn't change the old value
    this.propertiesProvider.addProperty("test-modifiable-local-date-time-map", invalid);

    Assert.assertEquals(2, testObj.testModifiableLocalDateTimeMap.get().size());
    Assert.assertEquals("2012-12-10T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key1").toString());
    Assert.assertEquals("2019-02-19T08:09:03.111", testObj.testModifiableLocalDateTimeMap.get().get("key2").toString());

    this.propertiesProvider.removeProperty("test-modifiable-local-date-time-map");
    Assert.assertNull(testObj.testModifiableLocalDateTimeMap.get());
  }

  public static class Observable<T> {
    private T value;

    public Observable(final T value) {
      this.value = value;
    }

    public void update(final T value) {
      this.value = value;
    }

    public T get() {
      return this.value;
    }
  }

  public static class ClassWithObservables {
    public Observable<Integer> myInt;
    public Observable<Long> myLong;
    public Observable<List<Long>> longList;
    public Observable<LocalTime> myLocalTimeCWO;
  }

  public static class ImmutableClassWithObservables {
    public static class Builder {
      public Observable<Integer> myInt;
      public Observable<Long> myLong;
      public Observable<List<Long>> longList;
      public Observable<LocalTime> myLocalTimeICWO;

      public ImmutableClassWithObservables build() {
        return new ImmutableClassWithObservables(this.myInt, this.myLong, this.longList, this.myLocalTimeICWO);
      }
    }

    private final Observable<Integer> myInt;
    private final Observable<Long> myLong;
    private final Observable<List<Long>> longList;
    private final Observable<LocalTime> myLocalTimeICWO;

    public ImmutableClassWithObservables(final Observable<Integer> myInt,
        final Observable<Long> myLong,
        final Observable<List<Long>> longList,
        final Observable<LocalTime> myLocalTimeICWO) {
      this.myInt = myInt;
      this.myLong = myLong;
      this.longList = longList;
      this.myLocalTimeICWO = myLocalTimeICWO;
    }
  }

  @Test
  public void testObservablePattern() throws Exception {
    this.propertiesProvider.addProperty("test-int-value", "32");
    this.propertiesProvider.addProperty("test-long-value", "10000");
    this.propertiesProvider.addProperty("test-long-value-list", "[30GiB, 2TiB, 3PiB]");
    this.propertiesProvider.addProperty("my-local-time-c-w-o", "09:08:07.456");

    final ClassWithObservables testObj =
        this.bindingsProvider.getDefaultImplementation(ClassWithObservables.class)
            .initialize();
    Assert.assertEquals(Integer.valueOf(32), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(10000), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB"), Size.getSizeInBytes("3PiB")), testObj.longList.get());
    Assert.assertEquals("09:08:07.456", testObj.myLocalTimeCWO.get().toString());

    this.propertiesProvider.addProperty("test-int-value", "64");
    this.propertiesProvider.addProperty("test-long-value", "80GiB");
    this.propertiesProvider.addProperty("test-long-value-list", "[10MB, 10, 1024]");
    this.propertiesProvider.addProperty("my-local-time-c-w-o", "06:01:06.777");

    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeCWO.get().toString());
  }


  @Test
  public void testObservablePatternWithBuilder() throws Exception {
    this.propertiesProvider.addProperty("test-int-value", "32");
    this.propertiesProvider.addProperty("test-long-value", "10000");
    this.propertiesProvider.addProperty("test-long-value-list", "[30GiB, 2TiB, 3PiB]");
    this.propertiesProvider.addProperty("my-local-time-i-c-w-o", "09:08:07.456");

    final ImmutableClassWithObservables testObj =
        this.bindingsProvider.getDefaultImplementation(ImmutableClassWithObservables.class)
            .initialize();
    Assert.assertEquals(Integer.valueOf(32), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(10000), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB"), Size.getSizeInBytes("3PiB")), testObj.longList.get());
    Assert.assertEquals("09:08:07.456", testObj.myLocalTimeICWO.get().toString());

    this.propertiesProvider.addProperty("test-int-value", "64");
    this.propertiesProvider.addProperty("test-long-value", "80GiB");
    this.propertiesProvider.addProperty("test-long-value-list", "[10MB, 10, 1024]");
    this.propertiesProvider.addProperty("my-local-time-i-c-w-o", "06:01:06.777");

    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeICWO.get().toString());
  }

  public static class AlreadyIntSupplier implements IntSupplier {
    @Override
    public int getAsInt() {
      return 300;
    }
  }

  public static class ClassWithSuppliers {
    public Supplier<Integer> myInt;
    public Supplier<Long> myLong;
    public Supplier<List<Long>> longList;
    public Supplier<LocalTime> myLocalTimeCWS;

    public IntSupplier primitiveInt;
    public LongSupplier primitiveLong;
    public BooleanSupplier primitiveBoolean;
    public DoubleSupplier primitiveDouble;

    public IntSupplier alreadyASupplier;
  }

  public static class ImmutableClassWithSuppliers {
    public static class Builder {
      public Supplier<Integer> myInt;
      public Supplier<Long> myLong;
      public Supplier<List<Long>> longList;
      public Supplier<LocalTime> myLocalTimeICWS;

      public IntSupplier primitiveInt;
      public LongSupplier primitiveLong;
      public BooleanSupplier primitiveBoolean;
      public DoubleSupplier primitiveDouble;
      public IntSupplier alreadyASupplier;

      public ImmutableClassWithSuppliers build() {
        return new ImmutableClassWithSuppliers(this.myInt, this.myLong, this.longList,
            this.myLocalTimeICWS,
            this.primitiveInt, this.primitiveLong, this.primitiveBoolean, this.primitiveDouble, this.alreadyASupplier);
      }
    }

    private final Supplier<Integer> myInt;
    private final Supplier<Long> myLong;
    private final Supplier<List<Long>> longList;
    private final Supplier<LocalTime> myLocalTimeICWS;

    private final IntSupplier primitiveInt;
    private final LongSupplier primitiveLong;
    private final BooleanSupplier primitiveBoolean;
    private final DoubleSupplier primitiveDouble;
    private final IntSupplier alreadyASupplier;

    public ImmutableClassWithSuppliers(final Supplier<Integer> myInt,
        final Supplier<Long> myLong,
        final Supplier<List<Long>> longList,
        final Supplier<LocalTime> myLocalTimeICWS,
        final IntSupplier primitiveInt,
        final LongSupplier primitiveLong,
        final BooleanSupplier primitiveBoolean,
        final DoubleSupplier primitiveDouble,
        final IntSupplier alreadyASupplier) {
      this.myInt = myInt;
      this.myLong = myLong;
      this.longList = longList;
      this.myLocalTimeICWS = myLocalTimeICWS;
      this.primitiveInt = primitiveInt;
      this.primitiveLong = primitiveLong;
      this.primitiveBoolean = primitiveBoolean;
      this.primitiveDouble = primitiveDouble;
      this.alreadyASupplier = alreadyASupplier;
    }
  }

  @Test
  public void testSupplierPattern() throws Exception {
    this.propertiesProvider.addProperty("test-int-value", "32");
    this.propertiesProvider.addProperty("test-long-value", "10000");
    this.propertiesProvider.addProperty("test-long-value-list", "[30GiB, 2TiB, 3PiB]");
    this.propertiesProvider.addProperty("my-local-time-c-w-s", "01:02:03.654");
    this.propertiesProvider.addProperty("test-primitive-int-value", "13");
    this.propertiesProvider.addProperty("test-primitive-long-value", "99999999999");
    this.propertiesProvider.addProperty("test-primitive-double-value", "33.3339");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "true");

    final ClassWithSuppliers testObj =
        this.bindingsProvider.getDefaultImplementation(ClassWithSuppliers.class)
            .initialize();
    Assert.assertEquals(Integer.valueOf(32), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(10000), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB"), Size.getSizeInBytes("3PiB")), testObj.longList.get());
    Assert.assertEquals("01:02:03.654", testObj.myLocalTimeCWS.get().toString());
    Assert.assertEquals(13, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(99999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.3339D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertTrue(testObj.primitiveBoolean.getAsBoolean());
    Assert.assertEquals(new AlreadyIntSupplier().getAsInt(), testObj.alreadyASupplier.getAsInt());


    this.propertiesProvider.addProperty("test-int-value", "64");
    this.propertiesProvider.addProperty("test-long-value", "80GiB");
    this.propertiesProvider.addProperty("test-long-value-list", "[10MB, 10, 1024]");
    this.propertiesProvider.addProperty("my-local-time-c-w-s", "06:01:06.777");
    this.propertiesProvider.addProperty("test-primitive-int-value", "24");
    this.propertiesProvider.addProperty("test-primitive-long-value", "100099999999999");
    this.propertiesProvider.addProperty("test-primitive-double-value", "33.888");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "false");

    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeCWS.get().toString());
    Assert.assertEquals(24, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(100099999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.888D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());

    // Make sure invalid values don't change previous values
    this.propertiesProvider.addProperty("test-int-value", "invalid int");
    this.propertiesProvider.addProperty("test-long-value", "invalid long");
    this.propertiesProvider.addProperty("test-long-value-list", "this is not a list");
    this.propertiesProvider.addProperty("my-local-time-c-w-s", "invalid LocalTime");
    this.propertiesProvider.addProperty("test-primitive-int-value", "invalid int");
    this.propertiesProvider.addProperty("test-primitive-long-value", "invalid long");
    this.propertiesProvider.addProperty("test-primitive-double-value", "invalid double");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "invalid boolean");

    // keep previous values
    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeCWS.get().toString());
    Assert.assertEquals(24, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(100099999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.888D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());

    // Booleans are interesting... Boolean.valueOf returns false for anything other than "true"
    // (ignoring case); So, if you set this to true
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "true");
    // assert it's true now
    Assert.assertTrue(testObj.primitiveBoolean.getAsBoolean());
    // then set it to an invalid value
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "invalid boolean");
    // it resolves to false (instead of just returning the previous value) - but that's OK, I guess.
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());
  }


  @Test
  public void testSupplierPatternWithBuilder() throws Exception {
    this.propertiesProvider.addProperty("test-int-value", "32");
    this.propertiesProvider.addProperty("test-long-value", "10000");
    this.propertiesProvider.addProperty("test-long-value-list", "[30GiB, 2TiB, 3PiB]");
    this.propertiesProvider.addProperty("my-local-time-i-c-w-s", "01:02:03.654");
    this.propertiesProvider.addProperty("test-primitive-int-value", "13");
    this.propertiesProvider.addProperty("test-primitive-long-value", "99999999999");
    this.propertiesProvider.addProperty("test-primitive-double-value", "33.3339");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "true");

    final ImmutableClassWithSuppliers testObj =
        this.bindingsProvider.getDefaultImplementation(ImmutableClassWithSuppliers.class)
            .initialize();
    Assert.assertEquals(Integer.valueOf(32), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(10000), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("30GiB"),
        Size.getSizeInBytes("2TiB"), Size.getSizeInBytes("3PiB")), testObj.longList.get());
    Assert.assertEquals("01:02:03.654", testObj.myLocalTimeICWS.get().toString());
    Assert.assertEquals(13, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(99999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.3339D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertTrue(testObj.primitiveBoolean.getAsBoolean());
    Assert.assertEquals(new AlreadyIntSupplier().getAsInt(), testObj.alreadyASupplier.getAsInt());

    this.propertiesProvider.addProperty("test-int-value", "64");
    this.propertiesProvider.addProperty("test-long-value", "80GiB");
    this.propertiesProvider.addProperty("test-long-value-list", "[10MB, 10, 1024]");
    this.propertiesProvider.addProperty("my-local-time-i-c-w-s", "06:01:06.777");
    this.propertiesProvider.addProperty("test-primitive-int-value", "24");
    this.propertiesProvider.addProperty("test-primitive-long-value", "100099999999999");
    this.propertiesProvider.addProperty("test-primitive-double-value", "33.888");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "false");

    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeICWS.get().toString());
    Assert.assertEquals(24, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(100099999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.888D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());

    // Make sure invalid values don't change previous values
    this.propertiesProvider.addProperty("test-int-value", "invalid int");
    this.propertiesProvider.addProperty("test-long-value", "invalid long");
    this.propertiesProvider.addProperty("test-long-value-list", "this is not a list");
    this.propertiesProvider.addProperty("my-local-time-i-c-w-s", "invalid LocalTime");
    this.propertiesProvider.addProperty("test-primitive-int-value", "invalid int");
    this.propertiesProvider.addProperty("test-primitive-long-value", "invalid long");
    this.propertiesProvider.addProperty("test-primitive-double-value", "invalid double");
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "invalid boolean");

    // keep previous values
    Assert.assertEquals(Integer.valueOf(64), testObj.myInt.get());
    Assert.assertEquals(Long.valueOf(Size.getSizeInBytes("80GiB")), testObj.myLong.get());
    Assert.assertEquals(Arrays.asList(Size.getSizeInBytes("10MB"),
        10l, 1024l), testObj.longList.get());
    Assert.assertEquals("06:01:06.777", testObj.myLocalTimeICWS.get().toString());
    Assert.assertEquals(24, testObj.primitiveInt.getAsInt());
    Assert.assertEquals(100099999999999L, testObj.primitiveLong.getAsLong());
    Assert.assertEquals(33.888D, testObj.primitiveDouble.getAsDouble(), 0D);
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());

    // Booleans are interesting... Boolean.valueOf returns false for anything other than "true"
    // (ignoring case); So, if you set this to true
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "true");
    // assert it's true now
    Assert.assertTrue(testObj.primitiveBoolean.getAsBoolean());
    // then set it to an invalid value
    this.propertiesProvider.addProperty("test-primitive-boolean-value", "invalid boolean");
    // it resolves to false (instead of just returning the previous value) - but that's OK, I guess.
    Assert.assertFalse(testObj.primitiveBoolean.getAsBoolean());
  }

  @Test
  public void testBuilder() throws Exception {
    this.propertiesProvider.addProperty("testString1", "first");
    this.propertiesProvider.addProperty("test_string_2", "second");
    this.propertiesProvider.addProperty("test-string-3", "third");
    this.propertiesProvider.addProperty("test-local-time-1", "01:02:03.654");
    this.propertiesProvider.addProperty("test-local-date-time-1", "2011-11-11T09:08:07.456");

    final BuiltTestImpl testObj =
        this.bindingsProvider.getDefaultImplementation(BuiltTestImpl.class)
            .initialize();

    Assert.assertEquals("first", testObj.getTestString1());
    Assert.assertEquals("second", testObj.getTestString2());
    Assert.assertEquals("third", testObj.getTestString3());
    Assert.assertEquals("01:02:03.654", testObj.getTestLocalTime1().toString());
    Assert.assertEquals("2011-11-11T09:08:07.456", testObj.getTestLocalDateTime1().toString());

    this.propertiesProvider.addProperty("test-string-1", "fourth");
    this.propertiesProvider.addProperty("test-string-2", "fifth");
    this.propertiesProvider.addProperty("test-string-3", "sixth");
    this.propertiesProvider.addProperty("test-local-time-1", "06:01:06.777");
    this.propertiesProvider.addProperty("test-local-date-time-1", "2019-02-19T08:09:03.111");

    Assert.assertEquals("fourth", testObj.getTestString1());
    Assert.assertEquals("fifth", testObj.getTestString2());
    Assert.assertEquals("third", testObj.getTestString3()); // doesn't update, no setter
    Assert.assertEquals("01:02:03.654", testObj.getTestLocalTime1().toString()); // doesn't update, no setter
    Assert.assertEquals("2019-02-19T08:09:03.111", testObj.getTestLocalDateTime1().toString());

    this.propertiesProvider.clear();
    Assert.assertNull(testObj.getTestString1());
    Assert.assertNull(testObj.getTestString2());
    Assert.assertEquals("third", testObj.getTestString3()); // doesn't update, no setter
    Assert.assertEquals("01:02:03.654", testObj.getTestLocalTime1().toString()); // doesn't update, no setter
    Assert.assertNull(testObj.getTestLocalDateTime1());
  }
}
