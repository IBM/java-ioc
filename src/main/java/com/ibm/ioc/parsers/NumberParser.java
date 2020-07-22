/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

/*
 * Allows to specify integer with known units of time or size Time units are always converted to milliseconds. <li> The
 * format should be look <number>unit, spaces are allowed
 */


public final class NumberParser {

  private static final NumberParser NO_NUMBER = new NumberParser(null, null, false);
  private final String number;
  private final String units;
  private final boolean isDecimal;

  private NumberParser(final String number, final String units, final boolean isDecimal) {
    this.number = number;
    this.units = units;
    this.isDecimal = isDecimal;
  }

  static NumberParser parse(final String stringValue) {
    if (stringValue == null) {
      return NO_NUMBER;
    }

    final String stringToParse = stringValue.toUpperCase().trim();
    // Find the last digit
    int index = 0;
    boolean decimal = false;
    while (index < stringToParse.length()) {
      if (stringToParse.charAt(index) == '.') {
        decimal = true;
      } else if (!Character.isDigit(stringToParse.charAt(index))) {
        break;
      }
      index++;
    }
    if (index == 0) {
      // No number
      return NO_NUMBER;
    }

    final String multiplier = stringToParse.substring(index).trim();
    return new NumberParser(stringToParse.substring(0, index), multiplier, decimal);
  }

  public boolean isDecimal() {
    return this.isDecimal;
  }

  public String getUnits() {
    return this.units;
  }

  public String getNumber() {
    return this.number;
  }
}


