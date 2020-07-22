/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

/**
 * Static helper methods used to convert configuration and property names into java names
 */
public class NamingHelper {
  /**
   * Returns a Java-like setter method string. Prepends "set" to the a Java-like method created by capitalizing the
   * parameter and any character following a hyphen and discarding the hyphens. For example: <code>block-size</code>
   * becomes <code>setBlockSize</code>.
   * 
   * @param name a name to convert
   * @return a Java-like setter method string
   * @see #convertNameIntoJavaMethod
   */
  public static String convertNameIntoJavaSetter(final String name) {
    return convertNameIntoJavaMethod(name, "set", "");
  }

  public static String convertNameIntoJavaMethod(
      final String name,
      final String prefix,
      final String suffix) {
    return convertNameIntoJava(name, prefix, suffix, true);

  }

  public static String convertNameIntoJavaField(final String name) {
    return convertNameIntoJava(name, null, null, false);
  }

  /**
   * Converts a string to a Java-like method name. It capitalizes every word that results from spliting the name at
   * every hyphen. It also prepends a prefix and appends a suffix.
   * <p>
   * For example:
   * <ul>
   * <li><code>convertNameIntoJavaMethod("method-name","pre","suf")</code> would become
   * <code>preMethodNamesuf</code></li>
   * <li><code>convertNameIntoJavaMethod("method-name","pre",null)</code> would become <code>preMethodName</code></li>
   * <li><code>convertNameIntoJavaMethod("method-name",null,null)</code> would become <code>MethodName</code></li>
   * </ul>
   * 
   * @param name a name to convert
   * @param prefix the prefix to prepend
   * @param suffix the suffix to append
   * @return a Java-like method string
   */
  // TODO: This method returns a capitalized method name when the prefix is null.
  // TODO: The suffix is not capitalized which might be expected by users calling this method.
  public static String convertNameIntoJava(
      final String name,
      final String prefix,
      final String suffix,
      final boolean capitalize) {
    assert name != null && name.length() > 0 : "Name should have at least one character";

    final String[] parts = name.split("-");
    final StringBuilder outputName = new StringBuilder();

    assert (parts.length > 0);

    if (prefix != null) {
      outputName.append(prefix);
    }
    if (parts[0].length() > 0) {
      if (capitalize) {
        outputName.append(parts[0].substring(0, 1).toUpperCase()).append(parts[0].substring(1));
      } else {
        outputName.append(parts[0].substring(0, 1)).append(parts[0].substring(1));
      }
    } else {
      // skip first '-'
    }
    for (int i = 1; i < parts.length; i++) {
      if (parts[i].length() > 0) {
        outputName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
      } else {
        // skip multiple '-'
      }
    }

    if (suffix != null) {
      outputName.append(suffix);
    }
    return outputName.toString();
  }
}
