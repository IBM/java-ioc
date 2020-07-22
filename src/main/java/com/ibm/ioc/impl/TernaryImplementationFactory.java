/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TernaryImplementationFactory<T> implements ImplementationFactory<T> {
  private static final Logger _logger = LoggerFactory.getLogger(TernaryImplementationFactory.class);

  private final BindingsProvider bindingsProvider;
  private final Class<?> requiredInterface;
  private final String propertyName;
  private final String trueRef;
  private final String falseRef;

  private DelayedEvaluator proxyEvaluator = null;

  private Map<Class<?>, String> interfaceMap;

  public TernaryImplementationFactory(
      final BindingsProvider bindingsProvider,
      final Class<?> requiredInterface,
      final String propertyName,
      final String trueRef,
      final String falseRef) {
    this.bindingsProvider = bindingsProvider;
    this.requiredInterface = requiredInterface;
    this.propertyName = propertyName;
    this.trueRef = trueRef;
    this.falseRef = falseRef;
  }

  private synchronized DelayedEvaluator getTernaryEvaluator() {
    if (this.proxyEvaluator == null) {
      final Object val = new PropertyEvaluator(this.propertyName).evaluate();
      final String referral = Boolean.valueOf(String.valueOf(val)) ? this.trueRef : this.falseRef;
      _logger.debug("Evaluated {}={}; using referral {} for {}", this.propertyName, val,
          referral, this.requiredInterface);
      this.proxyEvaluator =
          new ProxyEvaluator(this.bindingsProvider, this.requiredInterface, referral);
    }
    return this.proxyEvaluator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T initialize(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    return (T) getTernaryEvaluator().evaluate(overrides);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getImplementationClass(final Map<Class<?>, String> overrides) {
    // Since this is a proxy implementation where a def is referencing another def, we can't get
    // the actual implementation class until the referenced def is actually registered. This is
    // anyway only used for sanity checking, so returning the interface here should be good enough.
    return (Class<T>) this.requiredInterface;
  }

  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides) {
    try {
      return getTernaryEvaluator().getParameters(overrides);
    } catch (ConfigurationItemNotDefinedException | ObjectInitializationException e) {
      throw new RuntimeException(e); // indicates configuration error
    }
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    return getTernaryEvaluator().getDefaultRefs(overrides);
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    return getTernaryEvaluator().getCombinedOverrides(overrides);
  }

  @Override
  public Map<Class<?>, String> getImplementedInterfaces() {
    return this.interfaceMap;
  }

  @Override
  public void setImplementedInterfaces(final Map<Class<?>, String> interfaceMap) {
    this.interfaceMap = interfaceMap;
  }
}
