/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import com.ibm.ioc.JavaBindingsProvider.BindingsFactory;

public interface JavaBindings {
  void register(final BindingsFactory def);
}
