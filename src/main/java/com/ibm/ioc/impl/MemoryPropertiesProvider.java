/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.ioc.ConfigurationItemNotDefinedException;

public class MemoryPropertiesProvider extends PropertiesProviderBase {
  private final Map<String, String> properties = new ConcurrentHashMap<>();

  public void addProperty(final String key, final String value) {
    this.properties.put(key, value);
    notifyListeners();
  }

  public void addProperties(final Map<String, String> properties) {
    this.properties.putAll(properties);
    notifyListeners();
  }

  public void removeProperty(final String key) {
    this.properties.remove(key);
    notifyListeners();
  }

  public void clear() {
    this.properties.clear();
    notifyListeners();
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    return this.properties.containsKey(qualifiedName);
  }

  @Override
  public String getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException {
    final String prop = this.properties.get(qualifiedName);
    if (prop == null) {
      throw new ConfigurationItemNotDefinedException();
    }
    return prop;
  }

  @Override
  public Set<String> getQualifiedNames() {
    return new HashSet<String>(this.properties.keySet());
  }
}
