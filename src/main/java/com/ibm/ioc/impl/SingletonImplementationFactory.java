/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

public class SingletonImplementationFactory<T> implements ImplementationFactory<T> {
  private final Map<Map<Class<?>, String>, T> instanceMap = new HashMap<>();
  private final ImplementationFactory<T> factory;
  private final Map<Class<?>, String> implOverrides;

  public SingletonImplementationFactory(final ImplementationFactory<T> factory,
      final Map<Class<?>, String> implOverrides) {
    this.factory = factory;
    this.implOverrides = Collections.unmodifiableMap(implOverrides);
  }

  @Override
  public Class<T> getImplementationClass(final Map<Class<?>, String> overrides) {
    return this.factory.getImplementationClass(getCombinedOverrides(overrides));
  }

  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides) {
    return this.factory.getParameters(getCombinedOverrides(overrides));
  }

  @Override
  public final T initialize(final Map<Class<?>, String> overrides)
      throws ObjectInstantiationException, ObjectInitializationException {
    final Map<Class<?>, String> combinedOverrides = getCombinedOverrides(overrides);

    final Map<Class<?>, String> requiredOverrides = new HashMap<>();
    if (!combinedOverrides.isEmpty()) { // bypass traversing entire tree for most cases
      final Set<Class<?>> set = this.factory.getDefaultRefs(combinedOverrides);
      set.forEach(cls -> {
        if (combinedOverrides.containsKey(cls)) {
          requiredOverrides.put(cls, combinedOverrides.get(cls));
        }
      });
    }
    if (!this.instanceMap.containsKey(requiredOverrides)) {
      synchronized (this) {
        if (!this.instanceMap.containsKey(requiredOverrides)) {
          this.instanceMap.put(requiredOverrides, this.factory.initialize(combinedOverrides));
        }
      }
    }

    return this.instanceMap.get(requiredOverrides);
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Map<Class<?>, String> combinedOverrides = getCombinedOverrides(overrides);

    final Set<Class<?>> set = new HashSet<>(this.factory.getDefaultRefs(combinedOverrides));
    final Iterator<Class<?>> it = set.iterator();
    // remove from set of defaults to be replaced by higher layers if it's replaced at this layer
    while (it.hasNext()) {
      if (combinedOverrides.containsKey(it.next())) {
        it.remove();
      }
    }
    return set;
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    final Map<Class<?>, String> combinedOverrides = new HashMap<>(overrides);
    combinedOverrides.putAll(this.implOverrides);
    return combinedOverrides;
  }

  @Override
  public String toString() {
    return this.factory.toString();
  }

  @Override
  public Map<Class<?>, String> getImplementedInterfaces() {
    return this.factory.getImplementedInterfaces();
  }

  @Override
  public void setImplementedInterfaces(final Map<Class<?>, String> interfaceMap) {
    this.factory.setImplementedInterfaces(interfaceMap);
  }
}
