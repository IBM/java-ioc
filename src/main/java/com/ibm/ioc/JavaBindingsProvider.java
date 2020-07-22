/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ioc.impl.BindingsMapEvaluator;
import com.ibm.ioc.impl.BindingsProviderBase;
import com.ibm.ioc.impl.BooleanLiteral;
import com.ibm.ioc.impl.BuilderImplementationFactory;
import com.ibm.ioc.impl.DoubleLiteral;
import com.ibm.ioc.impl.Evaluatable;
import com.ibm.ioc.impl.ImplementationFactory;
import com.ibm.ioc.impl.ImplementationFactoryImpl;
import com.ibm.ioc.impl.IntegerLiteral;
import com.ibm.ioc.impl.InterfaceBinding;
import com.ibm.ioc.impl.LiteralEvaluator;
import com.ibm.ioc.impl.LongLiteral;
import com.ibm.ioc.impl.NamedEvaluator;
import com.ibm.ioc.impl.PropertyEvaluator;
import com.ibm.ioc.impl.ProxyEvaluator;
import com.ibm.ioc.impl.RefPropEvaluator;
import com.ibm.ioc.impl.ReferenceEvaluator;
import com.ibm.ioc.impl.SingletonImplementationFactory;
import com.ibm.ioc.impl.StringLiteral;
import com.ibm.ioc.impl.TernaryImplementationFactory;

public final class JavaBindingsProvider extends BindingsProviderBase {

  private static final Logger _logger = LoggerFactory.getLogger(JavaBindingsProvider.class);

  private static final int JAVA_VERSION;

  static {
    final String version = System.getProperty("java.version");
    final String[] components = version.split("\\.");

    final int v = Integer.parseInt(components[1]);
    JAVA_VERSION = v;
  }

  interface RegistrableDefinition {
    void register();
  }

  private final HashSet<RegistrableDefinition> allDefs = new HashSet<>();

  public JavaBindingsProvider(final List<? extends Class<?>> classList) {
    final BindingsFactory factory = new BindingsFactory();

    for (final Class<?> bindingsClass : classList) {
      initializeBinding(bindingsClass, factory);
    }
    registerAll();
    _logger.info("Finished loading and registering bindings");
  }

  public JavaBindingsProvider() {
    this(loadAllBindingsClasses("Bindings.class", "TestBindings.class"));
  }

  private static void withRef(final Class<?> cls, final String override,
      final Map<Class<?>, String> implOverrides) {
    if (override == null) {
      throw new IllegalStateException("Attempt to override " + cls.getName() + " with null");
    }
    if (implOverrides.containsKey(cls)) {
      throw new IllegalStateException("Override already exists for " + cls.getName() + "; current="
          + implOverrides.get(cls) + ", new=" + override);
    }
    implOverrides.put(cls, override);
  }

  // helper passthrough class to let the various Bindings register themselves
  public class BindingsFactory {
    public final <T> Definition def(final Class<T> objectClass) {
      return new Definition(objectClass);
    }

    public final <T> Definition def(final Class<T> objectClass, final Class<?> interfaceClass) {
      final Definition def = def(objectClass);
      def.iface(interfaceClass);
      return def;
    }

    public final <T> Definition builder(final Class<T> builderClass) {
      final Definition def = new Definition(builderClass);
      def.isBuilder = true;
      return def;
    }

    public final <T> Definition builder(
        final Class<T> builderClass,
        final Class<?> interfaceClass) {
      final Definition def = new Definition(builderClass);
      def.iface(interfaceClass);
      def.isBuilder = true;
      return def;
    }

    public final TernaryDefinition ternaryDef(
        final String propertyName,
        final Class<?> interfaceClass,
        final String trueRef,
        final String falseRef) {
      return new TernaryDefinition(interfaceClass, propertyName, trueRef, falseRef);
    }

    public final TernaryDefinition ternaryDef(
        final String propertyName,
        final Class<?> interfaceClass,
        final String alternateRef) {
      return ternaryDef(propertyName, interfaceClass, null, alternateRef);
    }

    public final class TernaryDefinition implements RegistrableDefinition {
      private final Class<?> interfaceClass;
      private final String propertyName;
      private final String trueRef;
      private final String falseRef;
      private final Map<Class<?>, String> implOverrides = new HashMap<>();
      private final Map<Class<?>, String> interfaceClassMap = new HashMap<>();

      private TernaryDefinition(final Class<?> interfaceClass,
          final String propertyName,
          final String trueRef,
          final String falseRef) {
        this.interfaceClass = interfaceClass;
        this.propertyName = propertyName;
        this.trueRef = trueRef;
        this.falseRef = falseRef;
        JavaBindingsProvider.this.allDefs.add(this);
      }

      public TernaryDefinition iface(final Class<?> interfaceClass) {
        this.interfaceClassMap.put(interfaceClass, null);
        return this;
      }

      public TernaryDefinition iface(final Class<?> interfaceClass, final String name) {
        this.interfaceClassMap.put(interfaceClass, name);
        return this;
      }

      public TernaryDefinition withRef(final Class<?> cls, final String override) {
        JavaBindingsProvider.withRef(cls, override, this.implOverrides);
        return this;
      }

      @Override
      public void register() {
        try {
          @SuppressWarnings({"rawtypes", "unchecked"})
          final ImplementationFactory<?> iFactory =
              new SingletonImplementationFactory(
                  new TernaryImplementationFactory(JavaBindingsProvider.this,
                      this.interfaceClass,
                      this.propertyName,
                      this.trueRef,
                      this.falseRef),
                  this.implOverrides);

          if (this.interfaceClassMap.isEmpty()) {
            iface(iFactory.getImplementationClass());
          }

          iFactory.setImplementedInterfaces(this.interfaceClassMap);

          for (final Entry<Class<?>, String> iface : this.interfaceClassMap.entrySet()) {
            final InterfaceBinding binding = getOrCreateInterfaceBinding(iface.getKey());
            if (iface.getValue() != null) {
              binding.addImplementationFactory(iface.getValue(), iFactory);
            } else {
              binding.setDefaultImplementationFactory(iFactory);
            }
          }
        } catch (final IllegalConfigurationContentException e) {
          throw new IllegalStateException("Illegal bindings", e);
        }
      }
    }

    public final class Definition implements RegistrableDefinition {
      private final Class<?> objectClass;
      private final Map<Class<?>, String> interfaceClassMap = new HashMap<>();
      private final Map<Class<?>, String> implOverrides = new HashMap<>();
      List<NamedEvaluator> params = new ArrayList<>();
      private boolean isBuilder;

      private Definition(final Class<?> objectClass) {
        this.objectClass = objectClass;
        JavaBindingsProvider.this.allDefs.add(this);
      }

      @Override
      public String toString() {
        return this.objectClass.toString();
      }

      public Definition iface(final Class<?> interfaceClass) {
        this.interfaceClassMap.put(interfaceClass, null);
        return this;
      }

      public Definition iface(final Class<?> interfaceClass, final String name) {
        this.interfaceClassMap.put(interfaceClass, name);
        return this;
      }

      public Definition withRef(final Class<?> cls, final String override) {
        JavaBindingsProvider.withRef(cls, override, this.implOverrides);
        return this;
      }

      private Definition set(
          final String name,
          final boolean required,
          final Evaluatable evaluatable) {
        this.params.add(new NamedEvaluator(name, required, evaluatable));
        return this;
      }

      public Definition set(final String name, final String constantValue) {
        this.params.add(new NamedEvaluator(name, true, new StringLiteral(constantValue)));
        return this;
      }

      @SuppressWarnings({"rawtypes", "unchecked"})
      public Definition set(final String name, final Object constantValue) {
        this.params
            .add(new NamedEvaluator(name, true, new ReferenceEvaluator(constantValue, null)));
        return this;
      }

      public Definition set(final String name, final boolean constantValue) {
        this.params.add(new NamedEvaluator(name, true, new BooleanLiteral(constantValue)));
        return this;
      }

      public Definition set(final String name, final long constantValue) {
        this.params.add(new NamedEvaluator(name, true, new LongLiteral(constantValue)));
        return this;
      }

      public Definition set(final String name, final int constantValue) {
        this.params.add(new NamedEvaluator(name, true, new IntegerLiteral(constantValue)));
        return this;
      }

      public Definition set(final String name, final double constantValue) {
        this.params.add(new NamedEvaluator(name, true, new DoubleLiteral(constantValue)));
        return this;
      }

      @SuppressWarnings({"rawtypes", "unchecked"})
      @Override
      public void register() {
        try {
          final ImplementationFactory iFactory;
          if (this.isBuilder) {
            iFactory = new SingletonImplementationFactory(
                new BuilderImplementationFactory(this.objectClass, this.params),
                this.implOverrides);
          } else {
            // Create the implementation factory
            iFactory = new SingletonImplementationFactory(new ImplementationFactoryImpl(
                this.objectClass, this.params), this.implOverrides);
          }

          if (this.interfaceClassMap.isEmpty()) {
            iface(iFactory.getImplementationClass());
          }

          iFactory.setImplementedInterfaces(this.interfaceClassMap);

          for (final Entry<Class<?>, String> iface : this.interfaceClassMap.entrySet()) {
            final InterfaceBinding binding = getOrCreateInterfaceBinding(iface.getKey());
            if (iface.getValue() != null) {
              binding.addImplementationFactory(iface.getValue(), iFactory);
            } else {
              binding.setDefaultImplementationFactory(iFactory);
            }
          }

        } catch (final IllegalConfigurationContentException e) {
          throw new IllegalStateException("Illegal bindings", e);
        }
      }

      public Definition prop(final String name, final String propertyName) {
        set(name, false, new PropertyEvaluator(propertyName));
        return this;
      }

      public Definition literal(final String name, final String value) {
        set(name, false, new LiteralEvaluator(value));
        return this;
      }

      public <V> Definition allRefs(final String name, final Class<V> class1) {
        set(name, true, new BindingsMapEvaluator<>(class1, JavaBindingsProvider.this));
        return this;
      }

      public Definition ref(final String field, final Class<?> class1) {
        set(field, true, new ProxyEvaluator(JavaBindingsProvider.this, class1, null));
        return this;
      }

      public Definition ref(
          final String field,
          final Class<?> class1,
          final String referralName) {
        set(field, true, new ProxyEvaluator(JavaBindingsProvider.this, class1, referralName));
        return this;
      }

      public Definition refprop(
          final String field,
          final Class<?> class1,
          final String referralPropertyName) {
        set(field, true, new RefPropEvaluator(JavaBindingsProvider.this, class1, referralPropertyName));
        return this;
      }

      public Definition nullRef(final String field) {
        set(field, false, new LiteralEvaluator(null));
        return this;
      }
    }
  }

  private static class IgnoreNewClassFormatException extends ClassNotFoundException {
    private static final long serialVersionUID = 1L;

    public IgnoreNewClassFormatException(final String s) {
      super(s);
    }
  }

  private static final class BindingsLoader extends ClassLoader {

    private final String className;

    private BindingsLoader(final ClassLoader parent, final String className) {
      super(parent);
      this.className = className;
    }

    private static void checkClassVersion(final URL classLocation)
        throws ClassNotFoundException {


      try (DataInputStream in = new DataInputStream(classLocation.openStream());) {
        final int magic = in.readInt();
        if (magic != 0xcafebabe) {
          throw new ClassNotFoundException("Invalid class file format: " + classLocation);
        }

        in.readUnsignedShort(); // minor
        final int major = in.readUnsignedShort();

        if (JAVA_VERSION <= 6 && major > 50) {
          final String msg = "Class format " + major + " not supported at runtime level " +
              JAVA_VERSION + ": " + classLocation;
          _logger.debug(msg);
          throw new IgnoreNewClassFormatException(msg);

        }

      } catch (final IOException e) {
        throw new ClassNotFoundException("Error loading class file: " + classLocation, e);
      }
    }

    @Override
    protected Class<?> findClass(final String fileName) throws ClassNotFoundException {
      try {
        final URL url = new URL(fileName);

        checkClassVersion(url);

        url.openStream();
        final URLConnection openConnection = url.openConnection();
        final int contentLength = openConnection.getContentLength();
        final ByteArrayOutputStream output = new ByteArrayOutputStream(contentLength);
        final InputStream input = openConnection.getInputStream();
        final byte[] buf = new byte[4096];
        int len;
        while ((len = input.read(buf)) > 0) {
          output.write(buf, 0, len);
        }

        final byte[] b = output.toByteArray();
        return defineClass(this.className, b, 0, b.length, null);
      } catch (final MalformedURLException e) {
        throw new ClassNotFoundException("Bad URL " + fileName, e);
      } catch (final ClassFormatError e) {
        throw new ClassNotFoundException("Invalid format", e);
      } catch (final IOException e) {
        throw new ClassNotFoundException("IOException", e);
      }
    }
  }


  private static List<Class<?>> loadAllBindingsClasses(final String... classResourceNames) {
    final List<Class<?>> allBindingsClasses = new ArrayList<>();
    try {
      final ClassLoader classLoader = JavaBindingsProvider.class.getClassLoader();
      for (final String classResourceName : classResourceNames) {
        final Enumeration<URL> urls = classLoader.getResources(classResourceName);
        while (urls.hasMoreElements()) {
          final URL officialUrl = urls.nextElement();
          _logger.info("Loading File {}", officialUrl);

          // classResourceName must end with ".class"
          if (!classResourceName.endsWith(".class")) {
            throw new IllegalArgumentException("Invalid class resource name: "
                + classResourceName);
          }

          // split length must be at least 2 because of the above check
          final String[] splitClassName = classResourceName.split("\\.");
          final String className = splitClassName[splitClassName.length - 2];

          final Class<?> bindingsFile =
              new BindingsLoader(classLoader, className).loadClass(officialUrl.toString());

          allBindingsClasses.add(bindingsFile);
        }
      }
    } catch (final IOException | ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
    return allBindingsClasses;
  }

  void initializeBinding(final Class<?> cls, final BindingsFactory factory) {
    try {
      final JavaBindings obj = (JavaBindings) cls.getDeclaredConstructor().newInstance();
      obj.register(factory);
    } catch (final IllegalArgumentException | ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  void registerAll() {
    for (final RegistrableDefinition def : this.allDefs) {
      def.register();
    }
  }
}
