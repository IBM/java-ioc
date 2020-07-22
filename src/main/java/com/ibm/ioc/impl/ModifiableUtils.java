/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Type;

import com.ibm.ioc.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ModifiableUtils {
  private static final Logger _logger = LoggerFactory.getLogger(ModifiableUtils.class);

  private static class NonModifiable<T> implements Modifiable<T> {
    private final T value;

    NonModifiable(final T initialValue) {
      this.value = initialValue;
    }

    @Override
    public T get() {
      return this.value;
    }

    @Override
    public void addModificationListener(final ModificationListener<T> listener) {}

    @Override
    public String toString() {
      return String.valueOf(this.value);
    }
  }

  private static Object convertOrConform(final Type type, final Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return TypeUtils.createObjectFromString(type, (String) value);
    } else if (TypeUtils.isAssignableFrom(TypeUtils.getRawType(type), value.getClass())
        || TypeUtils.getRawType(type).isInstance(value)) {
      return value;
    }
    return null;
  }

  private static Modifiable<Object> createModifiableImpl(
      final Type type,
      final Modifiable<Object> originalValue) {
    final Type modifiableType =
        Modifiable.class.isAssignableFrom(TypeUtils.getRawType(type))
            ? TypeUtils.getGenericTypes(type, Modifiable.class)[0]
            : type;
    final Object typedValue;

    // try convert from a string
    typedValue = convertOrConform(modifiableType, originalValue.get());

    final ModifiableImpl<Object> modifiableObject = new ModifiableImpl<>(typedValue);
    originalValue.addModificationListener(newValue -> {
      if (newValue == null) {
        modifiableObject.set(null);
      } else {
        final Object newTypedValue =
            convertOrConform(modifiableType, newValue);

        if (newTypedValue != null || ((newValue instanceof String) && ((String) newValue).isEmpty())) {
          modifiableObject.set(newTypedValue);
        } else {
          _logger.warn("Cannot modify "
              + type
              + " containing existing value "
              + typedValue
              + " with new value " + newValue + ", which cannot be evaluated to "
              + modifiableType);
        }
      }
    });
    return modifiableObject;
  }

  @SuppressWarnings("unchecked")
  static Modifiable<Object> createModifiable(
      final Type type,
      final Object originalValue) {

    if (originalValue instanceof Modifiable) {
      return createModifiableImpl(type, (Modifiable<Object>) originalValue);
    } else {
      return createModifiableImpl(type, new NonModifiable<>(originalValue));
    }
  }
}
