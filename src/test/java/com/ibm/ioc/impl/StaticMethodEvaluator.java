/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.List;
import java.util.Map;

import com.ibm.ioc.MethodInvocationException;
import com.ibm.ioc.ObjectInitializationException;

// TODO: Describe class or interface
public class StaticMethodEvaluator extends JavaMethodEvaluatorImpl implements Evaluatable {
  private final Class<?> clazz;

  /**
   * Defines a static method of the specified class
   * 
   * @param clazz Class upon which a method should be invoked
   * @param method Method name to be invoked
   * @param parameters List of parameters to be passed to a method
   */
  public StaticMethodEvaluator(
      final Class<?> clazz,
      final String method,
      final List<Evaluatable> parameters) {
    super(method, parameters);

    assert (clazz != null);
    this.clazz = clazz;
  }

  /**
   * Evaluates a value by invoking a static on a class
   */
  @Override
  public Object evaluate(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    try {
      return evaluateStatic(this.clazz, overrides);
    } catch (final MethodInvocationException e) {
      throw new ObjectInitializationException("Exception evaluating " + toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Static method evaluator for class: " + this.clazz.toString() + " " + super.toString();
  }
}
