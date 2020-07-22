/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

public class ObjectInstantiationException extends ConfigurationException {

  private static final long serialVersionUID = -796456531471319578L;

  public ObjectInstantiationException() {}

  public ObjectInstantiationException(final String reason, final Throwable cause) {
    super(reason, cause);
  }

  public ObjectInstantiationException(final String reason) {
    super(reason);
  }

  public ObjectInstantiationException(final Throwable cause) {
    super(cause);
  }

}
