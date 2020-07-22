/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.ibm.ioc.ObjectInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Literal Factory
public class LiteralEvaluatorFactory {
  private static final Logger _logger = LoggerFactory.getLogger(LiteralEvaluatorFactory.class);

  private static final Map<String, Constructor<?>> supportedTypes =
      new HashMap<String, Constructor<?>>();

  static {
    try {
      supportedTypes.put("string", StringLiteral.class.getConstructor(String.class));
      supportedTypes.put("int", IntegerLiteral.class.getConstructor(String.class));
      supportedTypes.put("long", LongLiteral.class.getConstructor(String.class));
      supportedTypes.put("short", ShortLiteral.class.getConstructor(String.class));
      supportedTypes.put("byte", ByteLiteral.class.getConstructor(String.class));
      supportedTypes.put("boolean", BooleanLiteral.class.getConstructor(String.class));
      supportedTypes.put("float", FloatLiteral.class.getConstructor(String.class));
      supportedTypes.put("double", DoubleLiteral.class.getConstructor(String.class));
    } catch (final Exception ex) {
      _logger.error("Can't instantiate supported types for literal factory", ex);
    }
  }

  private static class DirectLiteral extends LiteralEvaluator {
    DirectLiteral(final Object value) {
      super(value);
    }
  }

  private LiteralEvaluatorFactory() {}

  /**
   * Checks for a supported type
   * 
   * @param type
   * @return
   */
  public static boolean hasEvaluator(final String type) {
    return supportedTypes.get(type) != null;
  }

  /**
   * Returns a literal of the appropriate type
   * 
   * @param type literal type
   * @param value literal value
   * @return
   */
  public static LiteralEvaluator getEvaluator(final String type, final String value)
      throws ObjectInstantiationException {
    final Constructor<?> literalConstructor = supportedTypes.get(type);
    if (literalConstructor == null) {
      throw new IllegalArgumentException("Literal of type " + type + " is not supported");
    }

    LiteralEvaluator evaluator = null;

    try {
      evaluator = (LiteralEvaluator) literalConstructor.newInstance(value);
    } catch (final Exception e) {
      throw new ObjectInstantiationException("Could not create literal evaluator!", e);
    }
    return evaluator;
  }

  public static LiteralEvaluator getDirectEvaluator(final Object value) {
    return new DirectLiteral(value);
  }
}
