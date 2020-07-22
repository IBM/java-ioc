/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

final class Modifiables {
  public static <T> Modifiable<T> valueOf(final T obj) {
    return new ModifiableImpl<T>(obj);
  }
}
