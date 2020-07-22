/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Set;

import com.ibm.ioc.impl.PropertiesProviderBase;

public class RemotePropertiesProvider extends PropertiesProviderBase {
  public static class Builder {
    public RemotePropertiesLookup remotePropertiesLookup;

    public RemotePropertiesProvider build() {
      return new RemotePropertiesProvider(this.remotePropertiesLookup);
    }
  }

  private final RemotePropertiesLookup remotePropertiesLookup;

  public RemotePropertiesProvider(final RemotePropertiesLookup remotePropertiesLookup) {
    this.remotePropertiesLookup = remotePropertiesLookup;
    this.remotePropertiesLookup.setListener(this::notifyListeners);
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    return this.remotePropertiesLookup.contains(qualifiedName);
  }

  @Override
  public String getProperty(final String qualifiedName) {
    return this.remotePropertiesLookup.get(qualifiedName);
  }

  @Override
  public Set<String> getQualifiedNames() {
    return this.remotePropertiesLookup.listKeys();
  }
}
