/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

// TODO: Describe class or interface
public class NamingHelperTest {

  public interface Interface {
    void setName(String name);

    String getName();

    void setType(String type);

    String getType();
  }

  public static class MiddleClass implements Interface {
    @Override
    public String getName() {
      return null;
    }

    @Override
    public void setName(final String name) {}

    @Override
    public String getType() {
      return null;
    }

    @Override
    public void setType(final String type) {}

    protected int getTime() {
      return 0;
    }

    protected void setTime(final int time) {}
  }

  public static class LowerClass extends MiddleClass {

    @Override
    public String getType() {
      return super.getType();
    }

    @Override
    public void setType(final String type) {}

    public boolean isOpen() {
      return true;
    }

    public void setOpen(final boolean open) {}

    public UUID getIdentifier() {
      return null;
    }

    public void setIdentifier(final UUID identifier) {}

  }

  @Test
  public void testConvertNameIntoJavaSetter() {
    Assert.assertEquals("setXyz", NamingHelper.convertNameIntoJavaSetter("xyz"));
    Assert.assertEquals("setXyz", NamingHelper.convertNameIntoJavaSetter("Xyz"));
    Assert.assertEquals("setBlockDevice", NamingHelper.convertNameIntoJavaSetter("block-device"));
    Assert.assertEquals("setBlockDeviceSize", NamingHelper.convertNameIntoJavaSetter("block-device-size"));
  }

  @Test
  public void testConvertNameIntoJavaMethod() {
    Assert.assertEquals("isXyz", NamingHelper.convertNameIntoJavaMethod("xyz", "is", null));
    Assert.assertEquals("isXyz3", NamingHelper.convertNameIntoJavaMethod("xyz", "is", "3"));
    Assert.assertEquals("isBlockDevice", NamingHelper.convertNameIntoJavaMethod("block-device", "is", null));
    Assert.assertEquals("isBlockDevice_under", NamingHelper.convertNameIntoJavaMethod("block-device_under", "is", null));

  }

  @Test
  public void testConvertNameIntoJavaField() {
    Assert.assertEquals("xyzAbc12A", NamingHelper.convertNameIntoJavaField("xyz-abc-12A"));
    Assert.assertEquals("XyzAbc12A", NamingHelper.convertNameIntoJavaField("Xyz-abc-12A"));
    Assert.assertEquals("xyzAbc", NamingHelper.convertNameIntoJavaField("xyzAbc"));
    Assert.assertEquals("XyzAbc", NamingHelper.convertNameIntoJavaField("-xyz-abc"));
    Assert.assertEquals("xyzAbc", NamingHelper.convertNameIntoJavaField("xyz---------------abc"));
  }

}
