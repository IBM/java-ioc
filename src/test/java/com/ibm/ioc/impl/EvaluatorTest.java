/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import com.ibm.ioc.ConfigurationException;
import com.ibm.ioc.ObjectInitializationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// TODO: Describe class or interface
public class EvaluatorTest {
  @Before
  public void setup() {
    // DOMConfigurator.configure(System.getProperty("log4j.configuration"));
  }

  @Test
  public void factoryTestPositive() throws Exception {
    Assert.assertTrue(LiteralEvaluatorFactory.hasEvaluator("int"));
    Assert.assertTrue(LiteralEvaluatorFactory.hasEvaluator("double"));
    Assert.assertTrue(LiteralEvaluatorFactory.hasEvaluator("short"));
    Assert.assertTrue(LiteralEvaluatorFactory.hasEvaluator("byte"));
    Assert.assertTrue(LiteralEvaluatorFactory.hasEvaluator("long"));

    LiteralEvaluator ev = new StringLiteral("10");
    Assert.assertEquals(ev.evaluate().getClass(),
        String.class);
    Assert.assertEquals("10", ev.evaluate());
    ev = LiteralEvaluatorFactory.getEvaluator("string", "11");
    Assert.assertEquals(ev.evaluate().getClass(),
        String.class);
    Assert.assertEquals("11", ev.evaluate());

    ev = new IntegerLiteral(5);
    Assert.assertEquals(ev.evaluate().getClass(),
        Integer.class);
    Assert.assertEquals(5, ev.evaluate());
    ev = new IntegerLiteral("10");
    Assert.assertEquals(ev.evaluate().getClass(),
        Integer.class);
    Assert.assertEquals(10, ev.evaluate());
    ev = LiteralEvaluatorFactory.getEvaluator("int", "11");
    Assert.assertEquals(ev.evaluate().getClass(),
        Integer.class);
    Assert.assertEquals(11, ev.evaluate());

    ev = new LongLiteral(9999999999L);
    Assert.assertEquals(ev.evaluate().getClass(),
        Long.class);
    Assert.assertEquals(9999999999L, ev.evaluate());
    ev = new LongLiteral("1000000000000000000");
    Assert.assertEquals(ev.evaluate().getClass(),
        Long.class);
    Assert.assertEquals(1000000000000000000L, ev.evaluate());
    ev = LiteralEvaluatorFactory.getEvaluator("long", "11");
    Assert.assertEquals(ev.evaluate().getClass(),
        Long.class);
    Assert.assertEquals(11L, ev.evaluate());

    ev = new ShortLiteral((short) 777);
    Assert.assertEquals(ev.evaluate().getClass(),
        Short.class);
    Assert.assertEquals(ev.evaluate(), (short) 777);

    ev = new ShortLiteral("999");
    Assert.assertEquals(ev.evaluate().getClass(),
        Short.class);
    Assert.assertEquals(ev.evaluate(), (short) 999);
    ev = LiteralEvaluatorFactory.getEvaluator("short", "1000");
    Assert.assertEquals(ev.evaluate().getClass(),
        Short.class);
    Assert.assertEquals(ev.evaluate(),
        (short) 1000);

    ev = new ByteLiteral((byte) 125);
    Assert.assertEquals(ev.evaluate().getClass(),
        Byte.class);
    Assert.assertEquals(ev.evaluate(), (byte) 125);
    ev = new ByteLiteral("127");
    Assert.assertEquals(ev.evaluate().getClass(),
        Byte.class);
    Assert.assertEquals(ev.evaluate(), (byte) 127);
    ev = LiteralEvaluatorFactory.getEvaluator("byte", "10");
    Assert.assertEquals(ev.evaluate().getClass(),
        Byte.class);
    Assert.assertEquals(ev.evaluate(), (byte) 10);

    ev = new FloatLiteral(1.1f);
    Assert.assertEquals(ev.evaluate().getClass(),
        Float.class);
    float v0 =
        ((Float) ev.evaluate()).floatValue();
    Assert.assertTrue(Math.abs(v0 - (float) 1.1) < 0.00000001);
    ev = new FloatLiteral("127.11");
    Assert.assertEquals(ev.evaluate().getClass(),
        Float.class);
    v0 = ((Float) ev.evaluate()).floatValue();
    Assert.assertTrue(Math.abs(v0 - (float) 127.11) < 0.00000001);
    ev = LiteralEvaluatorFactory.getEvaluator("float", "2.11111E5");
    v0 = ((Float) ev.evaluate()).floatValue();
    Assert.assertEquals(ev.evaluate().getClass(),
        Float.class);
    Assert.assertTrue((Math.abs(v0 - 211111)) < 0.00000001);

    ev = new DoubleLiteral(127.1111111111111111111);
    Assert.assertEquals(ev.evaluate().getClass(),
        Double.class);
    double v1 =
        ((Double) ev.evaluate()).doubleValue();
    Assert.assertTrue(Math.abs(v1 - 127.1111111111111111111) < 0.00000001);
    ev = new DoubleLiteral("127.11");
    Assert.assertEquals(ev.evaluate().getClass(),
        Double.class);
    v1 = ((Double) ev.evaluate()).doubleValue();
    Assert.assertTrue(Math.abs(v1 - 127.11) < 0.00000001);
    ev = LiteralEvaluatorFactory.getEvaluator("double", "2.11111E5");
    v1 = ((Double) ev.evaluate()).doubleValue();
    Assert.assertEquals(ev.evaluate().getClass(),
        Double.class);
    Assert.assertTrue((Math.abs(v1 - 211111)) < 0.00000001);

    ev = new BooleanLiteral(false);
    Assert.assertEquals(ev.evaluate().getClass(),
        Boolean.class);
    Assert.assertFalse((boolean) ev.evaluate());
    ev = new BooleanLiteral("true");
    Assert.assertEquals(ev.evaluate().getClass(),
        Boolean.class);
    Assert.assertTrue((boolean) ev.evaluate());
    ev = LiteralEvaluatorFactory.getEvaluator("boolean", "false");
    Assert.assertEquals(ev.evaluate().getClass(),
        Boolean.class);
    Assert.assertFalse((boolean) ev.evaluate());
  }

  @Test(expected = RuntimeException.class)
  public void factoryTestNegative2() throws Exception {
    LiteralEvaluatorFactory.getEvaluator("Integer", "12");
  }

  @Test
  public void factoryTestNegative() {
    try {
      new IntegerLiteral("10.a");
      Assert.fail("Read int: 10");
    } catch (final Exception ex) {
    }
    try {
      new ByteLiteral("260");
      Assert.fail("Read byte: 260");
    } catch (final Exception ex) {
    }
  }

  @Test(expected = RuntimeException.class)
  public void factoryTestNegative1() {
    new IntegerLiteral("10.a");
  }

  @Test
  public void instanceEvaluatorTest() throws Exception {
    final UUID uuid = UUID.fromString("773259c0-dbb4-4f87-8367-f5c7d6c4394b");
    final UUID uuid1 = UUID.fromString("773259c0-dbb4-4f87-ffff-f5c7d6c4394b");

    InstanceMethodEvaluator inst =
        new InstanceMethodEvaluator(uuid, "compareTo",
            Arrays.asList(new Evaluatable[] {new ReferenceEvaluator<>(uuid1, null)}));
    Object res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Integer.class);
    Assert.assertNotSame(0, res);

    final int cmp = ((Integer) (res)).intValue();
    Assert.assertEquals(1, Math.abs(cmp));

    inst =
        new InstanceMethodEvaluator(uuid, "getLeastSignificantBits",
            Arrays.asList(new Evaluatable[] {}));
    res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Long.class);
    Assert.assertEquals(res, uuid.getLeastSignificantBits());
  }

  @Test
  public void staticEvaluatorTest() throws Exception {
    final UUID uuid = UUID.fromString("773259c0-dbb4-4f87-8367-f5c7d6c4394b");

    final StaticMethodEvaluator stat =
        new StaticMethodEvaluator(UUID.class, "fromString",
            Arrays.asList(new Evaluatable[] {new StringLiteral(
                "773259c0-dbb4-4f87-8367-f5c7d6c4394b")}));

    final Object res = stat.evaluate();
    Assert.assertEquals(UUID.class, res.getClass());
    Assert.assertEquals(0, uuid.compareTo((UUID) res));
  }

  @Test(expected = ConfigurationException.class)
  public void negativeStaticEvaluatorTest1() throws ConfigurationException {
    final StaticMethodEvaluator stat =
        new StaticMethodEvaluator(UUID.class, "fromString",
            Arrays.asList(new Evaluatable[] {new IntegerLiteral(99)}));
    stat.evaluate();
  }

  @Test
  public void complexEvaluatorTest() throws Exception {
    final UUID uuid = UUID.fromString("773259c0-dbb4-4f87-8367-f5c7d6c4394b");
    final long mostSigBits = uuid.getMostSignificantBits();

    final StaticMethodEvaluator stat =
        new StaticMethodEvaluator(UUID.class, "fromString",
            Arrays.asList(new Evaluatable[] {new StringLiteral(
                "773259c0-dbb4-4f87-8367-f5c7d6c4394b")}));
    final InstanceMethodEvaluator inst =
        new InstanceMethodEvaluator(
            stat.evaluate(),
            "getMostSignificantBits", Arrays.asList(new Evaluatable[] {}));

    final Object res = inst.evaluate();
    Assert.assertEquals(Long.class, res.getClass());
    final long msb = ((Long) (res)).longValue();
    Assert.assertEquals(mostSigBits, msb);
  }

  @Test
  public void conversionPrimitiveEvaluatorTest() throws Exception {
    final String sv = "123456789";

    InstanceMethodEvaluator inst =
        new InstanceMethodEvaluator(sv, "charAt",
            Arrays.asList(new Evaluatable[] {new IntegerLiteral(1)}));

    Object res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Character.class);
    Assert.assertEquals('2', res);

    inst =
        new InstanceMethodEvaluator(sv, "charAt",
            Arrays.asList(new Evaluatable[] {new ShortLiteral((short) 2)}));

    res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Character.class);
    Assert.assertEquals('3', res);
  }

  @Test
  public void conversionEvaluatorTest() throws Exception {
    String sv = new String("A");

    InstanceMethodEvaluator inst =
        new InstanceMethodEvaluator(sv, "equals",
            Arrays.asList(new Evaluatable[] {new StringLiteral("A")}));

    Object res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Boolean.class);
    Assert.assertTrue((boolean) res);

    inst =
        new InstanceMethodEvaluator(sv, "equals",
            Arrays.asList(new Evaluatable[] {new ReferenceEvaluator<>(new Object(),
                null)}));

    res = inst.evaluate();
    Assert.assertEquals(res.getClass(), Boolean.class);
    Assert.assertFalse((boolean) res);

    sv = "0123456";

    inst =
        new InstanceMethodEvaluator(sv, "substring", Arrays.asList(new Evaluatable[] {
            new IntegerLiteral(2), new IntegerLiteral(5)}));
    res = inst.evaluate();
    Assert.assertEquals(res.getClass(), String.class);
    Assert.assertEquals("234", res);
  }

  @Test
  public void namedEvaluatorTest() throws Exception {
    final String name = "my parameter";
    final int value = 5;
    final LiteralEvaluator le = new IntegerLiteral(value);
    final NamedEvaluator ne = new NamedEvaluator(name, true, le);

    final Object o = ne.evaluate();
    Assert.assertEquals(o, Integer.valueOf(value));
    Assert.assertEquals(ne.getName(), name);
  }

  @Test
  public void referenceEvaluatorType1() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();

    final boolean arg = true;
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("boolean-scalar", true,
                new BooleanLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(arg, ma.isBooleanScalar());
  }

  @Test
  public void referenceEvaluatorType2() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();

    final boolean arg = false;
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("boolean-scalar", true,
                new BooleanLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(arg, ma.isBooleanScalar());
  }

  @Test
  public void referenceEvaluatorType3() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final String arg = "true";
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("boolean-scalar", true,
                new StringLiteral(arg))));
    re.evaluate();
    Assert.assertTrue(ma.isBooleanScalar());
  }

  @Test
  public void referenceEvaluatorType4() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final String arg = "false";
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("boolean-scalar", true,
                new StringLiteral(arg))));
    re.evaluate();
    Assert.assertFalse(ma.isBooleanScalar());
  }

  @Test
  public void referenceEvaluatorType5() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final int arg = 25;
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", true,
                new IntegerLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(arg, ma.getIntScalar());
  }

  @Test
  public void referenceEvaluatorType6() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final String arg = "36";
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", true,
                new StringLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(36, ma.getIntScalar());
  }

  @Test
  public void referenceEvaluatorType7() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final String arg = "0x11";
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", true,
                new StringLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(17, ma.getIntScalar());
  }

  @Test
  public void referenceEvaluatorType8() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final double arg = 33.33;
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("double-scalar", true,
                new DoubleLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(arg, ma.getDoubleScalar(), 00001);
  }

  @Test
  public void referenceEvaluatorType9() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final String arg = "44.44";
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("double-scalar", true,
                new StringLiteral(arg))));
    re.evaluate();
    Assert.assertEquals(Double.valueOf(arg), ma.getDoubleScalar(), 0.0001);
  }

  @Test
  public void referenceEvaluatorType10() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final UUID arg = UUID.randomUUID();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("UUID-scalar", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return arg;
                  }
                })));
    re.evaluate();
    Assert.assertEquals(arg, ma.getUUIDScalar());
  }

  @Test
  public void referenceEvaluatorType11() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final UUID arg = UUID.randomUUID();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("UUID-scalar", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return arg.toString();
                  }
                })));
    re.evaluate();
    Assert.assertEquals(arg, ma.getUUIDScalar());
  }

  @Test
  public void referenceEvaluatorTypeNotRequired1() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", false,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return null;
                  }
                })));
    re.evaluate();
    Assert.assertEquals(777, ma.getIntScalar());
  }

  @Test
  public void referenceEvaluatorTypeNotRequired2() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("public-int-scalar", false,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return null;
                  }
                })));
    re.evaluate();
    Assert.assertEquals(-1, ma.publicIntScalar);
  }

  @Test(expected = ObjectInitializationException.class)
  public void referenceEvaluatorInvalidValue() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", false,
                new BooleanLiteral(false))));
    re.evaluate();
  }

  @Test(expected = ObjectInitializationException.class)
  public void referenceEvaluatorTypeRequired2() throws ObjectInitializationException {
    final MyObjectWithArray1 ma = new MyObjectWithArray1();
    final ReferenceEvaluator<MyObjectWithArray1> re =
        new ReferenceEvaluator<>(ma,
            Arrays.asList(new NamedEvaluator("int-scalar", true,
                new Evaluatable() {
                  @Override
                  public Object evaluate(final Map<Class<?>, String> overrides) {
                    return null;
                  }
                })));
    re.evaluate();
  }

  static class MyObjectWithArray1 {
    public int publicIntScalar = -1;

    private boolean booleanScalar;
    private int intScalar = 777;
    private double doubleScalar;
    private UUID UUIDScalar;

    private boolean[] booleanArray;
    private int[] intArray;
    private double[] doubleArray;
    private UUID[] UUIDarray;

    public boolean[] getBooleanArray() {
      return this.booleanArray;
    }

    public void setBooleanArray(final boolean[] booleanArray) {
      this.booleanArray = booleanArray;
    }

    public int[] getIntArray() {
      return this.intArray;
    }

    public void setIntArray(final int[] intArray) {
      this.intArray = intArray;
    }

    public double[] getDoubleArray() {
      return this.doubleArray;
    }

    public void setDoubleArray(final double[] doubleArray) {
      this.doubleArray = doubleArray;
    }

    public UUID[] getUUIDarray() {
      return this.UUIDarray;
    }

    public void setUUIDarray(final UUID[] darray) {
      this.UUIDarray = darray;
    }

    public boolean isBooleanScalar() {
      return this.booleanScalar;
    }

    public void setBooleanScalar(final boolean booleanScalar) {
      this.booleanScalar = booleanScalar;
    }

    public int getIntScalar() {
      return this.intScalar;
    }

    public void setIntScalar(final int intScalar) {
      this.intScalar = intScalar;
    }

    public double getDoubleScalar() {
      return this.doubleScalar;
    }

    public void setDoubleScalar(final double doubleScalar) {
      this.doubleScalar = doubleScalar;
    }

    public UUID getUUIDScalar() {
      return this.UUIDScalar;
    }

    public void setUUIDScalar(final UUID scalar) {
      this.UUIDScalar = scalar;
    }

  }

  public static class MyObject {
    private final int myIntParam;

    public MyObject(final int myIntParam) {
      this.myIntParam = myIntParam;
    }

    public int getMyIntParam() {
      return this.myIntParam;
    }
  }

  public static class MyObjectBuilder {
    private int myIntParam;

    public MyObjectBuilder() {}

    public void setMyIntParam(final int myIntParam) {
      this.myIntParam = myIntParam;
    }

    public MyObject build() {
      return new MyObject(this.myIntParam);
    }
  }

}
