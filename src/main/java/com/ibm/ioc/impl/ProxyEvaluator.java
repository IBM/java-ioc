/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

public class ProxyEvaluator implements DelayedEvaluator {
  private final BindingsProvider bindingsProvider;
  private final Class<?> requiredInterface;
  private final String referral;

  public ProxyEvaluator(
      final BindingsProvider bindingsProvider,
      final Class<?> requiredInterface,
      final String referral) {
    this.bindingsProvider = bindingsProvider;
    this.requiredInterface = requiredInterface;
    this.referral = referral;
  }

  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return getImplementationFactory(overrides).getParameters(overrides);
  }

  @Override
  public Object evaluate(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    return evaluateImpl(overrides);
  }

  @Override
  public String getReferral() {
    return this.referral;
  }

  @Override
  public Class<?> getRequiredInterface() {
    return this.requiredInterface;
  }

  @Override
  public Class<?> getImplementationClass(final Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException {
    return getImplementationFactory(overrides).getImplementationClass();
  }

  private Object evaluateImpl(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    try {
      return getImplementationFactory(overrides).initialize(overrides);
    } catch (final ObjectInstantiationException e) {
      throw new ObjectInitializationException("Invalid binding reference: " + toString(), e);
    } catch (final ConfigurationItemNotDefinedException e) {
      throw new ObjectInitializationException("Invalid binding reference: " + toString(), e);
    }
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Set<Class<?>> set = new HashSet<>();
    if (this.referral == null) {
      set.add(this.requiredInterface);
    }
    try {
      set.addAll(getImplementationFactory(overrides).getDefaultRefs(overrides));
    } catch (ConfigurationItemNotDefinedException | ObjectInitializationException ignore) {
      // this will be thrown again later when we're actually initializing
    }
    return set;
  }

  private ImplementationFactory<?> getImplementationFactory(final Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException {
    final ImplementationFactory<?> implementationFactory;
    if (this.referral != null) {
      implementationFactory =
          this.bindingsProvider.getImplementation(this.requiredInterface, this.referral);
    } else {
      if (overrides.containsKey(this.requiredInterface)) {
        implementationFactory =
            this.bindingsProvider.getImplementation(this.requiredInterface,
                overrides.get(this.requiredInterface));
      } else {
        implementationFactory =
            this.bindingsProvider.getDefaultImplementation(this.requiredInterface);
        if (implementationFactory == null) {
          throw new ObjectInitializationException("No default implementation for "
              + this.requiredInterface);
        }
      }
    }
    return implementationFactory;
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    try {
      return getImplementationFactory(overrides).getCombinedOverrides(overrides);
    } catch (ConfigurationItemNotDefinedException | ObjectInitializationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<Class<?>, String> getImplementedInterfaces(final Map<Class<?>, String> overrides) {
    try {
      return getImplementationFactory(overrides).getImplementedInterfaces();
    } catch (ConfigurationItemNotDefinedException | ObjectInitializationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("interface=").append(this.requiredInterface.getName());

    if (this.referral != null) {
      sb.append(", referral=").append(this.referral);
    }
    return sb.toString();
  }
}
