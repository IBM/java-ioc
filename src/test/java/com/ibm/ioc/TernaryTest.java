/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.ioc.JavaBindingsProvider.BindingsFactory;
import com.ibm.ioc.impl.ImplementationFactory;

public class TernaryTest {

  @Test
  public void testDefaultInterface() throws Exception {
    System.setProperty("ternaryTest.enabled1", "true");
    System.setProperty("ternaryTest.enabled2", "false");
    System.setProperty("ternaryTest.enabled4", "true");
    System.setProperty("ternaryTest.enabled5", "false");

    System.setProperty("ternaryTest.enabled7", "true");
    System.setProperty("ternaryTest.enabled8", "false");
    System.setProperty("ternaryTest.enabled10", "true");
    System.setProperty("ternaryTest.enabled11", "false");
    System.setProperty("ternaryTest.enabled13", "true");
    System.setProperty("ternaryTest.enabled14", "false");
    System.setProperty("ternaryTest.enabled16", "true");
    System.setProperty("ternaryTest.enabled17", "false");

    final JavaBindingsProvider provider =
        new JavaBindingsProvider(
            Arrays.asList(InterfaceBinding.class));

    final ImplementationFactory<MyImpl> myImplFactory =
        provider.getDefaultImplementation(MyImpl.class);
    final MyImpl myImpl = myImplFactory.initialize();

    Assert.assertEquals(Policy1.class, myImpl.policy1.getClass());
    Assert.assertEquals(Policy2.class, myImpl.policy2.getClass());
    Assert.assertEquals(Policy2.class, myImpl.policy3.getClass());
    Assert.assertEquals(DefaultPolicy.class, myImpl.policy4.getClass());
    Assert.assertEquals(Policy2.class, myImpl.policy5.getClass());
    Assert.assertEquals(Policy2.class, myImpl.policy6.getClass());


    final Policy t1 =
        provider.getImplementation(Policy.class, "t1").initialize();
    Assert.assertEquals(Policy1.class, t1.getClass());
    final Policy t2 =
        provider.getImplementation(Policy.class, "t2").initialize();
    Assert.assertEquals(Policy2.class, t2.getClass());
    final Policy t3 =
        provider.getImplementation(Policy.class, "t3").initialize();
    Assert.assertEquals(Policy2.class, t3.getClass());
    final Policy t4 =
        provider.getImplementation(Policy.class, "t4").initialize();
    Assert.assertEquals(DefaultPolicy.class, t4.getClass());
    final Policy t5 =
        provider.getImplementation(Policy.class, "t5").initialize();
    Assert.assertEquals(Policy2.class, t5.getClass());
    final Policy t6 =
        provider.getImplementation(Policy.class, "t6").initialize();
    Assert.assertEquals(Policy2.class, t6.getClass());

    final P1 p1 = provider.getDefaultImplementation(P1.class).initialize();
    Assert.assertEquals(Policy1.class, p1.getClass());
    final P2 p2 = provider.getDefaultImplementation(P2.class).initialize();
    Assert.assertEquals(Policy2.class, p2.getClass());
    final P3 p3 = provider.getDefaultImplementation(P3.class).initialize();
    Assert.assertEquals(Policy2.class, p3.getClass());
    final P4 p4 = provider.getDefaultImplementation(P4.class).initialize();
    Assert.assertEquals(DefaultPolicy.class, p4.getClass());
    final P5 p5 = provider.getDefaultImplementation(P5.class).initialize();
    Assert.assertEquals(Policy2.class, p5.getClass());
    final P6 p6 = provider.getDefaultImplementation(P6.class).initialize();
    Assert.assertEquals(Policy2.class, p6.getClass());

  }

  public static class InterfaceBinding implements JavaBindings {
    @Override
    public void register(final BindingsFactory def) {
      def.def(Policy1.class)
          .iface(Policy.class, "p1");

      def.def(Policy2.class)
          .iface(Policy.class, "p2");

      def.def(DefaultPolicy.class)
          .iface(Policy.class);

      def.ternaryDef("ternaryTest.enabled1", Policy.class, "p1", "p2")
          .iface(Policy.class, "policy1");
      def.ternaryDef("ternaryTest.enabled2", Policy.class, "p1", "p2")
          .iface(Policy.class, "policy2");
      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p1", "p2")
          .iface(Policy.class, "policy3");
      def.ternaryDef("ternaryTest.enabled4", Policy.class, "p2")
          .iface(Policy.class, "policy4");
      def.ternaryDef("ternaryTest.enabled5", Policy.class, "p2")
          .iface(Policy.class, "policy5");
      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p2")
          .iface(Policy.class, "policy6");

      def.def(MyImpl.class)
          .ref("policy1", Policy.class, "policy1")
          .ref("policy2", Policy.class, "policy2")
          .ref("policy3", Policy.class, "policy3")
          .ref("policy4", Policy.class, "policy4")
          .ref("policy5", Policy.class, "policy5")
          .ref("policy6", Policy.class, "policy6");

      def.ternaryDef("ternaryTest.enabled7", Policy.class, "p1", "p2")
          .iface(Policy.class, "t1");

      def.ternaryDef("ternaryTest.enabled8", Policy.class, "p1", "p2")
          .iface(Policy.class, "t2");

      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p1", "p2")
          .iface(Policy.class, "t3");

      def.ternaryDef("ternaryTest.enabled10", Policy.class, "p2")
          .iface(Policy.class, "t4");

      def.ternaryDef("ternaryTest.enabled11", Policy.class, "p2")
          .iface(Policy.class, "t5");

      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p2")
          .iface(Policy.class, "t6");

      def.ternaryDef("ternaryTest.enabled13", Policy.class, "p1", "p2")
          .iface(P1.class);

      def.ternaryDef("ternaryTest.enabled14", Policy.class, "p1", "p2")
          .iface(P2.class);

      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p1", "p2")
          .iface(P3.class);

      def.ternaryDef("ternaryTest.enabled16", Policy.class, "p2")
          .iface(P4.class);

      def.ternaryDef("ternaryTest.enabled17", Policy.class, "p2")
          .iface(P5.class);

      def.ternaryDef("ternaryTest.propNotSet", Policy.class, "p2")
          .iface(P6.class);
    }
  }


  public interface P1 {
  }
  public interface P2 {
  }
  public interface P3 {
  }
  public interface P4 {
  }
  public interface P5 {
  }
  public interface P6 {
  }

  public interface Policy extends P1, P2, P3, P4, P5, P6 {
  }

  public static class Policy1 implements Policy {
  }

  public static class Policy2 implements Policy {
  }
  public static class DefaultPolicy implements Policy {
  }

  public static class MyImpl {
    public Policy policy1;
    public Policy policy2;
    public Policy policy3;
    public Policy policy4;
    public Policy policy5;
    public Policy policy6;
  }
}
