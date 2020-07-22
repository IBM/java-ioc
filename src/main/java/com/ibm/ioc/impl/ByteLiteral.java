/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class ByteLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param byteValue
   */
  public ByteLiteral(final String byteValue) {
    super(Byte.parseByte(byteValue));
  }

  /**
   * Constructs literal from byte
   * 
   * @param value
   */
  public ByteLiteral(final byte value) {
    super(Byte.valueOf(value));
  }
}
