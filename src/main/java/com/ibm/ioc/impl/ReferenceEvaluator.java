/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ibm.ioc.Annotations.RequireParameterBinding;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Evaluator that represents a reference to an object
public class ReferenceEvaluator<T> implements Evaluatable {
  private static final Logger _logger = LoggerFactory.getLogger(ReferenceEvaluator.class);

  private T referenced = null;
  private List<NamedEvaluator> params = null;
  private final Set<String> uncalledRequiredSetterMethods;

  /**
   * @param referenced
   * @param params
   */
  public ReferenceEvaluator(final T referenced, final List<NamedEvaluator> params) {
    super();
    this.referenced = referenced;
    this.params = params;
    this.uncalledRequiredSetterMethods = new HashSet<>();
    for (final Method method : referenced.getClass().getMethods()) {
      if (method.isAnnotationPresent(RequireParameterBinding.class)) {
        this.uncalledRequiredSetterMethods.add(method.getName());
      }
    }

  }

  private boolean noPrivateRequiredSetterMethods() throws ObjectInitializationException {
    final List<Method> allMethods = new ArrayList<>();
    Class<?> cls = this.referenced.getClass();
    while (cls != null) {
      allMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
      cls = cls.getSuperclass();
    }
    final List<Method> annotatedPrivateMethods = allMethods.stream()
        .filter(method -> method.isAnnotationPresent(RequireParameterBinding.class)
            && !Modifier.isPublic(method.getModifiers()))
        .collect(Collectors.toList());
    if (!annotatedPrivateMethods.isEmpty()) {
      throw new ObjectInitializationException("One or more private methods on " + this.referenced.getClass()
          + " have annotation @" + RequireParameterBinding.class.getSimpleName() + ": "
          + annotatedPrivateMethods + ".  All annotated methods must be public.");
    }
    return true;
  }

  @Override
  public T evaluate(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    if (_logger.isTraceEnabled()) {
      _logger.trace("Initializaing object of type " + this.referenced.getClass());
    }

    if (this.params != null) {
      setParameters(overrides);
    }

    assert noPrivateRequiredSetterMethods();

    if (!this.uncalledRequiredSetterMethods.isEmpty()) {
      throw new ObjectInitializationException(
          "One or more required methods on " + this.referenced.getClass()
              + " have annotation @" + RequireParameterBinding.class.getSimpleName()
              + " but were not called during initialization: "
              + this.uncalledRequiredSetterMethods);
    }
    return this.referenced;
  }

  @Override
  public T evaluate() throws ObjectInitializationException {
    return evaluate(Collections.emptyMap());
  }

  @Override
  public Set<Class<?>> getDefaultRefs(final Map<Class<?>, String> overrides) {
    final Set<Class<?>> set = new HashSet<>();
    if (this.params != null) {
      for (final NamedEvaluator param : this.params) {
        set.addAll(param.getDefaultRefs(overrides));
      }
    }
    return set;
  }

  private void setParameters(final Map<Class<?>, String> overrides)
      throws ObjectInitializationException {
    for (final NamedEvaluator param : this.params) {
      if (param.getName().length() == 0) {
        throw new IllegalArgumentException("Empty parameter name for "
            + this.referenced.getClass().getName());
      }

      // Evaluate the parameter and set it on the referenced object
      Object paramEval = null;

      try {
        paramEval = param.evaluate(overrides);
      } catch (final ObjectInitializationException e) {
        // May throw if parameter is required
        logAndAct(param, "Exception evaluating parameter " + param.getName() + " for "
            + this.referenced.getClass().getName(), e, false);
        continue;
      }

      final StringBuilder parameterDescription = new StringBuilder();

      try {
        try {
          tryCallMethod(param, paramEval, parameterDescription);
        } catch (final NoSuchMethodException ex) {
          try {
            trySetField(param, paramEval, parameterDescription);
          } catch (final NoSuchFieldException e) {
            String whatHappened =
                "Failed to find match for method or field in " + this.referenced.getClass()
                    + " to set '" + param.getName() + "'";
            if (paramEval != null) {
              whatHappened += " (for parameter " + paramEval.toString() + ")";
            } else {
              whatHappened += " parameter can't be evaluated";
            }
            // We could allow this to happen if a parameter is not required
            logAndAct(param, whatHappened, null, true);
          }
        }
      } catch (final Exception ex) {
        logAndAct(param, "Failed to set parameter " + param.getName() + " while configuring "
            + this.referenced.getClass().getName() + " Details: " + parameterDescription, ex,
            true);
      }
    }
  }

  private void tryCallMethod(
      final NamedEvaluator param,
      final Object paramEval,
      final StringBuilder parameterDescription) throws NoSuchMethodException,
      ObjectInitializationException {
    final String methodName = NamingHelper.convertNameIntoJavaSetter(param.getName());

    final Method[] methods = this.referenced.getClass().getMethods();
    final AtomicReference<ObjectInitializationException> ex = new AtomicReference<>(null);
    for (final Method method : methods) {
      if (method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
        // Found a proper method with a single argument
        final Type type = method.getGenericParameterTypes()[0];
        final Type defaultType = getDefaultType();
        final Modifiable<Object> mutableParam =
            ModifiableUtils.createModifiable(defaultType == null ? type : defaultType,
                paramEval);

        this.uncalledRequiredSetterMethods.remove(method.getName());

        mutableParam.addModificationListener(value -> {
          if (value != null) {
            describeScalarParameter(value.toString(), parameterDescription, type);

            try {
              try {
                method.setAccessible(true);
              } catch (final SecurityException ignore) {
              }
              method.invoke(ReferenceEvaluator.this.referenced, value);
              if (_logger.isTraceEnabled()) {
                _logger.trace("Parameter " + param.getName() + " in reference "
                    + ReferenceEvaluator.this.referenced.getClass().getName()
                    + " resolved by "
                    + methodName + "(" + parameterDescription.toString() + ")");
              }
            } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
              _logger.error("Could not call method " + method + " on object "
                  + ReferenceEvaluator.this.referenced, e);
              ex.set(new ObjectInitializationException(e));
            }

          } else {
            // method scalar and no value for non-primitive parameter
            if ((type instanceof Class && !((Class<?>) type).isPrimitive())
                || (type instanceof ParameterizedType
                    && ((ParameterizedType) type).getRawType() instanceof Class
                    && !((Class<?>) ((ParameterizedType) type).getRawType()).isPrimitive())) {
              _logger.info("Attempt to resolve " + param.getName()
                  + " for reference "
                  + ReferenceEvaluator.this.referenced.getClass().getName()
                  + " to null value");
              try {
                try {
                  method.setAccessible(true);
                } catch (final SecurityException ignore) {
                }
                method.invoke(ReferenceEvaluator.this.referenced, new Object[] {null});
              } catch (IllegalAccessException | IllegalArgumentException
                  | InvocationTargetException e) {
                _logger.error("Could not call method " + method + " on object "
                    + ReferenceEvaluator.this.referenced, e);
                ex.set(new ObjectInitializationException(e));
              }
            } else {
              final String error =
                  "null value attempted to be set for primitive type "
                      + ReferenceEvaluator.this.referenced.getClass() + "."
                      + methodName;
              _logger.warn(error);
              // If paramEval is not null but value is null, it means we couldn't
              // evaluate - we should throw in this case
              if (paramEval != null || param.isRequired()) {
                ex.set(new ObjectInitializationException(error));
              }
            }
          }
        });

        if (ex.get() != null) {
          throw ex.get();
        }
        return; // Return if method is found
      }
    }

    throw new NoSuchMethodException(methodName);
  }

  private String trySetField(
      final NamedEvaluator param,
      final Object paramEval,
      final StringBuilder parameterDescription) throws IllegalAccessException,
      SecurityException, ObjectInitializationException, NoSuchFieldException {
    final String fieldName = NamingHelper.convertNameIntoJavaField(param.getName());
    final Field field = this.referenced.getClass().getField(fieldName);
    // Found a proper method with a single argument
    final Type defaultType = getDefaultType();
    final Type type = field.getGenericType();
    final Class<?> rawType = field.getType();

    // Support for Observables - only for fields, not setter methods
    if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      if (parameterizedType.getActualTypeArguments().length == 1) {
        final Type genericType = parameterizedType.getActualTypeArguments()[0];

        if (!(genericType instanceof WildcardType) && !(genericType instanceof TypeVariable)) {
          try {
            final Constructor<?> constructor =
                rawType.getConstructor(TypeUtils.getRawType(rawType.getTypeParameters()[0].getBounds()[0]));
            final Type declaredGenericType = constructor.getGenericParameterTypes()[0];
            try {
              final Method[] allMethods = rawType.getMethods();
              Method updateMethod = null;
              int foundMethods = 0;
              if (allMethods.length > 0) {
                for (final Method m : allMethods) {
                  if (m.getParameters().length == 1
                      && m.getGenericParameterTypes()[0].equals(declaredGenericType)
                      && m.getReturnType().equals(void.class)) {
                    updateMethod = m;
                    foundMethods++;
                  }
                }
              }
              if (foundMethods == 1 && updateMethod != null) {
                _logger.info(
                    "Found exactly one update method {}.{} for {} in {}- will use this for change notifications",
                    rawType, updateMethod, field.getName(), this.referenced.getClass());
                final Object valueOfGenericType = convertToType(genericType, paramEval);
                final Object finalValue = constructor.newInstance(valueOfGenericType);

                final Method finalUpdateMethod = updateMethod;
                if (paramEval instanceof Modifiable) {
                  final Modifiable<?> modifiable = (Modifiable<?>) paramEval;
                  modifiable.addModificationListener(newValue -> {
                    if (newValue == null) {
                      try {
                        try {
                          finalUpdateMethod.setAccessible(true);
                        } catch (final SecurityException ignore) {
                        }
                        finalUpdateMethod.invoke(finalValue, (Object) null);
                      } catch (IllegalAccessException | IllegalArgumentException
                          | InvocationTargetException e) {
                        throw new RuntimeException(e);
                      }
                    } else {
                      final Object newValueOfGenericType = convertToType(genericType, newValue);
                      try {
                        try {
                          finalUpdateMethod.setAccessible(true);
                        } catch (final SecurityException ignore) {
                        }
                        finalUpdateMethod.invoke(finalValue, newValueOfGenericType);
                      } catch (IllegalAccessException | IllegalArgumentException
                          | InvocationTargetException e) {
                        _logger.error("Could not update {} {} value to {} on {}", rawType,
                            genericType, newValue, this.referenced);
                      }
                    }
                  });
                }
                field.set(this.referenced, finalValue);
                return fieldName;
              }
            } catch (InstantiationException | IllegalArgumentException
                | InvocationTargetException ignore) {
            }
          } catch (final ClassCastException | NoSuchMethodException ignore) {
          }
        }
      }
    }

    // method scalar and parameter scalar
    final Object scalarValue =
        convertToType(defaultType == null ? type : defaultType, paramEval);
    if (scalarValue != null) {
      describeScalarParameter(paramEval.toString(), parameterDescription, rawType);
      try {
        field.setAccessible(true);
      } catch (final SecurityException ignore) {
      }
      field.set(this.referenced, scalarValue);
      if (_logger.isDebugEnabled()) {
        _logger.trace("Parameter " + param.getName() + " in reference "
            + this.referenced.getClass().getName() + " resolved by "
            + field.getType().getName() + "'(" + parameterDescription.toString() + ")");
      }
      return fieldName;
    } else {
      logAndAct(param, param.getName() + " for reference "
          + this.referenced.getClass().getName() + " is not resolved, will not be set",
          null, false);
    }

    return null;
  }

  private void describeScalarParameter(
      final String scalarValueDescription,
      final StringBuilder parameterDescription,
      final Type type) {
    final String typeStr = type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    parameterDescription.append(typeStr).append(":").append(scalarValueDescription);
  }

  private Object checkAndCreateSupplierIfPossible(final Type type, final Class<?> rawType,
      final Object originalValue) {
    // We create our own suppliers only for .prop, which are always Modifiable
    // .ref and .set will be evaluated on their own as direct matches
    if (!(originalValue instanceof Modifiable)) {
      return null;
    }
    if (Supplier.class.equals(rawType) && type instanceof ParameterizedType) {
      // Get the Supplier generic type
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      if (parameterizedType.getActualTypeArguments().length == 1) {
        final Type supplierType = parameterizedType.getActualTypeArguments()[0];
        final Modifiable<?> supplierModifiable =
            ModifiableUtils.createModifiable(supplierType, originalValue);
        return (Supplier<?>) supplierModifiable::get;
      }
    }

    // Check if type is a primitive int supplier
    if (IntSupplier.class.equals(rawType)) {
      final Modifiable<?> supplierModifiable =
          ModifiableUtils.createModifiable(Integer.class, originalValue);
      return (IntSupplier) () -> {
        final Integer value;
        return (value = (Integer) supplierModifiable.get()) == null ? 0 : value;
      };
    }

    // Check if type is a primitive long supplier
    if (LongSupplier.class.equals(rawType)) {
      final Modifiable<?> supplierModifiable =
          ModifiableUtils.createModifiable(Long.class, originalValue);
      return (LongSupplier) () -> {
        final Long value;
        return (value = (Long) supplierModifiable.get()) == null ? 0L : value;
      };
    }

    // Check if type is a primitive double supplier
    if (DoubleSupplier.class.equals(rawType)) {
      final Modifiable<?> supplierModifiable =
          ModifiableUtils.createModifiable(Double.class, originalValue);
      return (DoubleSupplier) () -> {
        final Double value;
        return (value = (Double) supplierModifiable.get()) == null ? 0D : value;
      };
    }

    // Check if type is a primitive boolean supplier
    if (BooleanSupplier.class.equals(rawType)) {
      final Modifiable<?> supplierModifiable =
          ModifiableUtils.createModifiable(Boolean.class, originalValue);
      return (BooleanSupplier) () -> {
        final Boolean value;
        return (value = (Boolean) supplierModifiable.get()) == null ? false : value;
      };
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Object convertToType(final Type type, final Object originalValue) {
    Object typedValue = null;
    if (originalValue == null) {
      return null;
    }

    final Class<?> rawType = TypeUtils.getRawType(type);

    // If type is Modifiable, create modifiable
    if (Modifiable.class.isAssignableFrom(rawType)) {
      typedValue = ModifiableUtils.createModifiable(type, originalValue);
    }

    // If type is a Supplier, create a Supplier from the originalValue
    if (typedValue == null) {
      typedValue = checkAndCreateSupplierIfPossible(type, rawType, originalValue);
    }

    // Else if original value can be directly assigned, assign it
    if (typedValue == null && TypeUtils.isAssignableFrom(rawType, originalValue.getClass())) {
      typedValue = assignScalarOrArray(rawType, originalValue);
    }

    // Else if original value is a modifiable which can be unwrapped and directly assigned, unwrap
    // and assign it
    if (typedValue == null && originalValue instanceof Modifiable<?>) {
      final Object actualValue = ((Modifiable<?>) originalValue).get();
      if (actualValue != null) {
        typedValue = assignScalarOrArray(rawType, actualValue);
      }
    }
    // Else try conversion from string
    if (typedValue == null && originalValue instanceof String) {
      typedValue = TypeUtils.createObjectFromString(type, (String) originalValue);
    }
    // Else if original value is a modifiable, unwrap and try conversion from string
    if (typedValue == null && originalValue instanceof Modifiable
        && ((Modifiable<Object>) originalValue).get() instanceof String) {
      typedValue =
          TypeUtils.createObjectFromString(type,
              (String) ((Modifiable<Object>) originalValue).get());
    }
    return typedValue;
  }

  private Object assignScalarOrArray(
      final Class<?> rawType,
      final Object actualValue) {
    final Object typedValue;
    if (TypeUtils.isAssignableFrom(rawType, actualValue.getClass())) {
      typedValue = actualValue;
    } else if (actualValue.getClass().isArray()) {
      typedValue = makeTypedArray((Object[]) actualValue, rawType.getComponentType());
    } else {
      typedValue = null;
    }
    return typedValue;
  }

  private Object makeTypedArray(
      final Object[] param,
      final Class<?> componentType) {
    final Object arrayType = Array.newInstance(componentType, param.length);
    int index = 0;
    for (final Object o : param) {
      final Object arrayValue = convertToType(componentType, o);
      if (arrayValue == null) {
        return null;
      }
      Array.set(arrayType, index, arrayValue);
      index++;
    }
    return arrayType;
  }

  protected Type getDefaultType() {
    return null;
  }

  private void logAndAct(
      final NamedEvaluator param,
      final String whatHappened,
      final Exception ex,
      final boolean unconditionalException) throws ObjectInitializationException {
    if (unconditionalException || param.isRequired()) {
      _logger.error(whatHappened, ex);
      if (ex != null) {
        throw new ObjectInitializationException(whatHappened, ex);
      } else {
        throw new ObjectInitializationException(whatHappened);
      }
    } else {
      _logger.debug(whatHappened, ex);
    }
  }

  @Override
  public String toString() {
    if (this.referenced != null) {
      return this.referenced.toString();
    } else {
      return "<ReferenceEvaluator null>";
    }
  }
}
