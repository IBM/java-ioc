/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class IntegerLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param intValue
   */
  public IntegerLiteral(final String intValue) {
    super(Integer.parseInt(intValue));
  }

  /**
   * Constructs literal from int
   * 
   * @param value
   */
  public IntegerLiteral(final int value) {
    super(Integer.valueOf(value));
  }
}
