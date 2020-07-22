/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.InflightReloader;
import com.ibm.ioc.InflightReloader.ReloadEvent;
import com.ibm.ioc.WUIni;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INIPropertiesProvider extends PropertiesProviderBase {
  private static final Logger _logger = LoggerFactory.getLogger(INIPropertiesProvider.class);

  private final String prefix;
  private Ini config;

  public INIPropertiesProvider(
      final String prefix,
      final File input,
      final boolean reloadable)
      throws IOException {
    this.prefix = prefix;
    this.config = WUIni.makeWUIni(input);
    if (reloadable) {
      InflightReloader.getInstance().registerPath(input, createReloadEvent());
    }
  }

  public INIPropertiesProvider(
      final String prefix,
      final URI input,
      final boolean reloadable)
      throws IOException {
    this.prefix = prefix;
    this.config = WUIni.makeWUIni(input);
    if (reloadable) {
      InflightReloader.getInstance().registerPath(input, createReloadEvent());
    }
  }

  private ReloadEvent createReloadEvent() {
    return path -> {
      try {
        final WUIni newConfig = WUIni.makeWUIni(path);
        synchronized (INIPropertiesProvider.this) {
          INIPropertiesProvider.this.config = newConfig;
        }

        notifyListeners();
        _logger.info("Reloaded INI file {}", path);
      } catch (final FileNotFoundException e) {
        synchronized (INIPropertiesProvider.this) {
          INIPropertiesProvider.this.config = WUIni.makeWUIni();
        }

        notifyListeners();
        _logger.debug("No ini file found at {}.  Will treat as empty file.", path);
      } catch (final IOException e) {
        _logger.error(e.getMessage(), e);
      }
    };
  }

  @Override
  public String getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException {
    final String[] values = getProperties(qualifiedName);
    if (values == null || values.length == 0) {
      throw new ConfigurationItemNotDefinedException("Property not defined: " + qualifiedName);
    }
    return values[0];
  }

  @SuppressWarnings("squid:S1168")
  private synchronized String[] getProperties(final String key) {
    if (key.startsWith(this.prefix) && key.length() > this.prefix.length()) {
      final String property = key.substring(this.prefix.length());
      final int idx = property.indexOf('.');
      if (idx < 0 || property.length() < idx + 1) {
        return null;
      }
      final String sectionName = property.substring(0, idx);
      final String propertyName = property.substring(idx + 1);

      final Section section = this.config.get(sectionName);
      if (section == null) {
        return null;
      } else if (section.containsKey(propertyName)) {
        return section.fetchAll(propertyName, String[].class);
      } else if (section.containsKey(propertyName.replaceAll("-", "_"))) {
        return section.fetchAll(propertyName.replaceAll("-", "_"), String[].class);
      } else if (section.containsKey(propertyName.replaceAll("_", "-"))) {
        return section.fetchAll(propertyName.replaceAll("_", "-"), String[].class);
      } else {
        return null;
      }
    }

    return null;
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    return getProperties(qualifiedName) != null;
  }

  @Override
  public synchronized Set<String> getQualifiedNames() {
    final Set<String> names = new HashSet<>();
    final Collection<Section> sections = this.config.values();
    for (final Section section : sections) {
      final String sectionName = section.getSimpleName();
      for (final String keyName : section.keySet()) {
        names.add(sectionName + "." + keyName);
      }
    }
    return names;
  }
}
