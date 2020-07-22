/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Collections;
import java.util.List;

import com.ibm.ioc.Binding;
import com.ibm.ioc.IllegalConfigurationContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceBinding extends Binding {
  private static final Logger _logger = LoggerFactory.getLogger(InterfaceBinding.class);

  // Will be set if this binding requires an interface
  private final Class<?> requiredInterface;

  public InterfaceBinding(final Class<?> requiredInterface) {
    assert requiredInterface != null;
    this.requiredInterface = requiredInterface;
  }

  public Class<?> getRequiredInterface() {
    return this.requiredInterface;
  }

  @Override
  protected void checkRequiredInterface(final ImplementationFactory<?> implementationFactory)
      throws IllegalConfigurationContentException {
    final Class<?> implementationClass = implementationFactory.getImplementationClass();
    if (!this.requiredInterface.isAssignableFrom(implementationClass)) {
      _logger.error(ConfigurationEvents.IMPLEMENTATION_MUST_EXTEND_INTERFACE,
          implementationClass, getQualifiedBindingName(), this.requiredInterface);
      throw new IllegalConfigurationContentException("binding=" + getQualifiedBindingName()
          + " doesn't extend interface=" + this.requiredInterface);
    }
  }

  @Override
  public List<String> getQualifiedBindingName() {
    return Collections.singletonList(getRequiredInterface().getName());
  }
}
