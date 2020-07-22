/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Type;
import java.util.List;

public class ExplicitReferenceEvaluator<T> extends ReferenceEvaluator<T> {
  private final Class<?> clazz;

  public ExplicitReferenceEvaluator(
      final Class<?> clazz,
      final T referenced,
      final List<NamedEvaluator> params) {
    super(referenced, params);
    this.clazz = clazz;
  }

  public ExplicitReferenceEvaluator(final Class<?> clazz, final T type) {
    super(type, null);
    this.clazz = clazz;
  }

  @Override
  protected Type getDefaultType() {
    return this.clazz;
  }
}
