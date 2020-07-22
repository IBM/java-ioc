/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class ShortLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param shortValue
   */
  public ShortLiteral(final String shortValue) {
    super(Short.parseShort(shortValue));
  }

  /**
   * Constructs literal from Short
   * 
   * @param value
   */
  public ShortLiteral(final short value) {
    super(Short.valueOf(value));
  }
}
