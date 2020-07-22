/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

// TODO: Describe class or interface
public final class ImplementationFactoryImpl<T> implements ImplementationFactory<T> {
  private final Class<T> implementationClass;
  private final List<NamedEvaluator> parameters;
  private Map<Class<?>, String> interfaceMap;

  public ImplementationFactoryImpl(
      final Class<T> implementationClass,
      final List<NamedEvaluator> parameters) {
    this.implementationClass = implementationClass;
    this.parameters = parameters;
  }

  // for use by clone()
  protected ImplementationFactoryImpl(final Class<T> implementationClass) {
    this.implementationClass = implementationClass;
    this.parameters = new ArrayList<NamedEvaluator>();
  }

  @Override
  public T initialize(final Map<Class<?>, String> overrides)
      throws ObjectInstantiationException, ObjectInitializationException {
    final T classInstance;

    try {
      classInstance = this.implementationClass.getDeclaredConstructor().newInstance();
    } catch (final Exception e) {
      throw new ObjectInstantiationException("Can't instatiate an object of type '"
          + this.implementationClass.getName() + "'", e);
    }

    final ReferenceEvaluator<T> evaluator =
        new ReferenceEvaluator<T>(classInstance, this.parameters);
    return evaluator.evaluate(overrides);
  }

  /**
   * @return the implementationClass
   */
  @Override
  public Class<T> getImplementationClass(final Map<Class<?>, String> overrides) {
    return this.implementationClass;
  }

  /**
   * Returns the list of parameters.
   * 
   * @return
   */
  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides) {
    return this.parameters;
  }

  /**
   * Add a named parameter.
   * 
   * @param name
   * @param value
   */
  void addParameter(final String name, final boolean required, final Evaluatable value) {
    this.parameters.add(new NamedEvaluator(name, required, value));
  }

  @Override
  public String toString() {
    return this.implementationClass.toString();
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Set<Class<?>> set = new HashSet<>();
    for (final NamedEvaluator param : this.parameters) {
      set.addAll(param.getDefaultRefs(overrides));
    }
    return set;
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    return Collections.emptyMap();
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
