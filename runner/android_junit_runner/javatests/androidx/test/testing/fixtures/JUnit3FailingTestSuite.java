package androidx.test.testing.fixtures;

import androidx.test.internal.runner.TestRequestBuilder;
import junit.framework.TestSuite;

/** Fixture used to test {@link TestRequestBuilder} */
public class JUnit3FailingTestSuite extends TestSuite {

  public JUnit3FailingTestSuite() {
    addTestSuite(JUnit3FailingTestCase.class);
  }
}
