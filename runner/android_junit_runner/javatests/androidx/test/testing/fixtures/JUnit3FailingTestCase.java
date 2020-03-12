package androidx.test.testing.fixtures;

import androidx.test.internal.runner.TestRequestBuilder;
import junit.framework.TestCase;

/** Fixture used to test {@link TestRequestBuilder} */
public class JUnit3FailingTestCase extends TestCase {
  public void testBroken() {
    fail("broken");
  }
}
