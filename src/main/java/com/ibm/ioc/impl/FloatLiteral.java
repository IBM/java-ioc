/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class FloatLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param floatValue
   */
  public FloatLiteral(final String floatValue) {
    super(Float.parseFloat(floatValue));
  }

  /**
   * Constructs literal from float
   * 
   * @param value
   */
  public FloatLiteral(final float value) {
    super(Float.valueOf(value));
  }
}
