/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/*
 * Allows to specify integer with known units of time or size Time units are always converted to milliseconds. <li> The
 * format should be look <number>unit, spaces are allowed
 */
@SuppressWarnings("squid:S109")
public final class IntegerParser {
  private static final Map<String, Integer> units = new HashMap<>();

  private IntegerParser() {
    // This is a utility class, and therefore should have no public constructor
  }


  static {
    units.put("K", 1000);
    units.put("M", 1000 * 1000);
    units.put("KB", 1024);
    units.put("MB", 1024 * 1024);
    units.put("KIB", 1024);
    units.put("MIB", 1024 * 1024);

    units.put("MS", 1);
    units.put("MSEC", 1);
    units.put("MSECS", 1);
    units.put("MILLI", 1);
    units.put("MILLIS", 1);
    units.put("S", 1000);
    units.put("SEC", 1000);
    units.put("SECS", 1000);
    units.put("SECOND", 1000);
    units.put("SECONDS", 1000);
    units.put("MIN", 60 * 1000);
    units.put("MINS", 60 * 1000);
    units.put("MINUTE", 60 * 1000);
    units.put("MINUTES", 60 * 1000);
    units.put("HR", 60 * 60 * 1000);
    units.put("HRS", 60 * 60 * 1000);
    units.put("HOUR", 60 * 60 * 1000);
    units.put("HOURS", 60 * 60 * 1000);
    units.put("DAY", 24 * 60 * 60 * 1000);
    units.put("DAYS", 24 * 60 * 60 * 1000);
  }

  public static Integer parse(final String string) {
    final NumberParser parse = NumberParser.parse(string);

    if (parse.getNumber() == null || parse.getUnits() == null) {
      // Number failed to parse, return null
      return null;
    }

    final Integer multiplier;
    if (parse.getUnits().isEmpty()) {
      multiplier = 1;
    } else {
      multiplier = units.get(parse.getUnits());
      if (multiplier == null) {
        return null;
      }
    }

    if (parse.isDecimal()) {
      return (int) (Double.valueOf(parse.getNumber()) * multiplier);
    } else {
      return Integer.valueOf(parse.getNumber()) * multiplier;
    }
  }

  public static TypeAdapter<Integer> getGsonTypeAdapter() {
    return new TypeAdapter<Integer>() {
      @Override
      public Integer read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }
        try {
          return in.nextInt();
        } catch (final NumberFormatException e) {
          final String strVal = in.nextString();
          final Integer val = parse(strVal);
          if (val == null) { // cannot parse
            throw new JsonSyntaxException(e);
          }
          return val;
        }
      }

      @Override
      public void write(final JsonWriter out, final Integer value) throws IOException {
        out.value(value);
      }
    };
  }

}
