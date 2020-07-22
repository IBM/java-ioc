/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefPropEvaluator implements DelayedEvaluator {
  private static final Logger _logger = LoggerFactory.getLogger(RefPropEvaluator.class);

  private final BindingsProvider bindingsProvider;
  private final Class<?> requiredInterface;
  private final String referralPropertyName;
  private final AtomicReference<ProxyEvaluator> proxyEvaluator = new AtomicReference<>();

  public RefPropEvaluator(
      final BindingsProvider bindingsProvider,
      final Class<?> requiredInterface,
      final String referralPropertyName) {
    this.bindingsProvider = bindingsProvider;
    this.requiredInterface = requiredInterface;
    this.referralPropertyName = referralPropertyName;
  }

  private ProxyEvaluator createProxyEvaluator() {
    final String referralName = String.valueOf(new PropertyEvaluator(this.referralPropertyName).evaluate());
    _logger.info("Using property {}={} to construct {}", this.referralPropertyName, referralName,
        this.requiredInterface);
    return new ProxyEvaluator(this.bindingsProvider, this.requiredInterface, referralName);
  }

  private void ensureInitialized() {
    if (this.proxyEvaluator.get() == null) {
      this.proxyEvaluator.compareAndSet(null, createProxyEvaluator());
    }
  }

  @Override
  public Object evaluate(final Map<Class<?>, String> overrides) throws ObjectInitializationException {
    ensureInitialized();
    return this.proxyEvaluator.get().evaluate(overrides);
  }

  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException {
    ensureInitialized();
    return this.proxyEvaluator.get().getParameters(overrides);
  }

  @Override
  public String getReferral() {
    ensureInitialized();
    return this.proxyEvaluator.get().getReferral();
  }

  @Override
  public Class<?> getRequiredInterface() {
    ensureInitialized();
    return this.proxyEvaluator.get().getRequiredInterface();
  }

  @Override
  public Class<?> getImplementationClass(final Map<Class<?>, String> overrides)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException {
    ensureInitialized();
    return this.proxyEvaluator.get().getImplementationClass(overrides);
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    ensureInitialized();
    return this.proxyEvaluator.get().getDefaultRefs(overrides);
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    ensureInitialized();
    return this.proxyEvaluator.get().getCombinedOverrides(overrides);
  }

  @Override
  public Map<Class<?>, String> getImplementedInterfaces(final Map<Class<?>, String> overrides) {
    ensureInitialized();
    return this.proxyEvaluator.get().getImplementedInterfaces(overrides);
  }

  @Override
  public String toString() {
    return "RefPropEvaluator [interface=" + this.requiredInterface + ", refprop=" + this.referralPropertyName + "]";
  }
}
