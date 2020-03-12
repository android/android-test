package androidx.test.testing.fixtures;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Test fixture for verifying support for suite() methods with tests. */
public class JUnit3SuiteWithTest extends TestCase {
  public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(SampleJUnit3Test.class);
    return suite;
  }

  public void testPass() {}
}
