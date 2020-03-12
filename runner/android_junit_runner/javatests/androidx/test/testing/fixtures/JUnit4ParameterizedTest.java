package androidx.test.testing.fixtures;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JUnit4ParameterizedTest {
  @Parameterized.Parameters(name = "{index}: foo({0})={1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{0, 0}, {1, 1}, {2, 2}});
  }

  @Test
  public void testFoo() {
    fail("broken");
  }
}
