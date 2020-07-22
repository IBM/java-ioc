/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.TypeUtils;

// TODO: Describe class or interface
public abstract class BindingsProviderBase implements BindingsProvider {
  private static final Logger _logger = LoggerFactory.getLogger(BindingsProviderBase.class);

  private final Map<Class<?>, InterfaceBinding> interfaceBindingsMap =
      new HashMap<Class<?>, InterfaceBinding>();

  private final Map<Class<?>, String> interfaceOverrides = new HashMap<Class<?>, String>();

  @Override
  @SuppressWarnings("unchecked")
  public <T> ImplementationFactory<T> getDefaultImplementation(final Class<T> interfaceClass)
      throws ConfigurationItemNotDefinedException {
    assert interfaceClass != null;

    final InterfaceBinding binding = this.interfaceBindingsMap.get(interfaceClass);
    if (binding == null) {
      throw new ConfigurationItemNotDefinedException("No binding defined for interface "
          + interfaceClass.getCanonicalName());
    }

    // If there's an override present, return referral implementation
    String override = findOverride(interfaceClass.getName());

    if (override == null || override.isEmpty()) {
      // Replace $ to . to support inner classes and maintain Java notation
      final String fixedInterfaceClassName = interfaceClass.getName().replace('$', '.');
      if (!fixedInterfaceClassName.equals(interfaceClass.getName())) {
        override = findOverride(fixedInterfaceClassName);
      }
    }

    if (override != null && !override.isEmpty()) {
      if (!override.equals(this.interfaceOverrides.get(interfaceClass))) {
        // only report overriding the default implementation once
        this.interfaceOverrides.put(interfaceClass, override);
        _logger.warn(
            "Overriding a default implementation for interface {} with the referral {}",
            interfaceClass.getName(), override);
      }
      return getImplementation(interfaceClass, override);
    } else {
      // defaultest default
      return (ImplementationFactory<T>) binding.getDefaultImplementationFactory();
    }
  }

  @SuppressWarnings("unchecked")
  private String findOverride(final String propertyName) {
    final Modifiable<?> defaultReferral = new PropertyEvaluator(propertyName).evaluate();
    return defaultReferral == null ? null : ((Modifiable<String>) defaultReferral).get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ImplementationFactory<T> getImplementation(
      final Class<T> interfaceClass,
      final String referral) throws ConfigurationItemNotDefinedException {
    assert interfaceClass != null;

    final InterfaceBinding binding = this.interfaceBindingsMap.get(interfaceClass);
    if (binding == null) {
      throw new ConfigurationItemNotDefinedException("No binding defined for interface "
          + interfaceClass.getCanonicalName());
    }

    return (ImplementationFactory<T>) binding.getImplementationFactory(referral);
  }

  @Override
  public <T> Map<String, ImplementationFactory<? extends T>> getImplementations(
      final Class<T> interfaceClass) throws ConfigurationItemNotDefinedException {
    final InterfaceBinding binding = this.interfaceBindingsMap.get(interfaceClass);
    if (binding == null) {
      throw new ConfigurationItemNotDefinedException("No binding defined for interface "
          + interfaceClass.getName());
    }
    return binding.getImplementationFactoryMap();
  }

  private void tryInitialize(
      final ImplementationFactory<?> factory,
      final Map<Class<?>, List<String>> unresolvedBindings) {
    if (factory == null) {
      return;
    }
    try {
      factory.initialize();
    } catch (final Exception e) {
      _logger.debug("Problem initializing " + factory, e);
      List<String> errorEntries = unresolvedBindings.get(factory.getClass());
      if (errorEntries == null) {
        errorEntries = new ArrayList<String>();
        unresolvedBindings.put(factory.getClass(), errorEntries);
      }
      errorEntries.add(e.getMessage());
    }
  }

  @Override
  public Map<Class<?>, List<String>> selfTest() {
    System.setProperty(TypeUtils.DISALLOW_INVALID_VALUES_TEST_PROPERTY, Boolean.TRUE.toString());
    final Map<Class<?>, List<String>> unresolvedBindings = new HashMap<>();
    try {
      for (final InterfaceBinding kb : this.interfaceBindingsMap.values()) {
        for (final String referral : kb.getImplementationReferralSet()) {
          tryInitialize(kb.getImplementationFactory(referral), unresolvedBindings);
        }
        tryInitialize(kb.getDefaultImplementationFactory(), unresolvedBindings);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Should not be here", e);
    } finally {
      System.clearProperty(TypeUtils.DISALLOW_INVALID_VALUES_TEST_PROPERTY);
    }

    return unresolvedBindings;
  }

  protected InterfaceBinding getOrCreateInterfaceBinding(final Class<?> interfaceClass) {
    InterfaceBinding binding = this.interfaceBindingsMap.get(interfaceClass);
    if (binding == null) {
      binding = new InterfaceBinding(interfaceClass);
      this.interfaceBindingsMap.put(interfaceClass, binding);
    }
    return binding;
  }

  public Map<Class<?>, InterfaceBinding> getInterfaceBindingsMap() {
    return new HashMap<>(this.interfaceBindingsMap);
  }
}
