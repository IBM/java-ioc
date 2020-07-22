/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.ibm.ioc.MethodInvocationException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.TypeUtils;

// TODO: Describe class or interface
abstract class JavaMethodEvaluatorImpl {
  public final int MAX_ARGS_SUPPORTED = 16;

  private final String methodName;
  private final List<Evaluatable> parameters;

  /**
   * @param method
   * @param parameters
   */
  public JavaMethodEvaluatorImpl(final String method, final List<Evaluatable> parameters) {
    super();
    assert method != null && !"".equals(method);

    if (parameters.size() > this.MAX_ARGS_SUPPORTED) {
      throw new RuntimeException("Too many arguments " + parameters.size() + ". Max supported:"
          + this.MAX_ARGS_SUPPORTED);
    }

    this.methodName = method;
    this.parameters = parameters;
  }

  Object evaluateStatic(final Class<?> clazz, final Map<Class<?>, String> overrides)
      throws MethodInvocationException,
      ObjectInitializationException {
    return evaluateImpl(clazz, overrides);
  }

  Object evaluateInstance(final Object instance, final Map<Class<?>, String> overrides)
      throws MethodInvocationException,
      ObjectInitializationException {
    return evaluateImpl(instance, overrides);
  }

  private Object evaluateImpl(final Object instance, final Map<Class<?>, String> overrides)
      throws MethodInvocationException,
      ObjectInitializationException {
    final Object[] paramsValueOptions = new Object[this.parameters.size()];

    int i = 0;
    for (final Evaluatable param : this.parameters) {
      paramsValueOptions[i] = param.evaluate(overrides);
      i++;
    }

    // Try all possible permutations of array and singular values
    final int totalPermutations = (int) Math.pow(2, this.parameters.size());
    final Class<?>[] paramTypes = new Class[this.parameters.size()];
    final Object[] paramValues = new Object[this.parameters.size()];

    for (int permutation = 0; permutation < totalPermutations; permutation++) {
      // Trying all non null variations of passed parameters
      int index = 0;
      for (final Object paramEvaluation : paramsValueOptions) {
        paramValues[index] = paramEvaluation;
        if (paramValues[index] == null) {
          // This permutation can't be used
          break;
        }
        paramTypes[index] = paramValues[index].getClass();
        index++;
      }
      if (index != this.parameters.size()) {
        continue;
      }

      String className = "";
      Method m = null;
      try {
        if (instance instanceof Class<?>) { // Static method
          className = ((Class<?>) instance).getName();
          m =
              TypeUtils.getMatchingMethod((Class<?>) instance, this.methodName, paramTypes,
                  true);
          if (m != null) {
            return m.invoke(null, paramValues);
          }
        } else {
          className = instance.getClass().getName();
          m =
              TypeUtils.getMatchingMethod(instance.getClass(), this.methodName, paramTypes,
                  false);
          if (m != null) {
            return m.invoke(instance, paramValues);
          }
        }
      } catch (final Exception e) {
        throw new MethodInvocationException("Error invoking " + this.methodName + "("
            + TypeUtils.listOfClassesToStr(paramTypes) + ") for object of type " + className,
            e);
      }
    }
    throw new MethodInvocationException("Can't find method match:" + this.methodName + "()");
  }

  @Override
  public String toString() {
    // private final String methodName;
    // private final List<Evaluatable> parameters;
    final StringBuilder b = new StringBuilder();
    b.append("method " + this.methodName + "(");
    boolean first = true;
    for (final Evaluatable param : this.parameters) {
      if (!first) {
        b.append(",");
      }
      b.append(param.toString());
      if (first) {
        first = false;
      }
    }
    b.append(")");
    return b.toString();
  }
}
