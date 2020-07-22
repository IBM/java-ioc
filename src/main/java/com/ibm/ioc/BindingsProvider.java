/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.List;
import java.util.Map;

import com.ibm.ioc.impl.ImplementationFactory;
import com.ibm.ioc.impl.InterfaceBinding;

// TODO: Describe class or interface
public interface BindingsProvider {
  /**
   * Returns the default {@link ImplementationFactory} for the input interface class. Throws an exception if an
   * implementation is not found in configuration for the given interface.
   * 
   * @param interfaceClass The interface class to look for in configuration.
   * @return
   * @throws ConfigurationItemNotDefinedException
   */
  <T> ImplementationFactory<T> getDefaultImplementation(Class<T> interfaceClass)
      throws ConfigurationItemNotDefinedException;

  /**
   * Returns the {@link ImplementationFactory} from an interface class + referral combination.
   * 
   * @param <T>
   * @param interfaceClass
   * @param referral
   * @return
   * @throws ConfigurationItemNotDefinedException
   */
  <T> ImplementationFactory<T> getImplementation(
      final Class<T> interfaceClass,
      final String referral) throws ConfigurationItemNotDefinedException;

  /**
   * Returns a map of all referrals to their {@link ImplementationFactory} map.
   * 
   * @param <T>
   * @param interfaceClass
   * @param referral
   * @return
   * @throws ConfigurationItemNotDefinedException
   */
  <T> Map<String, ImplementationFactory<? extends T>> getImplementations(
      final Class<T> interfaceClass) throws ConfigurationItemNotDefinedException;

  /**
   * This is a auxiliary method to detect objects that can't be instantiated from bindings
   * 
   * Should be used by unit tests
   * 
   * @return for each unique binding list of uninstantiable referrals or empty string for a default implementation
   */
  Map<Class<?>, List<String>> selfTest();

  /**
   * Get the map of all interfaces in bindings
   * 
   * @return map of interfaces
   */
  Map<Class<?>, InterfaceBinding> getInterfaceBindingsMap();
}
