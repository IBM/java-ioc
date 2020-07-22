/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;

public class BindingsMap<V> implements Map<String, V> {

  private final BindingsProvider bindingsProvider;
  private final Class<V> interfaceClass;

  private Map<String, V> implementationMap = null;
  private final Map<Class<?>, String> overrides;

  public BindingsMap(final Class<V> cls, final BindingsProvider bindingsProvider,
      final Map<Class<?>, String> overrides) {
    this.interfaceClass = cls;
    this.bindingsProvider = bindingsProvider;
    this.overrides = overrides;
  }

  public BindingsProvider getBindingsProvider() {
    return this.bindingsProvider;
  }

  public Class<?> getInterfaceClass() {
    return this.interfaceClass;
  }

  private ImplementationFactory<V> _get(final Object key) {
    if (key == null) {
      throw new NullPointerException();
    }

    if (!(key instanceof String)) {
      return null;
    }

    try {
      return this.bindingsProvider.getImplementation(this.interfaceClass, (String) key);
    } catch (final ConfigurationItemNotDefinedException e) {
      return null;
    }
  }

  @Override
  public void clear() {
    throw new RuntimeException("clear() not implemented for BindingsMap");
  }

  @Override
  public boolean containsKey(final Object key) {
    return keySet().contains(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return values().contains(value);
  }

  private synchronized Map<String, V> getImplementationMap() {
    if (this.implementationMap != null) {
      return this.implementationMap;
    }

    this.implementationMap = new HashMap<>();
    try {
      final Map<String, ImplementationFactory<? extends V>> map =
          this.bindingsProvider.getImplementations(this.interfaceClass);

      for (final String ref : map.keySet()) {
        this.implementationMap.put(ref, map.get(ref).initialize(this.overrides));
      }
    } catch (final ConfigurationItemNotDefinedException ignore) {
    } catch (final ObjectInstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final ObjectInitializationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

    return this.implementationMap;
  }

  @Override
  public Set<java.util.Map.Entry<String, V>> entrySet() {
    return getImplementationMap().entrySet();
  }

  @Override
  public V get(final Object key) {
    final ImplementationFactory<? extends V> fact = _get(key);

    if (fact == null) {
      return null;
    }

    try {
      return fact.initialize(this.overrides);
    } catch (final ObjectInstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final ObjectInitializationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean isEmpty() {
    return keySet().isEmpty();
  }

  @Override
  public Set<String> keySet() {
    return getImplementationMap().keySet();
  }

  @Override
  public V put(final String key, final Object value) {
    throw new RuntimeException("put() not implemented for BindingsMap");
  }

  @Override
  public void putAll(final Map<? extends String, ? extends V> m) {
    throw new RuntimeException("putAll() not implemented for BindingsMap");
  }

  @Override
  public V remove(final Object key) {
    throw new RuntimeException("remove() not implemented for BindingsMap");
  }

  @Override
  public int size() {
    return keySet().size();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Collection<V> values() {
    // HashSet to make values unique
    return new HashSet(getImplementationMap().values());
  }

  @Override
  public String toString() {
    return entrySet().toString();
  }
}
