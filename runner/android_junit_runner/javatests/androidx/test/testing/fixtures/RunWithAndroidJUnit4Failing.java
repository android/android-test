package androidx.test.testing.fixtures;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.TestRequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Fixture used to test {@link TestRequestBuilder} */
@RunWith(AndroidJUnit4.class)
public class RunWithAndroidJUnit4Failing {
  @Test
  public void testBroken() {
    fail("broken");
  }
}
