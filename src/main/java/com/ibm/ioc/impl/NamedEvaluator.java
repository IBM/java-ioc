/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Map;
import java.util.Set;

import com.ibm.ioc.ObjectInitializationException;

/**
 * Wrapper for an Evaluatable parameter that allows for a parameter name.
 * 
 * @author Manish Motwani
 */
public class NamedEvaluator implements Evaluatable {
  private final String name;
  private final Evaluatable value;
  private final boolean required;

  /**
   * @param name
   * @param value
   */
  public NamedEvaluator(final String name, final boolean required, final Evaluatable value) {
    this.name = name;
    this.value = value;
    this.required = required;
  }

  /**
   * @return
   */
  public String getName() {
    return this.name;
  }

  /**
   * if required and not set, object that uses this parameter would fail at creation
   * 
   * @return
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * @return
   */
  public Evaluatable getValue() {
    return this.value;
  }


  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    return this.value.getDefaultRefs(overrides);
  }

  // TODO: support lists
  @Override
  public Object evaluate(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    return this.value.evaluate(overrides);
  }

  @Override
  public String toString() {
    return this.name + (isRequired() ? "[required]" : "") + "--> " + this.value.toString();
  }
}
