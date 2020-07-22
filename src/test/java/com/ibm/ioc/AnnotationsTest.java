/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Arrays;
import java.util.Map;

import com.ibm.ioc.Annotations.RequireParameterBinding;
import com.ibm.ioc.impl.Evaluatable;
import com.ibm.ioc.impl.NamedEvaluator;
import com.ibm.ioc.impl.ReferenceEvaluator;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationsTest {
  public static class MyObject {
    private int int1;
    private int int2;

    public MyObject() {}

    public int getInt1() {
      return this.int1;
    }

    public void setInt1(final int int1) {
      this.int1 = int1;
    }

    public int getInt2() {
      return this.int2;
    }

    @RequireParameterBinding
    public void setInt2(final int int2) {
      this.int2 = int2;
    }
  }

  @Test
  public void createWithMissingInt1() throws ObjectInitializationException {
    final MyObject ma = new MyObject();
    final Integer arg = 3;
    final ReferenceEvaluator<MyObject> re =
        new ReferenceEvaluator<MyObject>(ma,
            Arrays.asList(new NamedEvaluator("int2", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return arg;
                  }
                })));
    re.evaluate();
    Assert.assertEquals(arg, (Integer) ma.getInt2());
  }

  // this one is expected to fail because of the annotation on int2 setter
  @Test(expected = ObjectInitializationException.class)
  public void createWithMissingInt2() throws ObjectInitializationException {
    final MyObject ma = new MyObject();
    final Integer arg = 3;
    final ReferenceEvaluator<MyObject> re =
        new ReferenceEvaluator<MyObject>(ma,
            Arrays.asList(new NamedEvaluator("int1", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return arg;
                  }
                })));
    re.evaluate();
    Assert.assertEquals(arg, (Integer) ma.getInt1());
  }

  @Test
  public void createWithBothInts() throws ObjectInitializationException {
    final MyObject ma = new MyObject();
    final Integer arg1 = 3;
    final Integer arg2 = 6;
    final ReferenceEvaluator<MyObject> re =
        new ReferenceEvaluator<MyObject>(ma,
            Arrays.asList(new NamedEvaluator("int1", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return arg1;
                  }
                }), new NamedEvaluator("int2", true,
                    new Evaluatable() {
                      @Override
                      public Object evaluate(final Map<Class<?>, String> overrides) {
                        return arg2;
                      }
                    })));
    re.evaluate();
    Assert.assertEquals(arg1, (Integer) ma.getInt1());
    Assert.assertEquals(arg2, (Integer) ma.getInt2());
  }
}


