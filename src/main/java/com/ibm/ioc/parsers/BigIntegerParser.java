/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.io.IOException;
import java.math.BigInteger;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class BigIntegerParser {
  public static BigInteger parse(final String stringValue) {
    if (stringValue == null) {
      return null;
    }
    final Long parsedLong = LongParser.parse(stringValue);
    if (parsedLong != null) {
      return BigInteger.valueOf(parsedLong);
    } else {
      try {
        return new BigInteger(stringValue);
      } catch (final NumberFormatException e) {
        return null;
      }
    }
  }

  public static TypeAdapter<BigInteger> getGsonTypeAdapter() {
    return new TypeAdapter<BigInteger>() {
      @Override
      public BigInteger read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        final String strVal = in.nextString();
        final BigInteger val = parse(strVal);
        if (val == null) { // cannot parse
          throw new JsonSyntaxException("Cannot parse " + strVal + " into BigInteger");
        }
        return val;
      }

      @Override
      public void write(final JsonWriter out, final BigInteger value) throws IOException {
        out.value(value);
      }
    };
  }
}
