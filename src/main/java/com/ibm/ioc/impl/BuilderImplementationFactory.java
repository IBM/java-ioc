/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

public final class BuilderImplementationFactory<T> implements ImplementationFactory<T> {
  private static final Logger _logger =
      LoggerFactory.getLogger(BuilderImplementationFactory.class);

  private ImplementationFactoryImpl<?> builderImplementationFactory = null;
  private final List<NamedEvaluator> nonBuilderParameters;
  private final List<NamedEvaluator> builderParameters;

  public <U> BuilderImplementationFactory(
      final Class<U> builderClass,
      final List<NamedEvaluator> parameters) {
    this.nonBuilderParameters = new ArrayList<NamedEvaluator>();

    final Set<Field> allPublicFields =
        new HashSet<Field>(Arrays.asList(builderClass.getFields()));
    this.builderParameters = new ArrayList<NamedEvaluator>();

    for (final NamedEvaluator param : parameters) {
      final String fieldName = NamingHelper.convertNameIntoJavaField(param.getName());
      boolean found = false;
      final Iterator<Field> fieldItr = allPublicFields.iterator();
      while (fieldItr.hasNext()) {
        final Field field = fieldItr.next();
        if (field.getName().equals(fieldName)) {
          found = true;
          allPublicFields.remove(field);
          break;
        }
      }

      if (!found) {
        _logger.debug(
            "Field or method not found {} on *builder* {} (maybe it's on implementation instead)",
            param, builderClass.getName());
        this.nonBuilderParameters.add(param);
      } else {
        this.builderParameters.add(param);
      }
    }

    if (!allPublicFields.isEmpty()) {
      throw new RuntimeException("Fields on builder " + builderClass
          + " not set: " + allPublicFields);
    }

    this.builderImplementationFactory =
        new ImplementationFactoryImpl<U>(builderClass, this.builderParameters);

    _logger.trace(ConfigurationEvents.ADDED_IMPLEMENTATION, getImplementationClass(),
        builderClass);
  }

  public Object getBuilder(final Map<Class<?>, String> overrides)
      throws ObjectInstantiationException, ObjectInitializationException {
    return this.builderImplementationFactory.initialize(overrides);
  }

  // Add builder parameters that also have setters on the builder built object
  private void addAdditionalBuilderParams(final Object object) {
    for (final NamedEvaluator param : this.builderParameters) {
      final String methodName = NamingHelper.convertNameIntoJavaSetter(param.getName());

      final Method[] methods = object.getClass().getMethods();
      for (final Method method : methods) {
        if (method.getName().equals(methodName)) {
          this.nonBuilderParameters.add(param);
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T initialize(final Map<Class<?>, String> overrides)
      throws ObjectInstantiationException, ObjectInitializationException {
    final Object builder = getBuilder(overrides);

    if (_logger.isTraceEnabled()) {
      _logger.trace(ConfigurationEvents.BUILDING_OBJECT, builder);
    }

    try {
      final T object = (T) builder.getClass().getMethod("build", new Class[0]).invoke(builder);
      addAdditionalBuilderParams(object);

      final ReferenceEvaluator<T> ref =
          new ReferenceEvaluator<T>(object, this.nonBuilderParameters);
      return ref.evaluate(overrides);
    } catch (final InvocationTargetException e) {
      _logger.error("Could not construct " + builder.getClass(), e);
      throw new ObjectInitializationException(e.getTargetException().getMessage(), e);
    } catch (final Exception e) {
      throw new ObjectInitializationException(e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<T> getImplementationClass(final Map<Class<?>, String> overrides) {
    final Class<?> builderClass = getBuilderClass(overrides);
    try {
      return (Class<T>) builderClass.getMethod("build").getReturnType();
    } catch (final Exception e) {
      throw new RuntimeException("Failed to get 'build()' method for "
          + builderClass.getCanonicalName(), e);
    } catch (final NoClassDefFoundError e) {
      throw new RuntimeException("Failed to get 'build()' method for "
          + builderClass.getCanonicalName(), e);
    }
  }

  public Class<?> getBuilderClass(final Map<Class<?>, String> overrides) {
    return this.builderImplementationFactory.getImplementationClass(overrides);
  }

  void addParameter(final String name, final boolean required, final Evaluatable value) {
    this.builderImplementationFactory.addParameter(name, required, value);
  }

  @Override
  public List<NamedEvaluator> getParameters(final Map<Class<?>, String> overrides) {
    return this.builderImplementationFactory.getParameters(overrides);
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Set<Class<?>> set =
        new HashSet<>(this.builderImplementationFactory.getDefaultRefs(overrides));
    for (final NamedEvaluator param : this.nonBuilderParameters) {
      set.addAll(param.getDefaultRefs(overrides));
    }
    return set;
  }

  @Override
  public Map<Class<?>, String> getCombinedOverrides(final Map<Class<?>, String> overrides) {
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return this.builderImplementationFactory.toString();
  }

  @Override
  public Map<Class<?>, String> getImplementedInterfaces() {
    return this.builderImplementationFactory.getImplementedInterfaces();
  }

  @Override
  public void setImplementedInterfaces(final Map<Class<?>, String> interfaceMap) {
    this.builderImplementationFactory.setImplementedInterfaces(interfaceMap);
  }
}
