/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalTimeParser {

  private LocalTimeParser() {
    // This is a utility class, and therefore should have no public constructor
  }

  public static LocalTime parse(final String stringValue) {
    if (stringValue == null) {
      return null;
    }
    try {
      return LocalTime.parse(stringValue);
    } catch (final DateTimeParseException e) {
      return null;
    }
  }

  public static TypeAdapter<LocalTime> getGsonTypeAdapter() {
    return new TypeAdapter<LocalTime>() {
      @Override
      public LocalTime read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        final String strVal = in.nextString();
        final LocalTime val = parse(strVal);
        if (val == null) { // cannot parse
          throw new JsonSyntaxException("Cannot parse " + strVal + " into LocalTime");
        }
        return val;
      }

      @Override
      public void write(final JsonWriter out, final LocalTime value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        out.value(value.toString());
      }
    };
  }
}


