/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.ibm.ioc.BindingsProvider;
import com.ibm.ioc.ConfigurationItemNotDefinedException;
import com.ibm.ioc.ObjectInitializationException;
import com.ibm.ioc.ObjectInstantiationException;
import com.ibm.ioc.impl.BindingsMap;
import com.ibm.ioc.impl.BindingsMapEvaluator;
import com.ibm.ioc.impl.DelayedEvaluator;
import com.ibm.ioc.impl.Evaluatable;
import com.ibm.ioc.impl.ImplementationFactory;
import com.ibm.ioc.impl.NamedEvaluator;

public class BindingsDependencyUtil {

  private static final WeakHashMap<BindingsProvider, IdentityHashMap<NamedEvaluator, List<ObjectNode>>> objectNodesByNamedEvaluator =
      new WeakHashMap<>();
  private static final WeakHashMap<BindingsProvider, IdentityHashMap<ImplementationFactory<?>, Map<String, ObjectNode>>> objectNodesByImplementationFactory =
      new WeakHashMap<>();

  private static class LazySupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    private volatile T loaded = null;

    LazySupplier(final Supplier<T> supplier) {
      this.supplier = supplier;
    }


    @Override
    public T get() {
      if (this.loaded == null) {
        this.loaded = this.supplier.get();
      }
      return this.loaded;
    }

  }


  private static synchronized List<ObjectNode> getCachedObjectNodes(
      final BindingsProvider bindingsProvider,
      final NamedEvaluator param) {
    final Map<NamedEvaluator, List<ObjectNode>> objectNodeMap =
        objectNodesByNamedEvaluator.get(bindingsProvider);
    if (objectNodeMap != null) {
      return objectNodeMap.get(param);
    }
    return null;
  }

  private static synchronized void cacheObjectNodes(final BindingsProvider bindingsProvider,
      final NamedEvaluator param,
      final List<ObjectNode> objectNodes) {
    IdentityHashMap<NamedEvaluator, List<ObjectNode>> objectNodeMap =
        objectNodesByNamedEvaluator.get(bindingsProvider);
    if (objectNodeMap == null) {
      objectNodeMap = new IdentityHashMap<>();
      objectNodesByNamedEvaluator.put(bindingsProvider, objectNodeMap);
    }
    objectNodeMap.put(param, objectNodes);
  }


  private static synchronized ObjectNode getCachedObjectNode(
      final BindingsProvider bindingsProvider,
      final ImplementationFactory<?> impl,
      final String name) {
    final Map<ImplementationFactory<?>, Map<String, ObjectNode>> objectNodeMap =
        objectNodesByImplementationFactory.get(bindingsProvider);
    if (objectNodeMap != null) {
      final Map<String, ObjectNode> implMap = objectNodeMap.get(impl);
      if (implMap != null) {
        return implMap.get(name);
      }
    }
    return null;
  }

  private static synchronized void cacheObjectNode(final BindingsProvider bindingsProvider,
      final ImplementationFactory<?> impl,
      final String name,
      final ObjectNode objectNode) {
    IdentityHashMap<ImplementationFactory<?>, Map<String, ObjectNode>> objectNodeMap =
        objectNodesByImplementationFactory.get(bindingsProvider);
    if (objectNodeMap == null) {
      objectNodeMap = new IdentityHashMap<>();
      objectNodesByImplementationFactory.put(bindingsProvider,
          objectNodeMap);
    }
    Map<String, ObjectNode> implMap = objectNodeMap.get(impl);
    if (implMap == null) {
      implMap = new HashMap<>();
      objectNodeMap.put(impl, implMap);
    }
    implMap.put(name, objectNode);
  }


  private static List<ObjectNode> getDependenciesForParams(final BindingsProvider bindingsProvider,
      final List<NamedEvaluator> params, final Map<Class<?>, String> overrides) {
    final List<ObjectNode> dependencies = new ArrayList<>();
    if (params != null) {
      for (final NamedEvaluator param : params) {
        final List<ObjectNode> cachedObjectNodes =
            getCachedObjectNodes(bindingsProvider, param);
        if (cachedObjectNodes != null) {
          dependencies.addAll(cachedObjectNodes);
          continue;
        }
        final Evaluatable eval = param.getValue();
        try {
          // In case the object is an "allRefs" map, add each object in the map as
          // dependency
          if (eval instanceof BindingsMapEvaluator) {
            final BindingsMap<?> allRefsMap = ((BindingsMapEvaluator<?>) eval).evaluate(overrides);
            final Class<?> interfaceClass = allRefsMap.getInterfaceClass();
            final List<ObjectNode> subDependencies = new ArrayList<>();
            final BindingsProvider allRefsBindingsProvider = allRefsMap.getBindingsProvider();

            try {
              for (final Entry<String, ? extends ImplementationFactory<?>> entry : allRefsBindingsProvider
                  .getImplementations(interfaceClass).entrySet()) {
                final ObjectNode objectNode =
                    getObjectNode(allRefsBindingsProvider,
                        entry.getKey(), entry.getValue(), overrides);
                subDependencies.add(objectNode);
              }
            } catch (final ConfigurationItemNotDefinedException ignore) {
              // If there are no references, there are no dependencies.
            }
            cacheObjectNodes(allRefsBindingsProvider, param,
                subDependencies);
            dependencies.addAll(subDependencies);
          } else if (eval instanceof DelayedEvaluator) {
            final DelayedEvaluator evaluator = (DelayedEvaluator) eval;
            final List<ObjectNode> dependencyObjects =
                getDependenciesForParams(bindingsProvider,
                    evaluator.getParameters(overrides), evaluator.getCombinedOverrides(overrides));

            final Supplier<Object> objSupplier = new LazySupplier<>(() -> {
              try {
                return evaluator.evaluate(overrides);
              } catch (final ObjectInitializationException e) {
                throw new RuntimeException("Could not evaluate dependencies for: " + eval, e);
              }
            });

            final ObjectNode objectNode =
                new ObjectNode(evaluator.getImplementationClass(overrides), objSupplier,
                    evaluator.getImplementedInterfaces(overrides), dependencyObjects);
            cacheObjectNodes(bindingsProvider, param,
                Collections.singletonList(objectNode));
            dependencies.add(objectNode);
          }
        } catch (ConfigurationItemNotDefinedException | ObjectInitializationException e) {
          throw new RuntimeException("Could not evaluate dependencies for: " + eval, e);
        }
      }
    }
    return dependencies;
  }

  public static ObjectNode getObjectNode(
      final BindingsProvider bindingsProvider,
      final String rootName,
      final ImplementationFactory<?> impl,
      final Map<Class<?>, String> overrides) {
    final ObjectNode cachedObjectNode =
        getCachedObjectNode(bindingsProvider, impl, rootName);
    if (cachedObjectNode != null) {
      return cachedObjectNode;
    }

    final List<ObjectNode> dependencies =
        getDependenciesForParams(bindingsProvider,
            impl.getParameters(overrides),
            impl.getCombinedOverrides(overrides));

    final Supplier<Object> objSupplier = new LazySupplier<>(() -> {
      try {
        return impl.initialize(overrides);
      } catch (final ObjectInstantiationException | ObjectInitializationException e) {
        // This should not happen
        throw new RuntimeException("Could not evaluate dependencies for: "
            + impl.getImplementationClass(), e);
      }
    });
    final ObjectNode objectNode = new ObjectNode(impl.getImplementationClass(),
        objSupplier, impl.getImplementedInterfaces(), dependencies);
    cacheObjectNode(bindingsProvider, impl, rootName, objectNode);
    return objectNode;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static List<ObjectNode> getObjectNodes(
      final BindingsProvider bindingsProvider,
      final Class interfaceClass) {
    final Map<String, ImplementationFactory<?>> implementations;
    try {
      implementations = bindingsProvider.getImplementations(interfaceClass);
    } catch (final ConfigurationItemNotDefinedException e) {
      return Collections.emptyList();
    }

    final List<ObjectNode> dependencies = new ArrayList<>();

    for (final Map.Entry<String, ImplementationFactory<?>> implEntry : implementations.entrySet()) {
      dependencies.add(getObjectNode(bindingsProvider,
          implEntry.getKey(), implEntry.getValue(), Collections.emptyMap()));
    }

    return dependencies;
  }
}
