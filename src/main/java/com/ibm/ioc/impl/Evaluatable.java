/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.ObjectInitializationException;

// TODO: Describe class or interface
public interface Evaluatable {
  Object evaluate(Map<Class<?>, String> overrides) throws ObjectInitializationException;

  default Object evaluate() throws ObjectInitializationException {
    return evaluate(Collections.emptyMap());
  }

  default Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    return Collections.emptySet();
  }
}
