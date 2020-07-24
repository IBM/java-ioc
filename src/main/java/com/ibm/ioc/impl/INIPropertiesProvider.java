/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.InflightReloader;
import com.ibm.ioc.InflightReloader.ReloadEvent;

public class INIPropertiesProvider extends PropertiesProviderBase {
  private static final Logger _logger = LoggerFactory.getLogger(INIPropertiesProvider.class);

  private Map<String, String> properties = new HashMap<>();

  public INIPropertiesProvider(
      final File input,
      final boolean reloadable)
      throws IOException {
    try(InputStream is = new FileInputStream(input)) {
      readIniFile(is, this.properties);
    } catch (FileNotFoundException ignore) {
    }
    if (reloadable) {
      InflightReloader.getInstance().registerPath(input, createReloadEvent());
    }
  }

  public INIPropertiesProvider(
      final URI input,
      final boolean reloadable)
      throws IOException {
    try(InputStream is = input.toURL().openStream()) {
      readIniFile(is, this.properties);
    } catch (FileNotFoundException ignore) {
    }
    if (reloadable) {
      InflightReloader.getInstance().registerPath(input, createReloadEvent());
    }
  }

  private ReloadEvent createReloadEvent() {
    return path -> {
      try {
        Map<String, String> newProperties = new HashMap<>();
        try(InputStream is = new FileInputStream(path)) {
            readIniFile(is, newProperties);
        }
        synchronized (INIPropertiesProvider.this) {
          this.properties = newProperties;
        }

        notifyListeners();
        _logger.info("Reloaded INI file {}", path);
      } catch (final FileNotFoundException e) {
        synchronized (INIPropertiesProvider.this) {
          this.properties = new HashMap<>();
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
    final String value = getPropertyValue(qualifiedName);
    if (value == null) {
      throw new ConfigurationItemNotDefinedException("Property not defined: " + qualifiedName);
    }
    return value;
  }

  private String getPropertyValue(String qualifiedName) {
    final int idx = qualifiedName.indexOf('.');
    if (idx < 0 || qualifiedName.length() < idx + 1) {
      return null;
    }
    final String sectionName = qualifiedName.substring(0, idx);
    final String propertyName = qualifiedName.substring(idx + 1);

    if (sectionName == null || sectionName.isEmpty()) {
      return null;
    } else if (this.properties.containsKey(sectionName + "." + propertyName)) {
      return this.properties.get(sectionName + "." + propertyName);
    } else {
      final String propertyNameAlt1 = propertyName.replace("-", "_");
      final String propertyNameAlt2 = propertyName.replace("_", "-");
      for(String alt : new String[]{propertyNameAlt1, propertyNameAlt2}) {
        String altQualifiedName = sectionName + "." + alt;
        if(this.properties.containsKey(altQualifiedName)) {
          return this.properties.get(altQualifiedName);
        }
      }
      return null;
    }
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    return getPropertyValue(qualifiedName) != null;
  }

  @Override
  public synchronized Set<String> getQualifiedNames() {
    return Collections.unmodifiableSet(this.properties.keySet());
  }

  private String parseValue(Object value) {
    if(value instanceof String) {
      return (String) value;
    } else if(value instanceof Collection) {
      Collection<?> valueCollection = ((Collection<?>) value);
      if(!valueCollection.isEmpty()) {
        return parseValue(valueCollection.iterator().next());
      } else {
        return null;
	  }
    } else if(value instanceof Object[]) {
      Object[] valueArray = (Object[]) value;
      if(valueArray.length > 0) {
        return parseValue(valueArray[0]);
      } else {
        return null;
	  }
	} else {
      return String.valueOf(value);
	}
  }

  private void readIniFile(final InputStream inputStream, final Map<String, String> properties) throws IOException {
    INIConfiguration iniConfiguration = new INIConfiguration();
    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      iniConfiguration.read(reader);
    } catch (Exception e) {
      throw new IOException("Can't read ini file: " + inputStream, e);
    }

    Iterator<String> keysIterator = iniConfiguration.getKeys();
    while (keysIterator.hasNext()) {
      String key = keysIterator.next();
      Object value = iniConfiguration.getProperty(key);
      if(value != null) {
        String stringValue = parseValue(value);
        if(stringValue != null) {
          properties.put(key, stringValue);
        }
      }
    }
    for (String sectionName : iniConfiguration.getSections()) {
      if (sectionName == null) {
        continue;
      }
      SubnodeConfiguration section = iniConfiguration.getSection(sectionName);
      Iterator<String> sectionKeysIterator = section.getKeys();
      while (sectionKeysIterator.hasNext()) {
        String key = sectionKeysIterator.next();
        Object value = section.getProperty(key);
        if(value != null) {
          String stringValue = parseValue(value);
          if(stringValue != null) {
            properties.put(sectionName + "." + key, stringValue);
          }
        }
      }
    }
  }
}
