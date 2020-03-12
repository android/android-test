package androidx.test.testing.fixtures;

import static org.junit.Assert.fail;

import androidx.test.internal.runner.TestRequestBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Fixture used to test {@link TestRequestBuilder} */
@RunWith(JUnit4.class)
@Ignore
public class JUnit4Ignored {
  @Test
  public void testBroken() {
    fail("expected this test to be ignored");
  }
}
