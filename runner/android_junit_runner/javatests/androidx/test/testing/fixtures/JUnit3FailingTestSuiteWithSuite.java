package androidx.test.testing.fixtures;

import junit.framework.TestSuite;

/** Fixture used to test {@link androidx.test.internal.runner.AndroidLogOnlyBuilder} */
public class JUnit3FailingTestSuiteWithSuite extends TestSuite {

  public JUnit3FailingTestSuiteWithSuite() {
    addTestSuite(JUnit3FailingTestCase.class);
  }

  public static junit.framework.Test suite() {
    return new JUnit3FailingTestSuiteWithSuite();
  }
}
