/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Set;

/**
 * This interface allows typed values to be accessed by the application using dor separated qualified names. For
 * example, a property's qualified name, <b>com.ibm.network.concurrency</b>
 * 
 * @author Manish Motwani
 */
public interface PropertiesProvider {
  public interface PropertiesModificationListener {
    void modified();
  }

  /**
   * Checks whether the property specified by the input qualified name is set or not.
   * 
   * @param key The key that identifies the desired property.
   * @return True if the property is set; false otherwise.
   */
  boolean isSet(final String qualifiedName);

  /**
   * Returns the string value of the property specified by the input qualified name, if it exists. If the property does
   * not exist, an exception is thrown.
   * 
   * @param qualifiedName The qualified name string array that represents the property.
   * @return The string value for the input property.
   * @throws ConfigurationItemNotDefinedException
   */
  Object getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException;

  /**
   * Introspection method. Returns all known properties defined in this povider
   * 
   * @return
   */
  Set<String> getQualifiedNames();

  void addModificationListener(PropertiesModificationListener listener);
}
