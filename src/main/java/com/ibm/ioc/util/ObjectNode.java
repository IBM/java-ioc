/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ObjectNode {
  final Supplier<?> objectSupplier;
  final Class<?> implementationClass;
  final List<ObjectNode> dependencies;
  final Map<Class<?>, String> implementedInterfaces;

  public ObjectNode(
      final Class<?> implementationClass,
      final Supplier<?> objectSupplier,
      final Map<Class<?>, String> implementedInterfaces,
      final List<ObjectNode> dependencies) {
    this.implementationClass = implementationClass;
    this.objectSupplier = objectSupplier;
    this.implementedInterfaces = implementedInterfaces;
    this.dependencies = Collections.unmodifiableList(dependencies);
  }

  public Supplier<?> getObjectSupplier() {
    return this.objectSupplier;
  }

  public List<ObjectNode> getDependencies() {
    return this.dependencies;
  }

  public Class<?> getImplementationClass() {
    return this.implementationClass;
  }

  public String getNameByInterface(final Class<?> interfaceClass) {
    return this.implementedInterfaces.get(interfaceClass);
  }

  @Override
  public String toString() {
    return this.implementationClass.toString();
  }
}
