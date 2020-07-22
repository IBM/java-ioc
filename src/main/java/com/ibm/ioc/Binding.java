/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.impl.ConfigurationEvents;
import com.ibm.ioc.impl.ImplementationFactory;

// TODO: Describe class or interface
public abstract class Binding {
  private static final Logger _logger = LoggerFactory.getLogger(Binding.class);

  // Maps referral to an implementation
  private final Map<String, ImplementationFactory<?>> referralImplementations = new HashMap<>();

  private ImplementationFactory<?> defaultImplementation = null;

  public void setDefaultImplementationFactory(final ImplementationFactory<?> implementationFactory)
      throws IllegalConfigurationContentException {
    if (this.defaultImplementation != null) {
      _logger.error(ConfigurationEvents.MULTIPLE_DEFAULT_IMPLEMENTATIONS,
          getQualifiedBindingName(), this.defaultImplementation + ", " + implementationFactory);
      throw new IllegalConfigurationContentException(
          "Multiple default implementations defined for binding: " + getQualifiedBindingName()
              + ": " + this.defaultImplementation + ", " + implementationFactory);
    }

    checkRequiredInterface(implementationFactory);
    this.defaultImplementation = implementationFactory;
  }

  public ImplementationFactory<?> getDefaultImplementationFactory() {
    return this.defaultImplementation;
  }

  public void addImplementationFactory(
      final String referral,
      final ImplementationFactory<?> implementationFactory)
      throws IllegalConfigurationContentException {
    checkRequiredInterface(implementationFactory);

    if (this.referralImplementations.containsKey(referral)) {
      final String errorMsg = "An implementation for binding "
          + getQualifiedBindingName() + " with referral " + referral + " already exists.";
      _logger.error(errorMsg);
      throw new IllegalConfigurationContentException(errorMsg);
    }
    this.referralImplementations.put(referral, implementationFactory);
  }

  /**
   * Implementation factory for a given referral
   * 
   * @param referral
   * @return
   * @throws ConfigurationItemNotDefinedException
   */
  public ImplementationFactory<?> getImplementationFactory(final String referral)
      throws ConfigurationItemNotDefinedException {
    final ImplementationFactory<?> implementationFactory =
        this.referralImplementations.get(referral);
    if (implementationFactory == null) {
      _logger.error(ConfigurationEvents.NO_IMPLEMENTATION_FOR_REFERRAL,
          getQualifiedBindingName(), referral);
      throw new ConfigurationItemNotDefinedException(String.format(
          "No implementation defined for binding %s and referral %s",
          getQualifiedBindingName(), referral));
    }
    return implementationFactory;
  }

  @SuppressWarnings("unchecked")
  public <T> Set<ImplementationFactory<T>> getImplementationFactorySet(final Class<T> cls) {
    final Set<ImplementationFactory<T>> factories = new HashSet<ImplementationFactory<T>>();
    for (final ImplementationFactory<?> factory : this.referralImplementations.values()) {
      factories.add((ImplementationFactory<T>) factory);
    }
    return factories;
  }

  @SuppressWarnings("unchecked")
  public <T> Map<String, ImplementationFactory<? extends T>> getImplementationFactoryMap() {
    final Map<String, ImplementationFactory<? extends T>> factories = new HashMap<>();
    this.referralImplementations.forEach((referral, factory) -> factories.put(referral,
        (ImplementationFactory<T>) factory));
    return factories;
  }

  public Set<Class<?>> getImplementationClassSet() {
    final Set<Class<?>> classSet = new HashSet<Class<?>>();
    this.referralImplementations
        .forEach((referral, factory) -> classSet.add(factory.getImplementationClass()));

    if (this.defaultImplementation != null
        && !classSet.contains(this.defaultImplementation.getImplementationClass())) {
      classSet.add(this.defaultImplementation.getImplementationClass());
    }
    return classSet;
  }

  public Set<String> getImplementationReferralSet() {
    return this.referralImplementations.keySet();
  }

  @Override
  public String toString() {
    return getImplementationReferralSet().toString();
  }

  public abstract List<String> getQualifiedBindingName();

  protected abstract void checkRequiredInterface(
      final ImplementationFactory<?> implementationFactory)
      throws IllegalConfigurationContentException;

}
