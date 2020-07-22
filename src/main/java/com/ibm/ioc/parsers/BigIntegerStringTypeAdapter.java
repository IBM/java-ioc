/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;


import java.lang.reflect.Type;
import java.math.BigInteger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Use to serialize/deserialize {@link BigInteger} to/from JSON string (gson defaults to number)
 */
public class BigIntegerStringTypeAdapter implements JsonSerializer<BigInteger>, JsonDeserializer<BigInteger> {

  @Override
  public JsonElement serialize(final BigInteger src, final Type typeOfSrc,
      final JsonSerializationContext context) {
    return new JsonPrimitive(String.valueOf(src));
  }

  @Override
  public BigInteger deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context)
      throws JsonParseException {
    return new BigInteger(json.getAsString());
  }
}