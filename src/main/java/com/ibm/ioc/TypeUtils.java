/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ibm.ioc.impl.NamingHelper;
import com.ibm.ioc.parsers.BigIntegerParser;
import com.ibm.ioc.parsers.IntegerParser;
import com.ibm.ioc.parsers.LocalDateTimeParser;
import com.ibm.ioc.parsers.LocalTimeParser;
import com.ibm.ioc.parsers.LongParser;

// Utilities used for reflection and other class related activities

public final class TypeUtils {
  private static final Logger _logger = LoggerFactory.getLogger(TypeUtils.class);
  public static final String DISALLOW_INVALID_VALUES_TEST_PROPERTY = "configuration.test.disallow-invalid-values";

  public static final Gson gson = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(int.class, IntegerParser.getGsonTypeAdapter())
      .registerTypeAdapter(Integer.class, IntegerParser.getGsonTypeAdapter())
      .registerTypeAdapter(long.class, LongParser.getGsonTypeAdapter())
      .registerTypeAdapter(Long.class, LongParser.getGsonTypeAdapter())
      .registerTypeAdapter(LocalTime.class, LocalTimeParser.getGsonTypeAdapter())
      .registerTypeAdapter(LocalDateTime.class, LocalDateTimeParser.getGsonTypeAdapter())
      .registerTypeAdapter(BigInteger.class, BigIntegerParser.getGsonTypeAdapter())
      .create();

  /**
   * Class collects primitive class information used to cast, print and parse value of primitive types
   */
  public static class PrimitiveTypeInfo {
    private final String name;
    private final Class<?> primitiveClass;
    private final Class<?> wrapperClass;
    private final PrimitiveTypeInfo widensTo;
    @SuppressWarnings("FieldMayBeFinal")
    private Method parseMethod;
    @SuppressWarnings("FieldMayBeFinal")
    private Method printMethod;

    private PrimitiveTypeInfo(final Class<?> wrapperClass, final PrimitiveTypeInfo widensTo) {
      this.wrapperClass = wrapperClass;
      this.widensTo = widensTo;
      try {
        this.primitiveClass = (Class<?>) this.wrapperClass.getDeclaredField("TYPE").get(null);
        this.name = this.primitiveClass.getName();
        if (this.primitiveClass != void.class) {
          if (this.primitiveClass == char.class) {
            this.parseMethod =
                TypeUtils.class.getDeclaredMethod("parseChar", String.class);
          } else {
            this.parseMethod =
                this.wrapperClass.getDeclaredMethod(
                    NamingHelper.convertNameIntoJavaMethod(this.name, "parse", null),
                    String.class);
            this.printMethod =
                this.wrapperClass.getDeclaredMethod("toString", this.primitiveClass);
          }
        }
      } catch (final NoSuchFieldException | NoSuchMethodException e) {
        throw new IllegalStateException("Incorrect type information", e);
      } catch (final IllegalAccessException e) {
        throw new IllegalStateException(e);
      }

    }

    /**
     * Primitive name a type is known (int, boolean etc)
     * 
     * @return
     */
    public String getName() {
      return this.name;
    }

    /**
     * Corresponding primitive type
     * 
     * @return
     */
    public Class<?> getPrimitiveClass() {
      return this.primitiveClass;
    }

    /**
     * Corresponding wrapper class
     * 
     * @return
     */
    public Class<?> getWrapperClass() {
      return this.wrapperClass;
    }

    /**
     * Method to be used to parse it from a string
     * 
     * @return Static method
     */
    public Method getParseMethod() {
      return this.parseMethod;
    }

    /**
     * Method to convert an object of primitive type into string
     * 
     * @return
     */
    public Method getPrintMethod() {
      return this.printMethod;
    }

    /**
     * Verifies whether primitive type is assignable. It is assignable if there is widening to in 1 or more steps int
     * {@literal --> float short --> double ( short --> int --> float --> double)}
     * 
     * @param other the other type
     * @return true if assignable
     */
    public boolean isAssignableFrom(final PrimitiveTypeInfo other) {
      // Find a long widening path.
      for (PrimitiveTypeInfo t = other; t != null; t = t.widensTo) {
        if (t == this) {
          return true;
        }
      }
      return false;
    }

    /**
     * If one is assignable to another
     * 
     * @param other
     * @return
     */
    public boolean isCastableFrom(final PrimitiveTypeInfo other) {
      // Return true if:
      // - there is a "widensTo" path from other to this, or vice-versa,
      // or
      // - there is a "widensTo" path from other and this to a
      // common-type
      // (i.e. the "widensTo" paths from this and other "meet" somewhere)
      // char is castable to short, because char --> int and short --> int
      if (isAssignableFrom(other) || other.isAssignableFrom(this)) {
        return true;
      }
      // No linear path to/from other...
      // If this and other have a widening-path to the same type, then
      // we can cast from this to other (and vice-versa)...
      for (PrimitiveTypeInfo t = this.widensTo; t != null; t = t.widensTo) {
        if (t.isAssignableFrom(other)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Convinience: object of primitive type converted to a java literal as this primitive
     * 
     * @param value to be converted
     * @return java string
     */
    public String printAsJavaLiteral(final Object value) {
      if (value.getClass().isAssignableFrom(this.wrapperClass)) {
        return value.toString();
      }
      throw new RuntimeException("Invalid object type " + value.getClass() + "to generate Java "
          + this.wrapperClass.getName() + " value");
    }

    /**
     * Convinience: string representation to a java literal as this primitive
     * 
     * @param value
     * @return
     */
    public String printAsJavaLiteral(final String value) {
      try {
        return printAsJavaLiteral(getParseMethod().invoke(null, value));
      } catch (final InvocationTargetException ex) {
        throw new IllegalStateException("Can't parse value:" + value + " to "
            + this.wrapperClass.getName() + " type", ex);
      } catch (final IllegalAccessException ex) {
        throw new IllegalStateException("Should not happen!", ex);
      }
    }
  }

  public static final PrimitiveTypeInfo DOUBLE = new PrimitiveTypeInfo(Double.class, null);

  public static final PrimitiveTypeInfo FLOAT = new PrimitiveTypeInfo(Float.class, DOUBLE) {
    @Override
    public String printAsJavaLiteral(final Object value) {
      if (value.getClass().isAssignableFrom(Float.class)) {
        return value.toString() + 'f';
      }
      throw new IllegalStateException("Invalid object type " + value.getClass()
          + "to generate Java float value");
    }
  };

  public static final PrimitiveTypeInfo LONG = new PrimitiveTypeInfo(Long.class, FLOAT) {
    @Override
    public String printAsJavaLiteral(final Object value) {
      if (value.getClass().isAssignableFrom(Long.class)) {
        return value.toString() + 'l';
      }
      throw new IllegalStateException("Invalid object type " + value.getClass()
          + "to generate Java long value");
    }
  };

  public static final PrimitiveTypeInfo INT = new PrimitiveTypeInfo(Integer.class, LONG);

  public static final PrimitiveTypeInfo SHORT = new PrimitiveTypeInfo(Short.class, INT);

  public static final PrimitiveTypeInfo CHAR = new PrimitiveTypeInfo(Character.class, INT) {
    @Override
    public String printAsJavaLiteral(final Object value) {
      if (value.getClass().isAssignableFrom(Character.class)) {
        return "'" + ((Character) value).charValue() + "'";
      }
      throw new IllegalStateException("Invalid object type " + value.getClass()
          + "to generate Java char value");
    }
  };

  public static final PrimitiveTypeInfo BYTE = new PrimitiveTypeInfo(Byte.class, SHORT);

  public static final PrimitiveTypeInfo BOOLEAN = new PrimitiveTypeInfo(Boolean.class, null);

  public static final PrimitiveTypeInfo VOID = new PrimitiveTypeInfo(Void.class, null);

  private static final Map<String, PrimitiveTypeInfo> pnameMap =
      new HashMap<>();

  private static final Map<Class<?>, PrimitiveTypeInfo> wclassMap =
      new HashMap<>();

  static {
    final PrimitiveTypeInfo[] t = {BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, VOID};

    int i;
    for (i = 0; i < t.length; i++) {
      pnameMap.put(t[i].getName(), t[i]);
      wclassMap.put(t[i].getWrapperClass(), t[i]);
    }
  }

  private TypeUtils() {}

  /**
   * PrimitiveInfo by name (int, long), throws if not a correct name
   * 
   * @param name
   * @return
   */
  public static PrimitiveTypeInfo getByPrimitiveName(final String name) {
    final PrimitiveTypeInfo t = findByPrimitiveName(name);
    if (t == null) {
      throw new IllegalArgumentException("Not a primitive type: " + name);
    }
    return t;
  }

  /**
   * PrimitiveInfo by name (int, long)
   * 
   * @param name
   * @return null if not a primitive name
   */
  public static PrimitiveTypeInfo findByPrimitiveName(final String name) {
    return pnameMap.get(name);
  }

  /**
   * PrimitiveInfo by class (int.class, long.class), throws if not a correct name
   * 
   * @param pclass
   * @return
   */
  public static PrimitiveTypeInfo getByPrimitiveClass(final Class<?> pclass) {
    return getByPrimitiveName(pclass.getName());
  }

  /**
   * PrimitiveInfo by class (int.class, long.class)
   * 
   * @param pclass
   * @return null if invalid class
   */
  public static PrimitiveTypeInfo findByPrimitiveClass(final Class<?> pclass) {
    return findByPrimitiveName(pclass.getName());
  }

  /**
   * PrimitiveInfo by wrapper name (Integer, Long)
   * 
   * @param name
   * @return null if doesn't exist
   */
  public static PrimitiveTypeInfo findByWrapperName(final String name) {
    try {
      return findByWrapperClass(Class.forName(name));
    } catch (final ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * PrimitiveInfo by wrapper name (Integer, Long), throws if doesn't exist
   * 
   * @param name
   * @return
   */
  public static PrimitiveTypeInfo getByWrapperName(final String name) {
    final PrimitiveTypeInfo t = findByWrapperName(name);
    if (t == null) {
      throw new IllegalArgumentException("Not a primitive wrapper class: " + name);
    }
    return t;
  }

  /**
   * PrimitiveInfo by wrapper class (Integer.class, Long.class)
   * 
   * @return
   */
  public static PrimitiveTypeInfo getByWrapperClass(final Class<?> wclass) {
    final PrimitiveTypeInfo t = findByWrapperClass(wclass);
    if (t == null) {
      throw new IllegalArgumentException("Not a primitive wrapper class: " + wclass);
    }
    return t;
  }

  public static PrimitiveTypeInfo findByWrapperClass(final Class<?> wclass) {
    return wclassMap.get(wclass);
  }

  /**
   * Checks whether class is primitive
   * 
   * @param pclass
   * @return
   */
  public static boolean isPrimitive(final Class<?> pclass) {
    return findByPrimitiveClass(pclass) != null;
  }

  /**
   * Checks whether name is of a primitive type
   * 
   * @return
   */
  public static boolean isPrimitive(final String name) {
    return findByPrimitiveName(name) != null;
  }

  /**
   * Checks whether type 'to' is assignable 'from' including primitives
   * 
   * @param to
   * @param from
   * @return
   */
  public static boolean isAssignableFrom(final Class<?> to, final Class<?> from) {
    Class<?> primitiveCandidateTo = findPrimitiveFor(to);
    if (primitiveCandidateTo == null) {
      primitiveCandidateTo = to;
    }
    Class<?> primitiveCandidateFrom = findPrimitiveFor(from);
    if (primitiveCandidateFrom == null) {
      primitiveCandidateFrom = from;
    }
    // Choose how to compare, as primitives or as regulat types
    if (primitiveCandidateTo.isPrimitive() && primitiveCandidateFrom.isPrimitive()) {
      return getByPrimitiveClass(primitiveCandidateTo).isAssignableFrom(
          getByPrimitiveClass(primitiveCandidateFrom));
    }
    return getNonPrimitiveFor(to).isAssignableFrom(
        getNonPrimitiveFor(from));
  }

  /**
   * Wrapper class from a primitive type
   * 
   * @param pclass primitive type
   * @return corresponding wrapper
   */
  public static Class<?> getWrapperFor(final Class<?> pclass) {
    return getByPrimitiveClass(pclass).getWrapperClass();
  }

  /**
   * Returns a non primitive type corresponding clazz
   * 
   * @param clazz
   * @return return itself if not a primitive, a wrapper otherwise
   */
  public static Class<?> getNonPrimitiveFor(final Class<?> clazz) {
    if (!clazz.isPrimitive()) {
      return clazz;
    }
    return getWrapperFor(clazz);
  }

  /**
   * Primitive type for a given wrapper class
   * 
   * @param wclass
   * @return
   */
  public static Class<?> getPrimitiveFor(final Class<?> wclass) {
    return getByWrapperClass(wclass).getPrimitiveClass();
  }

  /**
   * Primitive type for a given wrapper class, null if it is not primitive
   * 
   * @return
   */
  public static Class<?> findPrimitiveFor(final Class<?> clazz) {
    if (clazz.isPrimitive()) {
      return clazz;
    }
    final PrimitiveTypeInfo info = findByWrapperClass(clazz);
    if (info != null) {
      return info.getPrimitiveClass();
    }
    return null;
  }

  /**
   * Character requires a special parsing
   * 
   * @param str string top parse from
   * @return character
   */
  public static char parseChar(final String str) {
    if (str.length() == 0) {
      return '\0';
    } else if (str.length() > 1) {
      throw new IllegalArgumentException("String too long to convert to char: " + str);
    } else {
      return str.charAt(0);
    }
  }

  /**
   * Convinienece method to create list of types
   * 
   * @param args
   * @return
   */
  public static String listOfClassesToStr(final Class<?>[] args) {
    final StringBuilder res = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        res.append(", ");
      }
      res.append(args[i].getName());
    }
    return res.toString();
  }

  /**
   * Tries to find the first signature that could be invoked without run-time error This is not ideal but seems to be
   * quite adequate
   * 
   * @param clazz Class upon which a method is looked
   * @param methodName Method name
   * @param parameterTypes argument types
   * @param includeStatic include or not static methdods in search
   * @return Method object if found null otherwise
   */
  public static Method getMatchingMethod(
      final Class<?> clazz,
      final String methodName,
      final Class<?>[] parameterTypes,
      final boolean includeStatic) {
    final Method[] allMethods = clazz.getMethods();
    for (final Method method : allMethods) {
      if (!method.getName().equals(methodName)) {
        continue;
      }
      if (!includeStatic && Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      // Check wheather each parameter is assignable
      if (parameterTypes.length != method.getParameterTypes().length) {
        continue;
      }
      int i;
      for (i = 0; i < parameterTypes.length; i++) {
        if (!isAssignableFrom(method.getParameterTypes()[i], parameterTypes[i])) {
          break;
        }
      }
      if (i == parameterTypes.length) {
        return method;
      }
    }
    return null;
  }

  public static <T> List<T> createListOfType() {
    return new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  /**
   * Creates and populates list from initializers, return null if conversion fails.
   * 
   * @param type to be converted to
   * @param initialzers list of string with values
   * @return ;ist of specified type
   * @throws IllegalArgumentException
   */
  public static <T> List<T> createListOfType(final Class<T> type, final List<String> initialzers)
      throws IllegalArgumentException {
    final List<T> list = new ArrayList<>(initialzers.size());
    for (final String s : initialzers) {
      final Object obj = createObjectFromString(type, s);
      if (obj == null) {
        throw new IllegalArgumentException(
            String.format("Failed to convert %s to type %s, list initialziers %s", s, type, initialzers));
      }
      list.add((T) obj);
    }
    return list;
  }

  @SuppressWarnings({"unchecked"})
  public static Object createObjectFromString(final Type type, final String stringValue) {
    if (type.equals(String.class)) {
      return stringValue;
    }
    if (stringValue == null || stringValue.isEmpty()) {
      return null;
    }

    final Class<?> rawType = getRawType(type);

    final Class<?> wrapperType = getNonPrimitiveFor(rawType);

    Object convertedObject = null;
    Method conversionMethod;

    final String[] stringConversionSuspects = new String[] {"valueOf", "fromString", "decode"};
    final List<String> caseCandidates;
    if (rawType.isEnum()) {
      caseCandidates =
          Arrays.asList(stringValue, stringValue.toUpperCase(),
              stringValue.toLowerCase(),
              NamingHelper.convertNameIntoJava(stringValue, null, null, true));
    } else {
      caseCandidates = Arrays.asList(stringValue);
    }
    for (final String conversionSuspect : stringConversionSuspects) {
      for (final String val : caseCandidates) {
        try {
          conversionMethod = wrapperType.getMethod(conversionSuspect, String.class);
          if (conversionMethod != null && Modifier.isStatic(conversionMethod.getModifiers())
              && isAssignableFrom(rawType, conversionMethod.getReturnType())) {
            convertedObject = conversionMethod.invoke(null, val);
          }
        } catch (final Exception ignore) {
          // Ignore
        }
        if (convertedObject != null) {
          break;
        }
      }
      if (convertedObject != null) {
        break;
      }
    }
    if (convertedObject == null) {
      // Try to find a helper parser class for a given type
      final String fullClassName = wrapperType.getName();
      final String[] splitClassName = fullClassName.split("\\.");
      final String helperParserClassName =
          "com.ibm.ioc.parsers." + splitClassName[splitClassName.length - 1]
              + "Parser";

      try {
        final Class<?> parser = Class.forName(helperParserClassName);
        conversionMethod = parser.getDeclaredMethod("parse", String.class);
        if (conversionMethod != null && Modifier.isStatic(conversionMethod.getModifiers())
            && isAssignableFrom(rawType, conversionMethod.getReturnType())) {
          convertedObject = conversionMethod.invoke(null, stringValue);
        }
      } catch (final Exception ignore) {
        convertedObject = null;
      }
    }

    // If still null, try converting from json
    if (convertedObject == null) {
      try {
        convertedObject = gson.fromJson(stringValue, type);
      } catch (final JsonSyntaxException e) {
        // Ignore
      } catch (final RuntimeException e) {
        _logger.error("Gson threw a runtime exception other than JsonSyntaxException - BAD GSON",
            e);
      }
    }

    // If still null, and expected type is a List, try converting from a complex list type
    if (convertedObject == null && type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      final Type listGenericType = parameterizedType.getActualTypeArguments()[0];

      // Skip List<String> because that should already be evaluated directly from gson
      if (parameterizedType.getRawType() == List.class && listGenericType != String.class) {
        try {
          class InnerClass {
            @SuppressWarnings("unused")
            public List<String> stringList;
          }
          final Type listType;
          try {
            listType = InnerClass.class.getField("stringList").getGenericType();
          } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
          }

          final List<String> stringList = gson.fromJson(stringValue, listType);

          @SuppressWarnings("rawtypes")
          final List list = createListOfType();
          boolean validList = true;
          for (final String listItemStr : stringList) {
            final Object obj = createObjectFromString(listGenericType, listItemStr);
            if (obj == null) {
              validList = false;
              break;
            }
            list.add(obj);
          }
          if (validList) {
            convertedObject = list;
          }
        } catch (final JsonSyntaxException e) {
          // Ignore
        }
      }
    }

    // Finally just try a constructor
    // This is purposely the last try because we prefer to try other things first (see above)
    if (convertedObject == null) {
      // Try if constructor exists
      try {
        final Constructor<?> constructor = wrapperType.getConstructor(String.class);
        if (constructor != null) {
          constructor.setAccessible(true);
          convertedObject = constructor.newInstance(stringValue);
        }
      } catch (NoSuchMethodException | SecurityException | InstantiationException
          | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
        // Ignore
      }

    }

    if (convertedObject == null && wrapperType.isAssignableFrom(String.class)) {
      convertedObject = stringValue;
    }

    // If this is a bindings test where strict default properties are checked
    // and the string value couldn't be converted to the expected object type, OR
    // the expected object type is a boolean but the string value is not "true" or "false",
    // then throw an IllegalArgumentException
    if (Boolean.valueOf(System.getProperty(DISALLOW_INVALID_VALUES_TEST_PROPERTY)) &&
        (convertedObject == null || (Boolean.class.equals(wrapperType) && !isValidBoolean(stringValue)))) {
      throw new IllegalArgumentException(
          new IllegalConfigurationContentException("Could not convert \"" + stringValue + "\" to " + type));
    }

    return convertedObject;
  }

  private static boolean isValidBoolean(final String string) {
    final String lowerCase = string.toLowerCase();
    return Boolean.toString(true).equals(lowerCase) ||
        Boolean.toString(false).equals(lowerCase);
  }

  public static Class<?> getRawType(final Type type) {
    return (Class<?>) ((type instanceof ParameterizedType)
        ? ((ParameterizedType) type).getRawType()
        : type);
  }

  /**
   * Returns a generic type of a given type and the generic class. A type could be implementing multiple interfaces,
   * each genericised with different types. This is why the generic class is required to determine the generic type.
   * 
   * Examples:
   * 
   * type = {@literal List<Integer>}, genericClass = List. Returns Integer.
   * 
   * type = IntegerList (extends {@literal List<Integer>}), genericClass = List. Returns Integer.
   * 
   * @param type
   * @param genericClass
   * @return
   */
  public static Type[] getGenericTypes(
      final Type type,
      final Class<?> genericClass) {
    final Class<?> rawType = getRawType(type);
    if (genericClass.isAssignableFrom(getRawType(type))) {
      // If the type is a GenericClass<B,C>, return B,C
      if (type instanceof ParameterizedType) {
        return ((ParameterizedType) type).getActualTypeArguments();
      } else {
        // If the type is a Something extends GenericClass<B,C>, return B,C
        final Type[] types = rawType.getGenericInterfaces();
        for (final Type t : types) {
          final Type[] genericTypes = getGenericTypes(t, genericClass);
          if (genericTypes.length > 0) {
            return genericTypes;
          }
        }
      }
    }
    return new Type[0];
  }
}
