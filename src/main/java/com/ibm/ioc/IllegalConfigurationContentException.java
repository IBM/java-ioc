/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public class IllegalConfigurationContentException extends ConfigurationException {
  private static final long serialVersionUID = -7281640766238667185L;

  public IllegalConfigurationContentException() {}

  public IllegalConfigurationContentException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public IllegalConfigurationContentException(final String reason) {
    super(reason);
  }

  public IllegalConfigurationContentException(final Throwable cause) {
    super(cause);
  }
}
