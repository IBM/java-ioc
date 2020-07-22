/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public class MethodInvocationException extends ConfigurationException {
  private static final long serialVersionUID = 6681577387881931982L;

  public MethodInvocationException() {}

  public MethodInvocationException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public MethodInvocationException(final String reason) {
    super(reason);
  }

  public MethodInvocationException(final Throwable cause) {
    super(cause);
  }
}
