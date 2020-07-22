/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class BuilderImplementationFactoryTest {
  public interface MyObjectInterface {
    int getMyIntParam();

    String getMyStringParam();
  }

  public static class MyObject implements MyObjectInterface {
    private final int myIntParam;
    private final String myStringParam;
    private final boolean myAddedBooleanParam;
    private String setterParamWithoutConstructorArg;
    private String setterParamWithConstructorArg;

    public MyObject(
        final int myIntParam,
        final String myStringParam,
        final boolean myAddedBooleanParam,
        final String setterParamWithConstructorArg) {
      this.myIntParam = myIntParam;
      this.myStringParam = myStringParam;
      this.myAddedBooleanParam = myAddedBooleanParam;
      this.setterParamWithConstructorArg = setterParamWithConstructorArg;
    }

    @Override
    public int getMyIntParam() {
      return this.myIntParam;
    }

    @Override
    public String getMyStringParam() {
      return this.myStringParam;
    }

    public boolean getMyAddedBooleanParam() {
      return this.myAddedBooleanParam;
    }

    public String getSetterParamWithoutConstructorArg() {
      return this.setterParamWithoutConstructorArg;
    }

    public String getSetterParamWithConstructorArg() {
      return this.setterParamWithConstructorArg;
    }

    public void setSetterParamWithoutConstructorArg(final String setterParamWithoutConstructorArg) {
      this.setterParamWithoutConstructorArg = setterParamWithoutConstructorArg;
    }

    public void setSetterParamWithConstructorArg(final String setterParamWithConstructorArg) {
      this.setterParamWithConstructorArg = setterParamWithConstructorArg;
    }

  }

  public static class MyObjectBuilder {
    public int myIntParam;
    public String myStringParam;

    public boolean myAddedBooleanParam = false;
    public String setterParamWithConstructorArg;

    public MyObject build() {
      return new MyObject(this.myIntParam, this.myStringParam, this.myAddedBooleanParam,
          this.setterParamWithConstructorArg);
    }

  }

  private List<NamedEvaluator> createParamList() {
    final ArrayList<NamedEvaluator> params = new ArrayList<NamedEvaluator>(2);

    params.add(new NamedEvaluator("my-int-param", false, new IntegerLiteral(1024)));
    params.add(new NamedEvaluator("my-string-param", false, new StringLiteral("String value")));
    params.add(new NamedEvaluator("setter-param-with-constructor-arg", true,
        new StringLiteral("constructor")));
    params.add(new NamedEvaluator("setter-param-without-constructor-arg", true,
        new StringLiteral("no constructor")));
    return params;
  }

  private void runAssertChecks(final BuilderImplementationFactory<MyObject> factory)
      throws Exception {
    final Object builder = factory.getBuilder(Collections.emptyMap());

    Assert.assertTrue(builder instanceof MyObjectBuilder);

    MyObject myObj = ((MyObjectBuilder) builder).build();
    Assert.assertEquals(1024, myObj.getMyIntParam());
    Assert.assertEquals("String value", myObj.getMyStringParam());
    Assert.assertFalse(myObj.getMyAddedBooleanParam());
    Assert.assertEquals("constructor", myObj.getSetterParamWithConstructorArg() );
    Assert.assertEquals("no constructor", myObj.getSetterParamWithoutConstructorArg());
    Assert.assertFalse(myObj.getMyAddedBooleanParam());

    Assert.assertEquals(factory.getImplementationClass(), MyObject.class);
    Assert.assertEquals(factory.getBuilderClass(Collections.emptyMap()), MyObjectBuilder.class);

    List<NamedEvaluator> params = factory.getParameters();
    Assert.assertEquals(4, params.size());
    Assert.assertEquals(1024, params.get(0).evaluate());
    Assert.assertEquals("String value", params.get(1).evaluate() );
    Assert.assertEquals("constructor", params.get(2).evaluate());
    Assert.assertEquals("no constructor", params.get(3).evaluate());

    factory.addParameter("my-added-boolean-param", false, new BooleanLiteral(true));
    myObj = factory.initialize();
    Assert.assertTrue(myObj.getMyAddedBooleanParam());

    params = factory.getParameters();
    Assert.assertEquals(5, params.size());
    Assert.assertEquals(1024, params.get(0).evaluate());
    Assert.assertEquals("String value", params.get(1).evaluate());
    Assert.assertEquals("constructor", params.get(2).evaluate());
    Assert.assertEquals("no constructor", params.get(3).evaluate());
    Assert.assertTrue((boolean) params.get(4).evaluate());
  }

  @Ignore
  @Test
  public void testBuilderImplementationFactoryBuilderClass() throws Exception {
    final BuilderImplementationFactory<MyObject> fact =
        new BuilderImplementationFactory<MyObject>(MyObjectBuilder.class, createParamList());

    runAssertChecks(fact);
  }
}
