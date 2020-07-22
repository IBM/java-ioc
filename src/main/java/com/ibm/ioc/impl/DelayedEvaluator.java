/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.List;
import java.util.Map;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;

public interface DelayedEvaluator extends Evaluatable {
  List<NamedEvaluator> getParameters(Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;

  String getReferral();

  Class<?> getRequiredInterface();

  Class<?> getImplementationClass(Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException;

  Map<Class<?>, String> getCombinedOverrides(Map<Class<?>, String> overrides);

  Map<Class<?>, String> getImplementedInterfaces(Map<Class<?>, String> overrides);
}
