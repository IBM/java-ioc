/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

// TODO: Describe class or interface
public class LongLiteral extends LiteralEvaluator {
  /**
   * Constructs literal from string
   * 
   * @param longValue long value as a string
   */
  public LongLiteral(final String longValue) {
    super(Long.parseLong(longValue));
  }

  /**
   * Constructs literal from int or Long
   * 
   * @param value Long value
   */
  public LongLiteral(final long value) {
    super(Long.valueOf(value));
  }
}
