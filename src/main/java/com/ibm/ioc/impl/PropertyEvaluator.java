/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Map;

import com.ibm.ioc.PropertiesResolverFactory;

public class PropertyEvaluator implements Evaluatable {
  private final String propertyName;

  public PropertyEvaluator(final String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public Modifiable<?> evaluate(final Map<Class<?>, String> overrides) {
    return evaluate();
  }


  @Override
  public Modifiable<?> evaluate() {
    final OrderedPropertiesResolver resolver = PropertiesResolverFactory.getInstance();

    return resolver.resolveModifiable(this.propertyName);
  }
}
