package androidx.test.testing.fixtures;

import junit.framework.TestCase;

/** Fixture to test timeout functionality for a JUnit3 style test. Used in TimeoutTest */
public class JUnit3StyleTimeoutClass extends TestCase {

  public static final int GLOBAL_ARG_TIMEOUT = 100;
  public static final int GLOBAL_RULE_TIMEOUT = 50;
  public static final int TEST_TIMEOUT = 25;
  public static final int WAIT_FOR_TIMEOUT = 25;

  public void testArgTimeoutInterrupts() throws InterruptedException {
    Thread.sleep(GLOBAL_ARG_TIMEOUT + WAIT_FOR_TIMEOUT);
  }

  public void testArgTimeoutInterruptsThatThrows() throws InterruptedException {
    Thread.sleep(GLOBAL_ARG_TIMEOUT + WAIT_FOR_TIMEOUT);
    throw new RuntimeException("Test threw RuntimeException");
  }

  public void testArgTimeoutInterruptsThatFails() throws InterruptedException {
    Thread.sleep(GLOBAL_ARG_TIMEOUT + WAIT_FOR_TIMEOUT);
    fail("This is a failing Test");
  }

  public void testPassingTest() {
    // pass
  }
}
