/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Set;

/**
 * Hook to look up properties remotely. Users will have to implement this and inject it into
 * {@link RemotePropertiesProvider} and register {@link RemotePropertiesProvider} with {@link PropertiesResolverFactory}
 * as a PropertiesResolverFactory.REMOTE_CONFIG_TYPE.
 * 
 * For example:
 * 
 * <pre>
 * BindingsProvider bindingsProvider = new JavaBindingsProvider();
 * final RemotePropertiesProvider remotePropertiesProvider =
 *     bindingsProvider.getDefaultImplementation(RemotePropertiesProvider.class).initialize();
 * PropertiesResolverFactory.getInstance().registerPropertiesProvider(
 *     PropertiesResolverFactory.REMOTE_CONFIG_TYPE, remotePropertiesProvider);
 * </pre>
 * 
 */
public interface RemotePropertiesLookup {
  interface ModificationListener {
    void modified();
  }

  boolean contains(final String key);

  String get(final String key);

  Set<String> listKeys();

  void setListener(ModificationListener listener);
}
