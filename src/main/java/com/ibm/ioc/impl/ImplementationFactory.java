/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

public interface ImplementationFactory<T> {
  T initialize(Map<Class<?>, String> overrides)
      throws ObjectInstantiationException, ObjectInitializationException;

  default T initialize()
      throws ObjectInstantiationException, ObjectInitializationException {
    return initialize(Collections.emptyMap());
  }

  Class<T> getImplementationClass(Map<Class<?>, String> overrides);

  default Class<T> getImplementationClass() {
    return getImplementationClass(Collections.emptyMap());
  }

  List<NamedEvaluator> getParameters(Map<Class<?>, String> overrides);

  default List<NamedEvaluator> getParameters() {
    return getParameters(Collections.emptyMap());
  }

  Set<Class<?>> getDefaultRefs(Map<Class<?>, String> overrides);

  Map<Class<?>, String> getCombinedOverrides(Map<Class<?>, String> overrides);

  Map<Class<?>, String> getImplementedInterfaces();

  void setImplementedInterfaces(Map<Class<?>, String> interfaceMap);
}
