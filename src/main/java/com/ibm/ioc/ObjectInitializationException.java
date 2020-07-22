/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public class ObjectInitializationException extends ConfigurationException {
  private static final long serialVersionUID = 1490014508414495009L;

  public ObjectInitializationException() {}

  public ObjectInitializationException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public ObjectInitializationException(final String reason) {
    super(reason);
  }

  public ObjectInitializationException(final Throwable cause) {
    super(cause);
  }

}
