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
public final class LongParser {
  private static final Map<String, Long> units = new HashMap<>();

  private LongParser() {
    // This is a utility class, and therefore should have no public constructor
  }

  static {
    units.put("K", 1000L);
    units.put("M", 1000L * 1000L);
    units.put("G", 1000L * 1000L * 1000L);
    units.put("T", 1000L * 1000L * 1000L * 1000L);
    units.put("P", 1000L * 1000L * 1000L * 1000L * 1000L);
    units.put("KB", 1024L);
    units.put("MB", 1024L * 1024L);
    units.put("GB", 1024L * 1024L * 1024L);
    units.put("TB", 1024L * 1024L * 1024L * 1024L);
    units.put("PB", 1024L * 1024L * 1024L * 1024L * 1024L);
    units.put("KIB", 1024L);
    units.put("MIB", 1024L * 1024L);
    units.put("GIB", 1024L * 1024L * 1024L);
    units.put("TIB", 1024L * 1024L * 1024L * 1024L);
    units.put("PIB", 1024L * 1024L * 1024L * 1024L * 1024L);

    units.put("MS", 1L);
    units.put("MSEC", 1L);
    units.put("MILLI", 1L);
    units.put("MILLIS", 1L);
    units.put("S", 1000L);
    units.put("SEC", 1000L);
    units.put("SECS", 1000L);
    units.put("SECOND", 1000L);
    units.put("SECONDS", 1000L);
    units.put("MIN", 60L * 1000L);
    units.put("MINS", 60L * 1000L);
    units.put("MINUTE", 60L * 1000L);
    units.put("MINUTES", 60L * 1000L);
    units.put("HR", 60L * 60L * 1000L);
    units.put("HRS", 60L * 60L * 1000L);
    units.put("HOUR", 60L * 60L * 1000L);
    units.put("HOURS", 60L * 60L * 1000L);
    units.put("DAY", 24L * 60L * 60L * 1000L);
    units.put("DAYS", 24L * 60L * 60L * 1000L);
    units.put("WEEK", 7L * 24L * 60L * 60L * 1000L);
    units.put("WEEKS", 7L * 24L * 60L * 60 * 1000L);
    units.put("YEAR", 365L * 24 * 60L * 60L * 1000L);
    units.put("YEARS", 365L * 24L * 60L * 60L * 1000L);
  }

  public static Long parse(final String string) {
    final NumberParser parse = NumberParser.parse(string);

    if (parse.getNumber() == null || parse.getUnits() == null) {
      // Number failed to parse, return null
      return null;
    }

    final Long multiplier;
    if (parse.getUnits().isEmpty()) {
      multiplier = 1L;
    } else {
      multiplier = units.get(parse.getUnits());
      if (multiplier == null) {
        return null;
      }
    }

    if (parse.isDecimal()) {
      return (long) (Double.valueOf(parse.getNumber()) * multiplier);
    } else {
      return Long.valueOf(parse.getNumber()) * multiplier;
    }
  }

  public static TypeAdapter<Long> getGsonTypeAdapter() {
    return new TypeAdapter<Long>() {
      @Override
      public Long read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }
        try {
          return in.nextLong();
        } catch (final NumberFormatException e) {
          final String strVal = in.nextString();
          final Long val = parse(strVal);
          if (val == null) { // cannot parse
            throw new JsonSyntaxException(e);
          }
          return val;
        }
      }

      @Override
      public void write(final JsonWriter out, final Long value) throws IOException {
        out.value(value);
      }
    };
  }

}
