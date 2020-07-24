# java-ioc
Java Inversion of Control Framework


# Introduction 
Any large software system requires deployment time customization in order to improve system performance, availability; provide user specific customization or reflect customer’s priorities.

# Inversion of Control

Software may provide a wide range of configurable elements of a system. In order to achieve systematic and easy to use configurability, Inversion Of Control (IoC can be used as a guiding design principal in order to:
- Remove the knowledge of how services and components are wired together from application code; the code relies on interfaces only instead of concrete implementations,
- Minimize dependencies and coupling between system components, making adding third party plugin implementations easier,
- Get rid of most static variables and singletons; artifacts that result in tighter coupling between software components, and
- Facilitate effective unit testing using third party or hand-made Mock implementations of required interfaces.
- Reduce hard coding of settings that may change.

Now consider you’re tasked with implementing a new implementation of Servlet, NewServlet, to replace to old implementation, OldServlet, designed to use JSON for serialization instead of XML and Cloud storage instead of file storage. You have all your implementation complete and now just need to set up the configuration. A class not using IoC:

```java
public class NewServlet extends Servlet {
  public Serializer serializer;
  public StorageHandler storageHandler;
  public int timeout;
  public int maxConcurrency;
  public NewServlet() {
    this.serializer = new JSONSerializer(prettyPrint, encoding);
    this.storageHandler = new CloudStorageHandler(provider, location);
    this.timeout = 1000;
    this.maxConcurrency = 500;
  }
} 
```

Why is this bad?
- Serializer and StorageHandler here are interfaces which may have multiple different implementations. Here which one is used is hard-coded into the NewServlet. So if we had multiple different Servlets and wanted to change the implementation of Serializer or StorageHandler we would need to change all of them. If the new implementation required different parameters to be passed in we would have to handle passing them in in the construction in every class they were needed in.
- There is no way to unit test this without having a fully functional StorageHandler and Serializer.
- What if we need to use the StorageHandler or Serializer in a different class, we have to pass it in. How awkward!


Now consider the following using Constructor injection:
```java
public class NewServlet extends Servlet {
  public NewServlet(Serializer serializer, StorageHandler storageHandler, int timeout, int maxConcurrency) {
    this.serializer = serializer;
    this.storageHandler = storageHandler;
    this.timeout = timeout;
    this.maxConcurrency = maxConcurrency;
  }
}
```

Now this is better:
- Depending on how different examples of this servlet are initialized it should be easier to change what serializer and storageHandler are used.
- We can effectively unit test this ExampleServlet by making "Mock" Serializer and StorageHandler implementations that obviously work and then testing that the ExampleServlet works as it should.

# Writing Your Class
With our IOC framework it should be possible to improve on this and effectively remove configuration parameters from your code. To do this: First define a Java interface which your code will implement, this will ensure if a different implementation is needed it can be swapped in easily in place of the old implementation. Write the actual implementation class. In your implementation class you will want to define a public builder class which constructs the interface using your implementation. An example could be:

```java
public class NewServlet extends Servlet {
  public static class Builder {
    public Serializer serializer;
    public StorageHandler storageHandler,
    public int timeout;
    public int maxConcurrency;
 
    public NewServlet build() {
      return new NewServlet(serializer, storageHandler, timeout, maxConcurrency);
    }
  }
 
  public NewServlet(Serializer serializer, StorageHandler storageHandler, int timeout, int maxConcurrency) {
    this.serializer = serializer;
    this.storageHandler = storageHandler;
    this.timeout = timeout;
    this.maxConcurrency = maxConcurrency;
  }
}
```

Now our Java class is complete but we will need to modify two files in our project folder in order to ensure that our NewServlet is integrated into the software.
- src/main/java/Bindings.java - this contains information about how to create instances of classes.
- src/main/resources/default-properties.ini - this contains individual values for parameters referenced from Bindings.java.


# Bindings
The bindings file is just a definition of how different objects are constructed. Objects are simply created by specifying the builder or constructor used to make them and defining each of the fields of the constructor or builder by either referencing the default-properties.ini file or by referring to another object defined in the bindings. Objects in bindings are referenced by what class they create and possibly a kind field internal to the bindings. Each class can have (one) default configuration and any number of additional "kinds" defined by bindings. One definition of an object in the bindings can be used for multiple classes which is useful when dealing with inheritance and interfaces. In our situation here we probably simply want to replace OldServlet with NewServlet. Note that our bindings never should actually refer to an OldServlet but instead just refer to a Servlet object which is defined in some places to be an OldServlet. Thus to update our code we simply replace any reference to OldServlet with one to NewServlet.

But first we need to define our NewServlet construction and set it to construct a kind of Servlet.

```java
public class Bindings implements JavaBindings {
  @Override
  public void register(final BindingsFactory def) {
    def.builder(NewServlet.Builder.class)
        .iface(Servlet.class, "new-servlet")
        .ref("serializer", Serializer.class, "json-serializer")
        .ref("storage-handler", StorageHandler.class, "cloud-storage-handler")
        .prop("timeout", "timeout")
        .prop("max-concurrency", "concurrency");
    }
}

```
Lets look at what this does:
- The first line defines what builder we are using. The bindings are smart and will recognize that we are creating a NewServlet and will associate this object we are defining as the default NewServlet.
- The second line defines this NewServlet we defined as a Servlet with kind "new-servlet", this will allow us to replace the existing oldServlet implementation of servlet by changing the references to point to our newServlet.
- The next two lines will define the Serializer and StorageHandler we use by referring to other objects defined elsewhere in the bindings. If no third argument was provided we would use the default.
- The final two lines will set the properties of timeout and maxConcurrency to values set in the default-properties.ini file in the same project folder. Note here that the field names are converted to camel case thus "max-concurrency" becomes "maxConcurrency".

So now to complete the changes to be made we just need to change every reference to the OldServlet and make it use the NewServlet. So find references of the form:

    .ref("servlet", Servlet.class, "old-servlet")

And change them to:

    .ref("servlet", Servlet.class, "new-servlet")

Now we can expose some more bindings functions and features.
- allRefs - allRefs will give you all the the kinds of objects for that type defined in bindings. If for instance you were writing a objectReader that needed to be able to read objects using any different codec you’d want a collection of all codecs. To do so you could simply use allRef.

```java
 def.builder(NewServlet.Builder.class)
      .iface(Servlet.class, "new-servlet")
      .ref("serializer", Serializer.class, "json-serializer")
      .ref("storage-handler", StorageHandler.class, "cloud-storage-handler")
      .prop("timeout", "timeout")
      .prop("max-concurrency","concurrency")
```

- def - this can be used to initialize a class which doesn’t have a builder. It behaves similarly to .builder except the properties are fed directly into the class itself.
- set - Setter methods can be used to set values instead of using a method. Their use is discouraged and mainly for compatibility. Below is an example setting the "algorithm" and "key-size" fields.

```java
 def.def(AllOrNothingTransformCodec.class)
      .iface(DataSourceCodec.class, "aont-aes-128")
      .set("algorithm", "AES")
      .set("key-size", 128);
```

## Defining a default implementation 
The way to define a default implementation is to define one without a name. If you are using a builder, here's an example:

```java
 def.builder(NewServlet.Builder.class)
      .iface(Servlet.class) // This makes this binding the default for Servlet
      ....
```

## Defining a named (non-default) implementation

```java
 def.builder(NewServlet.Builder.class)
      .iface(Servlet.class, "my-servlet") // This adds a name "my-servlet" for this binding and therefore NOT the default
      ....
```

## Overriding defaults
It is possible for the default implementation to be overridden by a named implementation. It's as simple as setting the property of the interface you want to override (key) to the name of the binding you want to override with (value).

### Overriding by system property (JVM argument): 
`-Dorg.cleversafe.servlet.Servlet=my-servlet`

### Overriding by config file:

```
[bindings]
org.cleversafe.servlet.Servlet = my-servlet
```
TODO: add section on how to use config files
TODO: add section on how to use default-properties.ini

*NOTE: It's highly recommended NOT to override a default implementation by changing the default-properties.ini. This will make it so the default is always overridden, so you may as well change the default in bindings.*


# Properties
The default-properties.ini file generally consists simple of lines of form a=b, which will set the variable on the left as found in bindings.java to the value on the right of the equals sign. Additionally there are headers surrounded by brackets which define areas of properties. When specifying the second argument of the prop function the header should go first separated by a dot and then the variable name.
So thus referring to the timeout variable with a properties file looks like this:

```
[servlet]
timeout=1000
```
In Bindings.java, it would be referenced like this:
`.prop("timeout","servlet.timeout")`

## Properties Examples
TODO: Show lists, arrays, type conversions from String (e.g., "1MB" → int, long, BigInteger; "1GB" or "1 GiB" or "1 TB" or "1 EB" → long, BigInteger; "10 min" → 600000; "5 hr" or "5 hours" → 18000000, and so on).
TODO: More examples: Classes that have static **.fromString(String str)** type methods or **String** constructors, those classes can also be injected.

## Complex Properties (JSON)
You can initialize more advanced properties conveniently by using JSON for instance if you wanted to initialize some simple class

```java
public class DiskInfo {
  private final UUID diskIdentifer;
  private final long capacity;
  private final String model;
 
  public DiskInfo(
    final UUID diskIdentifer
    final long capacity,
    final String model) {
    this.diskIdentifer = diskIdentifer;
    this.model = model;
    this.capacity = capacity;
  }
}
```
you could just use JSON to specify the value of that class in the properties file:

```
[sliceserver]
disk_info = ["diskIdentifier"="63780524-6e21-3bb9-a679-8c7ac86919dc", "model"="ST4000DM000", "capacity"=4294967296]
```
Your bindings file would only need to refer to the properties as usual:
`.prop("disk","sliceserver.disk_info")`

# Runtime-Configurable Properties
Any property type described above can be made into a runtime-configurable property!  What this means is that you can update the property value without requiring a process restart.  This is very powerful and should be used in the case where the application can take advantage of an updated configuration value while preserving the existing execution context.  It is important because process restart takes time, impacts system reliability and availability; and most importantly may change execution context (such as cache states) so that the immediate impact of a changed parameter could not be easily measured.  Due to these complications, customer will have to live with a problem for an extended amount of time, even when a solution is available or at least could be tried. This feature also gives valuable performance tuning and testing options.  It is highly encouraged to make configuration values runtime-configurable as much as possible.  There are obviously some exceptions when it is not at all practical to make some properties runtime-configurable: examples include memory configuration, runtime-configurability support for which may require a completely different memory management design, or a change that requires stopping and restarting a full tree of Services.  But aside from some exceptions, most properties can and should be runtime-configurable.

There are multiple ways to create runtime-configurable properties in your class, depending on your requirements.  The advice here will be to use the least "powerful" way that will meet your requirements.  This rule should be generally followed, but it is especially relevant for when you have multiple easy ways to do the same thing.  Read more about this principle here: https://blog.codinghorror.com/the-principle-of-least-power/ and https://www.w3.org/2001/tag/doc/leastPower.html

But for any of the ways that are described below, the way to add the property to the class Bindings remains the same.  For example, if you want to configure a field called "periodicWritePeriod" with a value that comes from an external property called *property.name.periodic-write-period*, you need to add a *.prop* in your bindings like this:

```java
def.builder(LogWriter.Builder.class)
      .prop("periodic-write-period", "property.name.periodic-write-period");
```
Now you can allow this property to be configured at runtime. 
Here are the different approaches, ordered from the least to the most powerful.

## 1) The POJO *setter* pattern 
This is the least powerful method to allow a property to be reconfigured at runtime (but is expected to be used the most).

If you have a private field in a class that never leaves that class, the best bet is to use a POJO setter - this is the best solution because it creates the most intuitive Java class with a constructor and setters than can be used anywhere, not just by the configuration framework (e.g. unit tests and/or tools).

In order to do this, you will want to simply exclude the property from the Builder and add a setter for the object. The configuration framework will automatically call the setter every time the property changes. An example is below.

A **@RequireParameterBinding** annotation can be added to the setter to allow the configuration framework to ensure that the setter is correctly hooked into the configuration framework.  During object initialization, it ensures that corresponding parameter is present in bindings, and that it is mapped correctly to the corresponding setter.  While this annotation is optional (because it requires importing the annotation from the framework but the framework must fully work with normal Java classes with setters), it is recommended to use this annotation as it will catch bugs during a run of the BindingsTest if the corresponding .prop is missing from Bindings.

```java
public class LogWriter {
   public static class Builder {
      public LogWriter build() {
         return new LogWriter();
      }
   }
 
   private long periodicWritePeriod;
 
   // This setter gets called automatically by the configuration framework, every time the property changes.
   @RequireParameterBinding
   public void setPeriodicWritePeriod(final long periodicWritePeriod) {
      this.periodicWritePeriod = periodicWritePeriod;
   }
}
```

The setter pattern can *also* be used in conjunction with an *initial value* being passed in through the Builder and constructor:

```java
public class LogWriter {
  public static class Builder {
    public long periodicWritePeriod;
    public LogWriter build() {
      return new LogWriter(this.periodicWritePeriod);
    }
  }
 
  private final long periodicWritePeriod;
 
  // set the initial value during Object construction
  public LogWriter(final long periodicWritePeriod) {
    this.periodicWritePeriod = periodicWritePeriod;
  }
 
  // This setter gets called automatically by the configuration framework, every time the property changes.
  @RequireParameterBinding
  public void setPeriodicWritePeriod(final long periodicWritePeriod) {
    this.periodicWritePeriod = periodicWritePeriod;
  }
}
```

## 2) Java 8 primitive Suppliers (IntSupplier, LongSupplier, DoubleSupplier, BooleanSupplier)
This is more powerful than the POJO setter.

Use these ONLY if your class requirement is to get a re-configurable primitive (int, long, int, double, or boolean), and you *cannot* use the POJO setter pattern because you need to pass the re-configurability to classes other than the one that's built from the configuration framework.

```java
public class LogWriter {
  public static class Builder {
    public LongSupplier periodicWritePeriod;
    public LogWriter build() {
      return new LogWriter(this.periodicWritePeriod);
    }
  }
 
  private SomeOtherClass someOtherObject;
 
  // set the initial value during Object construction
  public LogWriter(final LongSupplier periodicWritePeriod) {
    this.someOtherObject = new SomeOtherClass(periodicWritePeriod);
  }
 
 
  [someOtherObject will use the LongSupplier method getAsLong() to get the latest value when it needs it]
}
```

## 3) Java 8 Generic Supplier<?> 

This is more powerful than the POJO setter and slightly more powerful than the primitive Suppliers.

Use this ONLY if your class requirement is to get a re-configurable *non*-primitive (e.g. collections or custom classes created from properties), and you *cannot* use the POJO setter pattern because you need to pass the re-configurability to classes other than the one that's built from the IoC framework.

For example, if you're injecting a String to Integer Map (constructed from a JSON property value, e.g. ["firstPeriod" : 300, "secondPeriod" : 1000, "anotherPeriod" : 8000]), you'd do the following:
```java
public class LogWriter {
  public static class Builder {
    // The following can be injected in place of anywhere a Map<String, Integer> can be injected
    public Supplier<Map<String, Integer>> periodicWritePeriods;
    public LogWriter build() {
      return new LogWriter(this.periodicWritePeriods);
    }
  }
 
  private SomeOtherClass someOtherObject;
 
  // set the initial value during Object construction
  public LogWriter(final Supplier<Map<String, Integer>> periodicWritePeriods) {
    this.someOtherObject = new SomeOtherClass(periodicWritePeriods);
  }
 
  [someOtherObject will use the Supplier method get() to get the latest value when it needs it]
}
```

## 4) Define your own custom Generic injectables
This is the most powerful method to allow creating runtime-reconfigurable properties in your class.  Use this only if the above methods fail to satisfy your requirements.

One example where you'd need a custom Generic injectable is when you not only need to pass the value around to other classes but also require running some number of unrelated jobs when the property value changes.

The IoC framework makes the best attempt to inject what your class needs.  If you inject an interface called `Observable<SomeClassType>` and the property value cannot directly resolve to an `Observable`, it'll look to see if it can be resolved to a "SomeClassType".

The requirements for a custom generic injectable are that what you're injecting is of the Java Generic form `A<B>`, and
1. class A is declared with EXACTLY ONE Generic parameter, B (Note also that B could again be a further Generic type, like Map<String, SomeObject> that can be created from gson.  Any B that you can inject today for a property will also work with A`<B>` as long as A satisfies the remaining requirements below.)
2. your property will resolve into B
3. class A has a default constructor so it can be used by the configuration framework for instantiation
4. class A implements EXACTLY ONE method of the following form (to allow value updates):
        return type "void"
        single argument, B (with the exact same Generic signature as the one used in the class declaration of `A<B>`)

For example, consider this *Observable* class:

```java
public final class Observable<T> {
  public interface Observer<T> {
    void changed(T value);
  }

  private volatile T value;

  // notify in the same order as observers were added
  private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();

  public Observable() {
    this(null);
  }

  public Observable(final T initialValue) {
    this.value = initialValue;
  }

  public T getValue() {
    return this.value;
  }

  public void updateValue(final T newValue) {
    this.value = newValue;
    for (final Observer<T> observer : this.observers) {
      observer.changed(newValue);
    }
  }

  public void addObserver(final Observer<T> observer) {
    this.observers.add(observer);
    observer.changed(this.value);
  }

  public void removeObserver(final Observer<T> observer) {
    this.observers.remove(observer);
  }
}
```

The above class meets the requirements of a custom generic injectable and allows adding *Observer*s.  It may be used in the following way:

```java
public class LogWriter {
  public static class Builder {
    // The following can be injected in place of anywhere a Map<String, Integer> can be injected
    public Observable<Map<String, Integer>> periodicWritePeriods;
    public LogWriter build() {
      return new LogWriter(this.periodicWritePeriods);
    }
  }
 
  private SomeOtherClass someOtherObject;
 
  // set the initial value during Object construction
  public LogWriter(final Observable<Map<String, Integer>> periodicWritePeriods) {
    this.someOtherObject = new SomeOtherClass(periodicWritePeriods);
  }
}
 
 
...
// Somewhere else, unrelated code
public class SomeOtherClass {
  private final Observable<Map<String, Integer>> periodicWritePeriods;
  public SomeOtherClass(final Observable<Map<String, Integer>> periodicWritePeriods) {
    this.periodicWritePeriods = periodicWritePeriods;
  }
 
 
  // ...
  // ...
     // register an observer somewhere to kick off a job whenever the property changes
     this.periodicWritePeriods.addObserver(newValue -> doSomethingWith(newValue));
}
```

While the existing class, Observable, is a great example of how to use custom Generic injectables, you can basically do anything you want with this pattern.  The possibilities are endless!  (The singularity is near...)

## Best Practices
- Unlike Bindings .ref's, .prop values are nullable.  This means if a property is not set, if a property cannot be resolved to the required Java type, or if a property gets unset after being set, the configuration framework will set the value to 'null'.  If you expect the property to not be set, your class should handle nulls; this is especially the case for properties resolving as Java Collections.  The configuration framework accepts arbitrary user input, with minimal validation.  A typo by the user can result in the property being updated with a value of 'null' (and this has caused more than a few NPEs).
- Everything injected from Bindings is final and cannot be mutated; this includes Java Collections.  It's generally a bad practice to mutate something injected from Bindings; however, if your class absolutely needs to do that, consider making a copy of the injected object instead of directly storing the injected object.



