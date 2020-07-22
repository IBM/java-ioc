/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

// TODO: Describe class or interface
public class TypeUtilsTest {
  @Test
  public void test1() {
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(String.class), String.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(Integer.class), Integer.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(int.class), Integer.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(Boolean.class), Boolean.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(boolean.class), Boolean.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(char.class), Character.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(short.class), Short.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(long.class), Long.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(float.class), Float.class);
    Assert.assertEquals(TypeUtils.getNonPrimitiveFor(double.class), Double.class);

    Assert.assertEquals(TypeUtils.getPrimitiveFor(Integer.class), int.class);
    Assert.assertEquals(TypeUtils.getPrimitiveFor(Boolean.class), boolean.class);

    Assert.assertEquals(TypeUtils.INT, TypeUtils.getByPrimitiveName("int"));
    Assert.assertEquals(TypeUtils.DOUBLE, TypeUtils.getByPrimitiveName("double"));

    Assert.assertEquals(TypeUtils.INT, TypeUtils.getByPrimitiveClass(int.class));
    Assert.assertEquals(TypeUtils.DOUBLE, TypeUtils.getByPrimitiveClass(double.class));

    Assert.assertEquals(TypeUtils.BOOLEAN, TypeUtils.getByWrapperClass(Boolean.class));
    Assert.assertEquals(TypeUtils.CHAR, TypeUtils.getByWrapperClass(Character.class));

    Assert.assertTrue(TypeUtils.isAssignableFrom(int.class, short.class));
    Assert.assertTrue(TypeUtils.isAssignableFrom(Integer.class, short.class));
    Assert.assertTrue(TypeUtils.isAssignableFrom(Integer.class, Short.class));
    Assert.assertTrue(TypeUtils.isAssignableFrom(Integer.class, Byte.class));
    Assert.assertTrue(TypeUtils.isAssignableFrom(float.class, Byte.class));

    Assert.assertFalse(TypeUtils.isAssignableFrom(int.class, long.class));
    Assert.assertFalse(TypeUtils.isAssignableFrom(int.class, double.class));
    Assert.assertFalse(TypeUtils.isAssignableFrom(float.class, double.class));
    Assert.assertFalse(TypeUtils.isAssignableFrom(Float.class, double.class));

    Assert.assertTrue(TypeUtils.isAssignableFrom(Number.class, Integer.class));
    Assert.assertFalse(TypeUtils.isAssignableFrom(Integer.class, Number.class));

    Assert.assertFalse(TypeUtils.isAssignableFrom(Integer.class, String.class));
    Assert.assertTrue(TypeUtils.isAssignableFrom(Object.class, String.class));
    Assert.assertFalse(TypeUtils.isAssignableFrom(String.class, Object.class));
  }

  @Test
  public void conversionTest() {
    Assert.assertEquals(Boolean.FALSE, TypeUtils.createObjectFromString(boolean.class, "false"));
    Assert.assertEquals(Boolean.FALSE, TypeUtils.createObjectFromString(Boolean.class, "false"));
    Assert.assertEquals(Boolean.TRUE, TypeUtils.createObjectFromString(boolean.class, "true"));
    Assert.assertEquals(Boolean.TRUE, TypeUtils.createObjectFromString(Boolean.class, "true"));
    Assert.assertEquals(Boolean.FALSE, TypeUtils.createObjectFromString(Boolean.class, "xyz"));

    Assert.assertEquals(5, TypeUtils.createObjectFromString(int.class, "5"));
    Assert.assertEquals(5, TypeUtils.createObjectFromString(Integer.class, "5"));
    Assert.assertEquals(16,
        TypeUtils.createObjectFromString(Integer.class, "0x10"));
    Assert.assertEquals(5, TypeUtils.createObjectFromString(int.class, "5.1"));
    Assert.assertEquals(5, TypeUtils.createObjectFromString(Integer.class, "5.1"));

    Assert.assertEquals(5L, TypeUtils.createObjectFromString(long.class, "5.1"));
    Assert.assertEquals(5L, TypeUtils.createObjectFromString(Long.class, "5.1"));

    Assert.assertEquals(5 * 1024 * 1024,
        TypeUtils.createObjectFromString(Integer.class, " 5  MB"));
    Assert.assertEquals(2 * 24 * 60 * 60 * 1000,
        TypeUtils.createObjectFromString(Integer.class, " 2 days"));

    Assert.assertEquals(5.5, TypeUtils.createObjectFromString(double.class, "5.5"));
    Assert.assertEquals(5.5, TypeUtils.createObjectFromString(Double.class, "5.5"));
    Assert.assertNull(TypeUtils.createObjectFromString(int.class, "5aa."));
    Assert.assertNull(TypeUtils.createObjectFromString(Integer.class, "5b"));

    final UUID uuid = UUID.randomUUID();
    Assert.assertEquals(uuid, TypeUtils.createObjectFromString(UUID.class, uuid.toString()));

  }

  @Test
  public void conversionStringListTest() {
    final List<String> l = TypeUtils.createListOfType(String.class, Lists.newArrayList("a", "b"));
    Assert.assertEquals(Lists.newArrayList("a", "b"), l);
  }

  @Test
  public void conversionIntegerListTest() {
    final List<Integer> l = TypeUtils.createListOfType(Integer.class, Lists.newArrayList("1", "2"));
    Assert.assertEquals(Lists.newArrayList(1, 2), l);
  }

  @Test
  public void conversionIntegerListTestWithUnits() {
    final List<Integer> l = TypeUtils.createListOfType(Integer.class, Lists.newArrayList("1KiB", "2MiB", "3sec"));
    Assert.assertEquals(Lists.newArrayList(1 * 1024, 2 * 1024 * 1024, 3 * 1000), l);
  }

  @Test
  public void conversionDoubleListTest() {
    final List<Double> l = TypeUtils.createListOfType(Double.class, Lists.newArrayList("1.1", "2.2"));
    Assert.assertEquals(Lists.newArrayList(1.1, 2.2), l);
  }

  @Test(expected = IllegalArgumentException.class)
  public void conversionIntegerListTestWithError() {
    TypeUtils.createListOfType(Integer.class, Lists.newArrayList("a", "2"));
  }

  @Test
  public void conversionIntegerListTestWithDecimal() {
    final List<Integer> l = TypeUtils.createListOfType(Integer.class, Lists.newArrayList("1.1", "2"));
    Assert.assertEquals(Lists.newArrayList(1, 2), l);
  }

  @Test(expected = IllegalArgumentException.class)
  public void conversionLongListTestWithError() {
    TypeUtils.createListOfType(Long.class, Lists.newArrayList("a", "2"));
  }

  @Test
  public void conversionLongListTestWithDecimal() {
    final List<Long> l = TypeUtils.createListOfType(Long.class, Lists.newArrayList("1.1", "2"));
    Assert.assertEquals(Lists.newArrayList(1L, 2L), l);
  }

}
