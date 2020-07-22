/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.ioc.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertiesProviderBase implements PropertiesProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PropertiesProviderBase.class);

  private final Collection<PropertiesModificationListener> listeners =
      Collections.newSetFromMap(new ConcurrentHashMap<PropertiesModificationListener, Boolean>());

  @Override
  public final void addModificationListener(final PropertiesModificationListener listener) {
    _logger.debug("Added modification listener {}, {} listeners", listener, this.listeners.size());
    this.listeners.add(listener);

    // immediately trigger callback in case we'ved missed an earlier notify
    listener.modified();
  }

  public void notifyListeners() {
    for (final PropertiesModificationListener listener : this.listeners) {
      listener.modified();
    }
  }
}
