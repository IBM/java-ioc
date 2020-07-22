/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.HashSet;
import java.util.Set;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertiesProvider extends PropertiesProviderBase {
  private static final Logger _logger = LoggerFactory.getLogger(SystemPropertiesProvider.class);

  @Override
  public boolean isSet(final String qualifiedName) {
    return System.getProperty(qualifiedName) != null;
  }

  public void addProperty(final String key, final Object value) {
    System.setProperty(key, value.toString());
  }

  public void addProperty(final String key, final Object[] objArray) {
    System.setProperty(key, convertArrayToProperty(objArray));
  }

  private String convertArrayToProperty(final Object[] array) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      final Object obj = array[i];
      builder.append(obj.toString());

      if (i != array.length - 1) {
        builder.append(",");
      }
    }

    return builder.toString();
  }

  @Override
  public Set<String> getQualifiedNames() {
    return new HashSet<String>(System.getProperties().stringPropertyNames());
  }

  @Override
  public String getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException {
    final String ret = System.getProperty(qualifiedName);
    if (ret != null) {
      if (_logger.isTraceEnabled()) {
        _logger.trace("Resolved " + qualifiedName + " in global context to " + ret.toString());
      }
      return ret;
    }
    throw new ConfigurationItemNotDefinedException("property " + qualifiedName + " not found.");
  }
}
