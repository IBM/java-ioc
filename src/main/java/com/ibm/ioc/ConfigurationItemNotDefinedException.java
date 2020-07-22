/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public class ConfigurationItemNotDefinedException extends ConfigurationException {
  private static final long serialVersionUID = -4667227142248411836L;

  public ConfigurationItemNotDefinedException() {}

  public ConfigurationItemNotDefinedException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public ConfigurationItemNotDefinedException(final String reason) {
    super(reason);
  }

  public ConfigurationItemNotDefinedException(final Throwable cause) {
    super(cause);
  }

}
