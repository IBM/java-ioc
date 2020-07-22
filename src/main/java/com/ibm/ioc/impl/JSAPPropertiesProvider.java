/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.ioc.ConfigurationItemNotDefinedException;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.UnflaggedOption;

public class JSAPPropertiesProvider extends PropertiesProviderBase {
  private final JSAPResult results;
  private final JSAP parser;

  public JSAPPropertiesProvider(final JSAPResult results, final JSAP parser) {
    this.results = results;
    this.parser = parser;
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    return this.results.contains(qualifiedName);
  }

  @Override
  public Object getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException {
    if (this.results.contains(qualifiedName)) {
      final Parameter parameter = this.parser.getByID(qualifiedName);
      if (parameter instanceof UnflaggedOption && ((UnflaggedOption) parameter).isGreedy()) {
        return this.results.getObjectArray(qualifiedName);
      }
      if (parameter instanceof Option && ((Option) parameter).isList()) {
        return this.results.getObjectArray(qualifiedName);
      }

      return this.results.getObject(qualifiedName);
    } else {
      throw new ConfigurationItemNotDefinedException(qualifiedName);
    }
  }

  @Override
  public Set<String> getQualifiedNames() {
    final Set<String> names = new HashSet<String>();
    final Iterator<?> i = this.parser.getIDMap().idIterator();
    while (i.hasNext()) {
      names.add(i.next().toString());
    }
    return names;
  }
}
