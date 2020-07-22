/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.PropertiesProvider;
import com.ibm.ioc.PropertiesResolver;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;

/**
 * Supports automatic prefix substitution
 */
public class OrderedPropertiesResolver extends PropertiesProviderBase
    implements PropertiesResolver {
  private abstract static class TypedInvoker<T> implements Callable<T> {
    private String propertyName;

    private PropertiesProvider provider;

    public TypedInvoker() {}

    void setParameters(final String propertyName, final PropertiesProvider provider) {
      this.propertyName = propertyName;
      this.provider = provider;
    }

    public String getPropertyName() {
      return this.propertyName;
    }

    public PropertiesProvider getProvider() {
      return this.provider;
    }
  }

  private final SortedMap<Integer, String> providerTypes = new TreeMap<>(
      Collections.reverseOrder());

  private final Map<String, PropertiesProvider> providers =
      new HashMap<>();

  private final List<String> defaultPrefixes = new ArrayList<>();
  private final List<String> nonSubstituablePrefixes = new ArrayList<>();
  private final List<Function<String, String>> caseCombinations = Arrays.asList(
      (final String name) -> name,
      (final String name) -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name),
      (final String name) -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name),
      (final String name) -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name),
      (final String name) -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, name),
      (final String name) -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name),
      (final String name) -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name));

  @Override
  public void announcePropertiesProvider(final String type, final int order) {
    if (order < 0 || order > 255) {
      throw new IllegalArgumentException("order should be non negative and smaller then 256");
    }

    final Iterator<Entry<Integer, String>> it = this.providerTypes.entrySet().iterator();
    // Remove if exists
    while (it.hasNext()) {
      final Entry<Integer, String> s = it.next();
      if (s.getValue().equals(type)) {
        it.remove();
        break;
      }
    }
    this.providerTypes.put(order, type);
  }

  @Override
  public String[] getAnnouncedProperties() {
    final String[] announcedProperties;
    // Empty case
    if (this.providerTypes.isEmpty()) {
      announcedProperties = new String[0];
    } else {
      // Non-empty
      final Iterator<Entry<Integer, String>> it = this.providerTypes.entrySet().iterator();
      final Entry<Integer, String> max = it.next();
      assert max.getKey() >= 0 && max.getKey() < 256;
      announcedProperties = new String[max.getKey() + 1];

      announcedProperties[max.getKey()] = max.getValue();
      while (it.hasNext()) {
        final Entry<Integer, String> s = it.next();
        announcedProperties[s.getKey()] = s.getValue();
      }
    }
    return announcedProperties;
  }

  @Override
  public void registerPropertiesProvider(final String type, final PropertiesProvider provider) {
    if (findTypeOrder(type) == -1) {
      throw new IllegalArgumentException("property of type is not announced");
    }
    if (provider != null) {
      provider.addModificationListener(() -> {
        reloadResolvedProperties();
        notifyListeners();
      });
      this.providers.put(type, provider);
    } else {
      this.providers.remove(type);
    }
  }

  @Override
  public int findTypeOrder(final String type) {
    final Iterator<Entry<Integer, String>> it = this.providerTypes.entrySet().iterator();
    // Remove if exists
    while (it.hasNext()) {
      final Entry<Integer, String> s = it.next();
      if (s.getValue().equals(type)) {
        return s.getKey();
      }
    }
    return -1;
  }

  @Override
  public String[] getAnnouncedTypes() {
    return this.providerTypes.values().toArray(new String[this.providerTypes.size()]);
  }

  @Override
  public PropertiesProvider getPropertiesProvider(final String type) {
    if (findTypeOrder(type) == -1) {
      throw new IllegalArgumentException("property of type is not announced");
    }
    return this.providers.get(type);
  }

  /*
   * *
   * 
   * @param prefix
   */
  public void addDefaultPrefix(final String prefix) {
    this.defaultPrefixes.add(prefix);
  }

  /*
   * *
   * 
   * @param collection of prefixes
   */
  public void setDefaultPrefixes(final Collection<String> allPrefixes) {
    this.defaultPrefixes.clear();
    this.defaultPrefixes.addAll(allPrefixes);
  }

  public void setIgnorablePrefixes(final Collection<String> ignorablePrefixes) {
    this.nonSubstituablePrefixes.clear();
    this.nonSubstituablePrefixes.addAll(ignorablePrefixes);
  }

  /*
   * *
   * 
   * @return
   */
  public List<String> getDefaultPrefixes() {
    return Collections.unmodifiableList(this.defaultPrefixes);
  }

  public List<String> getNonSubstituablePrefixes() {
    return Collections.unmodifiableList(this.nonSubstituablePrefixes);
  }

  private <T> T iterateAndGet(final String name, final TypedInvoker<T> invoker) {
    for (final String type : this.providerTypes.values()) {
      final List<String> triedNames = Lists.newArrayList();
      final PropertiesProvider pp = this.providers.get(type);
      for (final Function<String, String> caseCombinationFunction : this.caseCombinations) {
        final String nameVariation = caseCombinationFunction.apply(name);
        // check is relatively expensive, there will be repetitions among variations
        if (triedNames.contains(nameVariation)) {
          continue;
        }
        triedNames.add(nameVariation);
        if (pp != null) {
          invoker.setParameters(nameVariation, pp);
          T resolved;
          try {
            resolved = invoker.call();
          } catch (final Exception e) {
            resolved = null;
          }
          if (resolved != null) {
            return resolved;
          } else {
            // Only if allow prefixing - doesn't start with default
            // things like java.
            if (allowPrefixing(nameVariation)) {
              // Some trickery here to make defaults easier
              final String prefix = startWithDefaultPrefix(nameVariation);
              if (prefix != null) {
                // If starts with a default prefix, try resolving without the prefix
                resolved = tryResolve(invoker, pp, nameVariation.substring(prefix.length()));
                if (resolved != null) {
                  return resolved;
                }
              }
              // If couldn't resolve with the default prefix, try all default prefixes
              for (final String defaultPrefixes : this.defaultPrefixes) {
                resolved = tryResolve(invoker, pp, defaultPrefixes + nameVariation);
                if (resolved != null) {
                  return resolved;
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  private static <T> T tryResolve(
      final TypedInvoker<T> invoker,
      final PropertiesProvider pp,
      final String searchName) {
    T resolved;
    invoker.setParameters(searchName, pp);
    try {
      resolved = invoker.call();
    } catch (final Exception e) {
      resolved = null;
    }
    if (resolved != null) {
      return resolved;
    }
    return null;
  }

  private boolean allowPrefixing(final String name) {
    for (final String prefix : this.nonSubstituablePrefixes) {
      if (name.startsWith(prefix)) {
        return false;
      }
    }
    return true;
  }

  private String startWithDefaultPrefix(final String name) {
    for (final String prefix : this.defaultPrefixes) {
      if (name.startsWith(prefix)) {
        return prefix;
      }
    }
    return null;
  }

  @Override
  public boolean isSet(final String qualifiedName) {
    try {
      // Use iterateAndGet to make sure we go through same code-path as getProperty()
      // and try all prefixes...
      final Boolean res = iterateAndGet(qualifiedName, new TypedInvoker<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          final PropertiesProvider pp = getProvider();
          final String name = getPropertyName();
          if (pp.isSet(name)) {
            return true;
          } else {
            // Returning null will ensure iterateAndGet continues to next provider
            return null; // NOSONAR
          }
        }
      });
      return res != null;
    } catch (final Exception e) {
      return false;
    }
  }

  /*
   * Implementation uses ReferenceEvaluator to perform all necessary conversion attempts. Since a setMethod() in a
   * setter is generic, type information is not preserved at run time.
   * 
   * Class is used to overwrite natural type inference performed by the regular ReferenceEvaluator
   */
  @Override
  public <T> T resolve(final String name, final Class<T> clazz)
      throws ConfigurationItemNotDefinedException, ObjectInitializationException {
    final Setter<T> tmp = new Setter<>();
    final Object propertyValue = getProperty(name);
    final ExplicitReferenceEvaluator<Setter<T>> re =
        new ExplicitReferenceEvaluator<>(clazz, tmp,
            Arrays.asList(new NamedEvaluator("property", false,
                overrides -> propertyValue)));
    final Setter<T> o = re.evaluate();
    if (o == null) {
      throw new ConfigurationItemNotDefinedException("Property " + name + " is not found");
    } else {
      return o.settable;
    }
  }

  public static class Setter<T> {
    T settable;

    public void setProperty(final T x) {
      this.settable = x;
    }

  }

  @Override
  public String resolveString(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return resolve(name, String.class);
  }

  @Override
  public int resolveInt(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return resolve(name, Integer.class);
  }

  @Override
  public boolean resolveBoolean(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return resolve(name, Boolean.class);
  }

  @Override
  public double resolveDouble(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return resolve(name, Double.class);
  }

  public static class ArraySetter<T> {
    T[] settable;

    public void setProperty(final T[] x) {
      this.settable = x;
    }

  }

  @Override
  public long resolveLong(final String name) throws ConfigurationItemNotDefinedException,
      ObjectInitializationException {
    return resolve(name, Long.class);
  }

  private final ConcurrentMap<String, ModifiableImpl<Object>> resolvedProperties =
      new ConcurrentHashMap<>();

  private void reloadResolvedProperties() {
    for (final Map.Entry<String, ModifiableImpl<Object>> entry : this.resolvedProperties
        .entrySet()) {
      final String propertyKey = entry.getKey();
      final ModifiableImpl<Object> previouslyResolved = entry.getValue();
      final Object previousValue = previouslyResolved.get();

      try {
        final Object currentValue = getProperty(propertyKey);

        // If current value is a string and different, update it
        if (currentValue instanceof String && !currentValue.equals(previousValue)) {
          previouslyResolved.set(currentValue);
        }
        // otherwise if current value is null and it wasn't null previously, update it
        else if (currentValue == null && previousValue != null) {
          previouslyResolved.set(null);
        }
      } catch (final ConfigurationItemNotDefinedException e) {
        if (previousValue != null) {
          previouslyResolved.set(null);
        }
      }
    }
  }

  public ModifiableImpl<?> resolveModifiable(final String qualifiedName) {
    final ModifiableImpl<Object> previouslyResolved = this.resolvedProperties.get(qualifiedName);

    if (previouslyResolved != null) {
      return previouslyResolved;
    }

    Object propertyValue = null;
    try {
      propertyValue = getProperty(qualifiedName);
    } catch (final ConfigurationItemNotDefinedException ignore) {
      // NOOP
    }

    final ModifiableImpl<Object> resolved = new ModifiableImpl<>(propertyValue);
    final ModifiableImpl<Object> previous =
        this.resolvedProperties.putIfAbsent(qualifiedName, resolved);
    return previous == null ? resolved : previous;
  }

  @Override
  public Object getProperty(final String qualifiedName)
      throws ConfigurationItemNotDefinedException {
    Object res = null;
    res = iterateAndGet(qualifiedName, new TypedInvoker<Object>() {
      @Override
      public Object call() throws Exception {
        final PropertiesProvider pp = getProvider();
        final String name = getPropertyName();
        if (pp.isSet(name)) {
          return pp.getProperty(getPropertyName());
        } else {
          return null;
        }
      }
    });
    if (res != null) {
      return res;
    }
    throw new ConfigurationItemNotDefinedException("Can't resolve property " + qualifiedName);
  }

  @Override
  public Set<String> getQualifiedNames() {
    final Set<String> allNames = new HashSet<>();

    for (final String type : getAnnouncedTypes()) {
      final PropertiesProvider provider = getPropertiesProvider(type);
      if (provider != null) {
        final Collection<String> names = provider.getQualifiedNames();
        allNames.addAll(names);
      }
    }
    return allNames;
  }
}
