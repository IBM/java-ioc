/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

public class ConfigurationEvents {
  // Debug
  public static final String LOADED_BINDINGS = "Loaded bindings: {}.";
  public static final String BUILDING_OBJECT = "Building object of class {}.";
  public static final String ADDED_IMPLEMENTATION = "Implementation {}.";
  public static final String INITIALIZING_OBJECT = "Initializing object of class {}.";

  // Warn
  public static final String ILLEGAL_FORMAT =
      "Found bindings file with illegal format: {}; Skipping...";
  public static final String ILLEGAL_CONTENT =
      "Found bindings file with illegal content: {}; Skipping...";
  public static final String COULD_NOT_LOAD_BINDINGS =
      "Could not load bindings file: {}; Skipping...";

  // Errors
  public static final String BINDINGS_PROVIDER_LOAD_ERROR =
      "Bindings provider could not be loaded.";
  public static final String IMPLEMENTATION_MUST_EXTEND_INTERFACE =
      "Implementation class {} in binding {} must implement the required interface {}.";
  public static final String MULTIPLE_DEFAULT_IMPLEMENTATIONS =
      "Multiple default implementations defined for binding {}: {}.";
  public static final String NO_DEFAULT_IMPLEMENTATION =
      "No default implementation defined for binding {}.";
  public static final String NO_IMPLEMENTATION_FOR_REFERRAL =
      "No implementation is defined for binding {} and referral {}.";
  public static final String NO_IMPLEMENTATION_FOR_VERSION =
      "No implementation is defined for binding {}, referral {} and version {}.";

}
