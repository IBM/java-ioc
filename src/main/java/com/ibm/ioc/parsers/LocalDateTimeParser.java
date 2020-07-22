/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTimeParser {

  private LocalDateTimeParser() {
    // This is a utility class, and therefore should have no public constructor
  }

  public static LocalDateTime parse(final String stringValue) {
    if (stringValue == null) {
      return null;
    }
    try {
      return LocalDateTime.parse(stringValue);
    } catch (final DateTimeParseException e) {
      return null;
    }
  }

  public static TypeAdapter<LocalDateTime> getGsonTypeAdapter() {
    return new TypeAdapter<LocalDateTime>() {
      @Override
      public LocalDateTime read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        final String strVal = in.nextString();
        final LocalDateTime val = parse(strVal);
        if (val == null) { // cannot parse
          throw new JsonSyntaxException("Cannot parse " + strVal + " into LocalDateTime");
        }
        return val;
      }

      @Override
      public void write(final JsonWriter out, final LocalDateTime value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        out.value(value.toString());
      }
    };
  }
}


