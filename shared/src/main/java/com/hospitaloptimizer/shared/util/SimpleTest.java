package com.hospitaloptimizer.shared.util;

import java.lang.reflect.Method;
import java.util.*;

/**
 * A tiny, dependency-free unit-test framework.
 *
 * WHY NOT JUNIT: this container's network is restricted to a small allow-list
 * of package registries that does not include Maven Central, so JUnit jars
 * cannot be reliably fetched at build time. Rather than make the whole team's
 * build depend on an internet connection, every pod writes plain classes
 * whose public "testXxx()" methods are discovered and run by this runner --
 * same idea as JUnit, zero setup.
 *
 * Usage: extend nothing, just write a class with public void testFoo() methods
 * that call the assert* helpers below, then call SimpleTest.run(YourClass.class).
 */
public final class SimpleTest {

    private static int totalPass = 0;
    private static int totalFail = 0;
    private static final List<String> failures = new ArrayList<>();

    private SimpleTest() {}

    public static void run(Class<?> testClass) {
        int pass = 0, fail = 0;
        Object instance;
        try {
            instance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("FAILED to instantiate " + testClass.getSimpleName() + ": " + e);
            return;
        }
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getName().startsWith("test") && m.getParameterCount() == 0) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                    pass++;
                    totalPass++;
                } catch (Exception e) {
                    fail++;
                    totalFail++;
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String msg = testClass.getSimpleName() + "." + m.getName() + " -> " + cause;
                    failures.add(msg);
                    System.out.println("  FAIL: " + msg);
                }
            }
        }
        System.out.printf("%-45s %d passed, %d failed%n", testClass.getSimpleName(), pass, fail);
    }

    public static void printSummary() {
        System.out.println("----------------------------------------------------");
        System.out.println("TOTAL: " + totalPass + " passed, " + totalFail + " failed (" + (totalPass + totalFail) + " tests run)");
        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            failures.forEach(f -> System.out.println("  - " + f));
        }
    }

    public static boolean allPassed() {
        return totalFail == 0;
    }

    // ---- assertion helpers ----

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("assertTrue failed: " + message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("assertFalse failed: " + message);
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + "> (delta " + delta + ")");
        }
    }

    public static void assertNull(Object o) {
        if (o != null) throw new AssertionError("expected null but was <" + o + ">");
    }

    public static void assertNotNull(Object o) {
        if (o == null) throw new AssertionError("expected non-null value");
    }

    public static void assertThrows(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) return;
            throw new AssertionError("expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        throw new AssertionError("expected " + expected.getSimpleName() + " but nothing was thrown");
    }
}
