/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.ioc.JavaBindingsProvider.BindingsFactory;
import com.ibm.ioc.impl.ImplementationFactory;

public class JavaBindingsProviderNestedInterfaceTest {

  @Test
  public void testDefaultInterface() throws Exception {
    final JavaBindingsProvider provider =
        new JavaBindingsProvider(
            Arrays.asList(InterfaceBinding.class));

    System.setProperty("Ref", "p2");
    System.setProperty("Ref1", "p1");
    System.setProperty(MyInterface.class.getName().replace('$', '.'), "another");

    final ImplementationFactory<?> defaultImplementationSize =
        provider.getDefaultImplementation(Size.class);
    defaultImplementationSize.initialize();

    final ImplementationFactory<?> policyImpl1 = provider.getImplementation(Policy.class, "p1");
    policyImpl1.initialize();

    final ImplementationFactory<?> policyImpl2 = provider.getImplementation(Policy.class, "p2");
    policyImpl2.initialize();

    final ImplementationFactory<?> defaultImplementation =
        provider.getDefaultImplementation(MyInterface.class);
    final MyBaseImplementation1 iface2 =
        (MyBaseImplementation1) defaultImplementation.initialize();
    Assert.assertEquals(Policy1.class, iface2.policy.getClass());

    final ImplementationFactory<?> anotherImplementationMyInterface =
        provider.getImplementation(MyInterface.class, "another");
    final MyBaseImplementation1 iFace1 =
        (MyBaseImplementation1) anotherImplementationMyInterface.initialize();
    Assert.assertEquals(Policy1.class, iFace1.policy.getClass());



  }

  public static class InterfaceBinding implements JavaBindings {
    @Override
    public void register(final BindingsFactory def) {
      def.def(Policy1.class)
          .iface(Policy.class, "p1");

      def.def(Policy2.class).iface(Policy.class, "p2");

      def.def(Size.class);
      def.def(MyBaseImplementation.class, MyInterface.class)
          .set("intValue", 45)
          .refprop("policy", Policy.class, "Ref")
          .allRefs("foo", Size.class);

      def.def(MyBaseImplementation1.class)
          .iface(MyInterface.class, "another")
          .refprop("policy", Policy.class, "Ref1");


    }
  }

  public interface Policy {
  }

  public static class Policy1 implements Policy {
    public Policy1() {}
  }

  public static class Policy2 implements Policy {
  }

  public interface MyBaseInterface {
  }

  public interface MyInterface extends MyBaseInterface {
  }

  public static class MyBaseImplementation implements MyInterface {
    public long intValue;
    public String stringValue;
    public Map<String, Size> foo;
    public Policy policy;
  }

  public static class MyBaseImplementation1 implements MyInterface {
    public Policy policy;
  }

}
