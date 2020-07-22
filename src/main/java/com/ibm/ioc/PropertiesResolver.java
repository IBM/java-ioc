/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

/**
 * PropertyResolver provide a mechanism to resolve value of a desired property at run time from multiple sources.
 * Particularly a few level of property value settings are meant to be supported:
 * <ol>
 * <li>development setting stored in properties-devel.xml in a class path</li>
 * <li>deployment time properties passed to the application properties-deploy.xml</li>
 * <li>system properties set in run time environment and obtained from System.getProperties()</li>
 * <li>jsap properties passed in the command line using</li>
 * </ol>
 * JSAP The resolution happens in the order described
 */
public interface PropertiesResolver extends PropertiesProvider {
  /**
   * Announces a new property and its order. The higher the order the better chances
   * 
   * @param type named type of a provider
   * @param order value from 0 and higher
   */
  void announcePropertiesProvider(String type, int order);

  /**
   * Each property according its order, null for missing
   * 
   * @return
   */
  String[] getAnnouncedProperties();

  /**
   * Registers provider for previously announced type
   * 
   * @param type
   * @param provider property provider itself. null would remove this provider without changing order
   */
  void registerPropertiesProvider(String type, PropertiesProvider provider);

  /**
   * Order of a given type
   * 
   * @param type @return, -1 if not found
   */
  int findTypeOrder(String type);

  /**
   * @return all announced types, even if they haven't been registered
   */
  String[] getAnnouncedTypes();

  /**
   * PRE-CONDITION: findTypeOrder(String type) != -1
   * 
   * @param type
   * @return provider, null if none registered
   */
  PropertiesProvider getPropertiesProvider(String type);

  /**
   * Tries to resolve property to a specified type. If a type of property doesn't match the one provided in the call,
   * various attempts to do conversion are made.
   * 
   * Particularly method valueOf(), fromString() and decode() are used. Special type specific parses classes could be
   * used (class name is based on pattern)
   * 
   * @param <T> Type of property
   * @param name property name
   * @param clazz class of T need for implementation purposes)
   * @return object of a given type constructed from a property value
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  <T> T resolve(final String name, final Class<T> clazz)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException;

  /**
   * Tries to resolve property to String type
   * <li>Convenience the same as
   * <li>resolve(name, String.class)
   * 
   * @param name
   * @return
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  String resolveString(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;

  /**
   * Tries to resolve property to Integer type
   * <li>Convenience the same as
   * <li>resolve(name, int.class)
   * 
   * @param name
   * @return
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  int resolveInt(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;

  /**
   * Tries to resolve property to Long type
   * <li>Convenience the same as
   * <li>resolve(name, long.class)
   * 
   * @param name
   * @return
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  long resolveLong(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;

  /**
   * Tries to resolve property to Boolean type
   * <li>Convenience the same as
   * <li>resolve(name, boolean.class)
   * 
   * @param name
   * @return
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  boolean resolveBoolean(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;

  /**
   * Tries to resolve property to Double type
   * <li>Convenience the same as
   * <li>resolve(name, Double.class)
   * 
   * @param name
   * @return
   * @throws ConfigurationItemNotDefinedException
   * @throws ObjectInitializationException
   */
  double resolveDouble(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException;
}
