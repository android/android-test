package androidx.test.testing.fixtures;

import androidx.test.filters.SmallTest;
import junit.framework.TestCase;

public class SampleJUnit3Test extends TestCase {

  @SmallTest
  public void testSmall() {}

  @SmallTest
  public void testSmall2() {}

  public void testOther() {}
}
