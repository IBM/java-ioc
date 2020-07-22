/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class BooleanLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param booleanValue
   */
  public BooleanLiteral(final String booleanValue) {
    super(Boolean.parseBoolean(booleanValue));
  }

  /**
   * Constructs literal from boolean
   * 
   * @param value
   */
  public BooleanLiteral(final boolean value) {
    super(Boolean.valueOf(value));
  }
}
