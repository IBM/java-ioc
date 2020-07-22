/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class DoubleLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param doubleValue
   */
  public DoubleLiteral(final String doubleValue) {
    super(Double.parseDouble(doubleValue));
  }

  /**
   * Constructs literal from int or Dpuble
   * 
   * @param value
   */
  public DoubleLiteral(final double value) {
    super(Double.valueOf(value));
  }
}
