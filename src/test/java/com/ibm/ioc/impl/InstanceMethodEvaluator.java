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
public class InstanceMethodEvaluator extends JavaMethodEvaluatorImpl implements Evaluatable {
  private final Object instance;

  /**
   * @param instance
   * @param method
   * @param parameters
   */
  public InstanceMethodEvaluator(
      final Object instance,
      final String method,
      final List<Evaluatable> parameters) {
    super(method, parameters);

    assert instance != null;
    this.instance = instance;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.cleversafe.config.evaluator.Evaluatable#evaluate( List<String> contexts)
   */
  @Override
  public Object evaluate(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    try {
      return evaluateInstance(this.instance, overrides);
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
    return "Instance method evaluator : " + this.instance.getClass().toString() + " "
        + super.toString();
  }
}
