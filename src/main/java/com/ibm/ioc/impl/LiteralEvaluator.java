/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Map;

// Base class for literal evaluation
public class LiteralEvaluator implements Evaluatable {
  private final Object value;

  /**
   * @param value
   */
  public LiteralEvaluator(final Object value) {
    super();
    this.value = value;
  }

  @Override
  public Object evaluate(final Map<Class<?>, String> overrides) {
    return this.value;
  }
}
