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

public class WithRefTest {

  @Test
  public void testDefaultInterface() throws Exception {
    final JavaBindingsProvider provider =
        new JavaBindingsProvider(
            Arrays.asList(InterfaceBinding.class));
    final ImplementationFactory<A> AFactory = provider.getDefaultImplementation(A.class);
    Assert.assertNull(AFactory);

    final C c1 = provider.getImplementation(C.class, "1").initialize();
    Assert.assertEquals(AImpl1.class, c1.a.getClass());
    Assert.assertEquals(AImpl1.class, c1.b.a.getClass());
    Assert.assertTrue(c1.b.a == c1.a);

    final C c2 = provider.getImplementation(C.class, "2").initialize();
    Assert.assertEquals(AImpl2.class, c2.a.getClass());
    Assert.assertEquals(AImpl2.class, c2.b.a.getClass());
    Assert.assertTrue(c2.b.a == c2.a);

    final C c3 = provider.getImplementation(C.class, "3").initialize();
    Assert.assertEquals(AImpl1.class, c3.a.getClass());
    Assert.assertEquals(AImpl2.class, c3.b.a.getClass());

    final C c4 = provider.getImplementation(C.class, "4").initialize();
    Assert.assertEquals(AImpl2.class, c4.a.getClass());
    Assert.assertEquals(AImpl1.class, c4.b.a.getClass());

    final D d = provider.getDefaultImplementation(D.class).initialize();
    Assert.assertEquals(AImpl2.class, d.c.a.getClass());
    Assert.assertEquals(AImpl1.class, d.c.b.a.getClass());
    Assert.assertEquals(AImpl2.class, d.a.getClass());
    Assert.assertTrue(d.c.a == d.a);

    final D d1 = provider.getImplementation(D.class, "1").initialize();
    Assert.assertEquals(AImpl1.class, d1.c.a.getClass());
    Assert.assertEquals(AImpl2.class, d1.c.b.a.getClass());
    Assert.assertEquals(AImpl1.class, d1.a.getClass());
    Assert.assertTrue(d1.c.a == d1.a);

    final E e1 = provider.getImplementation(E.class, "1").initialize();
    final E e2 = provider.getImplementation(E.class, "2").initialize();
    Assert.assertTrue(e1.d == e2.d);

    final E e3 = provider.getImplementation(E.class, "3").initialize();
    final E e4 = provider.getImplementation(E.class, "4").initialize();
    Assert.assertTrue(e3.d == e4.d);

    final F f1 = provider.getImplementation(F.class, "1").initialize();
    final F f2 = provider.getImplementation(F.class, "2").initialize();
    Assert.assertTrue(f1.e == f2.e);

    final D d2 = provider.getImplementation(D.class, "2").initialize();
    Assert.assertEquals(AImpl2.class, d2.c.a.getClass());
    Assert.assertEquals(AImpl1.class, d2.c.b.a.getClass());
    Assert.assertEquals(AImpl1.class, d2.a.getClass());
    Assert.assertTrue(d2.c.b.a == d2.a);

    final D d3 = provider.getImplementation(D.class, "3").initialize();
    Assert.assertEquals(AImpl2.class, d3.c.a.getClass());
    Assert.assertEquals(AImpl1.class, d3.c.b.a.getClass());
    Assert.assertEquals(AImpl2.class, d3.a.getClass());
    Assert.assertTrue(d3.c.a == d3.a);

    Assert.assertTrue(d3.c == d2.c);
  }

  public static class InterfaceBinding implements JavaBindings {
    @Override
    public void register(final BindingsFactory def) {
      def.def(AImpl1.class)
          .iface(A.class, "1");
      def.def(AImpl2.class)
          .iface(A.class, "2");

      def.def(B.class)
          .ref("a", A.class);

      def.def(C.class)
          .iface(C.class, "1")
          .ref("a", A.class)
          .ref("b", B.class)
          .withRef(A.class, "1");

      def.def(C.class)
          .iface(C.class, "2")
          .ref("a", A.class)
          .ref("b", B.class)
          .withRef(A.class, "2");

      def.def(C.class)
          .iface(C.class, "3")
          .ref("a", A.class, "1")
          .ref("b", B.class)
          .withRef(A.class, "2");

      def.def(C.class)
          .iface(C.class, "4")
          .ref("a", A.class, "2")
          .ref("b", B.class)
          .withRef(A.class, "1");

      def.def(D.class, D.class)
          .ref("c", C.class)
          .ref("a", A.class)
          .withRef(C.class, "4")
          .withRef(A.class, "2");

      def.def(D.class)
          .iface(D.class, "1")
          .ref("c", C.class)
          .ref("a", A.class)
          .withRef(C.class, "3")
          .withRef(A.class, "1");

      def.def(E.class)
          .iface(E.class, "1")
          .ref("d", D.class);

      def.def(E.class)
          .iface(E.class, "2")
          .ref("d", D.class);

      def.def(E.class)
          .iface(E.class, "3")
          .ref("d", D.class, "1")
          .withRef(A.class, "2");

      def.def(E.class)
          .iface(E.class, "4")
          .ref("d", D.class, "1")
          .withRef(A.class, "2");

      def.def(F.class)
          .iface(F.class, "1")
          .ref("e", E.class, "2")
          .withRef(A.class, "xxxxxxxxxx!");

      def.def(F.class)
          .iface(F.class, "2")
          .ref("e", E.class, "2")
          .withRef(A.class, "yyyyyyyyy@!");

      def.def(B.class)
          .iface(B.class, "2")
          .ref("a", A.class)
          .withRef(A.class, "1");

      def.def(C.class)
          .iface(C.class, "5")
          .ref("a", A.class)
          .ref("b", B.class, "2")
          .withRef(A.class, "2");

      def.def(D.class)
          .iface(D.class, "2")
          .ref("c", C.class, "5")
          .ref("a", A.class)
          .withRef(A.class, "1");

      def.def(D.class)
          .iface(D.class, "3")
          .ref("c", C.class, "5")
          .ref("a", A.class)
          .withRef(A.class, "2");
    }
  }

  public interface A {
  }

  public static class AImpl1 implements A {
  }

  public static class AImpl2 implements A {
  }

  public static class B {
    public A a;
  }

  public static class C {
    public B b;
    public A a;
  }

  public static class D {
    public C c;
    public A a;
  }

  public static class E {
    public D d;
  }

  public static class F {
    public E e;
  }
}
