/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public abstract class ConfigurationException extends Exception {
  private static final long serialVersionUID = 2655529119112888434L;

  public ConfigurationException() {
    super();
  }

  public ConfigurationException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public ConfigurationException(final String reason) {
    super(reason);
  }

  public ConfigurationException(final Throwable cause) {
    super(cause);
  }
}
