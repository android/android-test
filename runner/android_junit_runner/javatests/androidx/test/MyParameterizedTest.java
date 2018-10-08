/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test;

import android.app.Instrumentation;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Placeholder to verify {@link Instrumentation} can be injected into {@link Parameterized} tests
 */
@Suppress // b/26110951
@RunWith(Parameterized.class)
@SmallTest
public class MyParameterizedTest {

  @Parameters(name = "{index}: fib({0})={1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{0, 0}, {1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 5}, {6, 8}});
  }

  private final int mInput;
  private final int mExpected;

  public MyParameterizedTest(int input, int expected) {
    mInput = input;
    mExpected = expected;
  }

  @Test
  public void testFib() {
    Assert.assertEquals(mExpected, fibonacci(mInput));
    // verify Instrumentation was injected in a Parameterized test
    Assert.assertNotNull(InstrumentationRegistry.getInstrumentation());
  }

  private int fibonacci(int n) {
    if (n == 0 || n == 1) return n;
    else return fibonacci(n - 1) + fibonacci(n - 2);
  }
}
