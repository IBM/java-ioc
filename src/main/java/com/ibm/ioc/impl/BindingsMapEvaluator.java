/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;

public class BindingsMapEvaluator<T> implements Evaluatable {
  private final Class<T> interfaceClass;
  private final BindingsProvider bindingsProvider;

  public BindingsMapEvaluator(final Class<T> interfaceClass,
      final BindingsProvider bindingsProvider) {
    this.interfaceClass = interfaceClass;
    this.bindingsProvider = bindingsProvider;
  }

  @Override
  public BindingsMap<T> evaluate(final Map<Class<?>, String> overrides) {
    return new BindingsMap<T>(this.interfaceClass, this.bindingsProvider, overrides);
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Set<Class<?>> set = new HashSet<>();

    try {
      final Map<String, ImplementationFactory<? extends T>> factories =
          this.bindingsProvider.getImplementations(this.interfaceClass);
      factories.values().forEach(factory -> set.addAll(factory.getDefaultRefs(overrides)));
    } catch (final ConfigurationItemNotDefinedException e) {
      throw new RuntimeException(e);
    }
    return set;
  }
}
