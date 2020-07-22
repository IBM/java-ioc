/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collections;
import java.util.Set;

import com.ibm.ioc.RemotePropertiesLookup;

/**
 * Returns and contains no properties
 */
public class NullRemotePropertiesLookup implements RemotePropertiesLookup {
  @Override
  public boolean contains(final String key) {
    return false;
  }

  @Override
  public String get(final String key) {
    return null;
  }

  @Override
  public Set<String> listKeys() {
    return Collections.emptySet();
  }

  @Override
  public void setListener(final ModificationListener listener) {

  }
}
