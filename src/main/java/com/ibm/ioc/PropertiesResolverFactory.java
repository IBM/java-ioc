/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.impl.INIPropertiesProvider;
import com.ibm.ioc.impl.OrderedPropertiesResolver;
import com.ibm.ioc.impl.SystemPropertiesProvider;

public class PropertiesResolverFactory {
  private static final Logger _logger = LoggerFactory.getLogger(PropertiesResolverFactory.class);
  // Development time properties resources
  private static final String INI_DEPLOYMENT_FILE_NAME = "default-properties.ini";

  public static final String RUN_CONFIG_TYPE = "run"; // jsap properties for tools
  public static final String REMOTE_CONFIG_TYPE = "remote"; // user overrides
  public static final String SYSTEM_CONFIG_TYPE = "system"; // system properties or -D flag JVM args
  public static final String DEPLOY_CONFIG_TYPE = "deploy"; // conf file
  public static final String DEVEL_CONFIG_TYPE = ""; // default properties
  private static final String[] propertyLevels = {
      DEVEL_CONFIG_TYPE,
      REMOTE_CONFIG_TYPE,
      DEPLOY_CONFIG_TYPE,
      SYSTEM_CONFIG_TYPE,
      RUN_CONFIG_TYPE};
  
  private static final Set<String> commonPrefixes = new HashSet<>(); 
  
  {
	  commonPrefixes.add("bindings.");
  }
 

  private static PropertiesResolverFactory instance;

  private final OrderedPropertiesResolver resolver;

  private static final String[] defaultIgnorablePrefixes = new String[] {
      "java.",
      "javax.",
      "sun.",
      "org.xml.",
      "org.w3."};
  private static final String DEFAULT_IGNORABLE_PREFIX_PROPERTY = "com.ibm.ioc.ignore-prefix";

  PropertiesResolverFactory() {
    this.resolver = new OrderedPropertiesResolver();
    commonPrefixes.forEach(this.resolver::addDefaultPrefix);

    // Likely to make sense to have ignorable values also obtained from system properties
    String[] ignore = defaultIgnorablePrefixes;
    if (System.getProperty(DEFAULT_IGNORABLE_PREFIX_PROPERTY) != null) {
      ignore = System.getProperty(DEFAULT_IGNORABLE_PREFIX_PROPERTY).split(":");
    }
    this.resolver.setIgnorablePrefixes(Arrays.asList(ignore));

    for (int i = 0; i < propertyLevels.length; i++) {
      this.resolver.announcePropertiesProvider(propertyLevels[i], i);
    }
    registerDevel();
    registerSystem();
  }
  
  public static void setCommonPropertyPrefixes(String... prefixes) {
	  Collections.addAll(commonPrefixes, prefixes);
  }

  private void registerSystem() {
    this.resolver.registerPropertiesProvider(
        propertyLevels[3],
        new SystemPropertiesProvider());
  }

  private void registerDevel() {
    try {
      final Enumeration<URL> urls =
          getClass().getClassLoader().getResources(INI_DEPLOYMENT_FILE_NAME);

      final PropertiesResolver defaultResolver = new OrderedPropertiesResolver();
      int ind = 0;
      while (urls.hasMoreElements()) {
        final URI uri = urls.nextElement().toURI();
        registerIniProvider(uri, defaultResolver, ind);
        ++ind;
      }
      this.resolver.registerPropertiesProvider(
          propertyLevels[0],
          defaultResolver);
    } catch (final Exception e) {
      _logger.error("IOException occurred while trying to load properties from resources.", e);
    }
  }

  private static void registerIniProvider(final URI uri, final PropertiesResolver resolver, final int index) {
    try {
      // packaged ini files don't need reloading
      final INIPropertiesProvider p = new INIPropertiesProvider(uri, false);
      resolver.announcePropertiesProvider(uri.toString(), index);
      resolver.registerPropertiesProvider(uri.toString(), p);

      _logger.trace("Loaded properties from {}", uri);
    } catch (final Exception e) {
      _logger.error("Error loading properties from {}", uri, e);
    }
  }

  public static synchronized OrderedPropertiesResolver getInstance() {
    if (instance == null) {
      instance = new PropertiesResolverFactory();
    }
    return instance.getResolver();
  }

  OrderedPropertiesResolver getResolver() {
    return this.resolver;
  }
}
