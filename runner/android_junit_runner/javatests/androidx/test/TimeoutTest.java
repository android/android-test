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

import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.TestRequestBuilder;
import java.util.List;
import junit.framework.TestCase;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TimeoutTest {
  private static final int GLOBAL_ARG_TIMEOUT = 100;
  private static final int GLOBAL_RULE_TIMEOUT = 50;
  private static final int TEST_TIMEOUT = 25;
  private static final int WAIT_FOR_TIMEOUT = 25;

  private TestRequestBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new TestRequestBuilder(getInstrumentation(), getArguments());
  }

  @Test
  public void testTimeoutsInJUnit4WithRule() {
    Request request =
        builder
            .addTestClass(JUnit4WithRuleClass.class.getName())
            .setPerTestTimeout(GLOBAL_ARG_TIMEOUT)
            .build();
    JUnitCore junitCore = new JUnitCore();
    Result result = junitCore.run(request);
    assertThat(result.getFailures(), isEmpty());
    assertEquals(2, result.getRunCount());
  }

  @Test
  public void testTimeoutsInJUnit4WithNoRule() {
    Request request =
        builder
            .addTestClass(JUnit4NoRuleClass.class.getName())
            .setPerTestTimeout(GLOBAL_ARG_TIMEOUT)
            .build();
    JUnitCore junitCore = new JUnitCore();
    Result result = junitCore.run(request);
    assertThat(result.getFailures(), isEmpty());
    assertEquals(2, result.getRunCount());
  }

  /** Ensure that the combination of timing out and passing tests are all reported correctly */
  @Test
  public void testTimeoutInJUnit3Style() {
    Request request =
        builder
            .addTestClass(JUnit3StyleClass.class.getName())
            .setPerTestTimeout(GLOBAL_ARG_TIMEOUT)
            .build();
    JUnitCore junitCore = new JUnitCore();
    Result result = junitCore.run(request);
    assertEquals(3, result.getFailures().size());
    assertEquals(
        String.format("Test timed out after %s milliseconds", GLOBAL_ARG_TIMEOUT),
        result.getFailures().get(0).getMessage());
    assertEquals(
        String.format("Test timed out after %s milliseconds", GLOBAL_ARG_TIMEOUT),
        result.getFailures().get(1).getMessage());
    assertEquals(
        String.format("Test timed out after %s milliseconds", GLOBAL_ARG_TIMEOUT),
        result.getFailures().get(2).getMessage());
  }

  /**
   * Tests that don't timeout but still fail due to RuntimeException or Assertions should still
   * propagate the correct error back to the user.
   */
  @Test
  public void testJUnit3TimeoutTestsThatFailButNotTimeout() {
    Request request =
        builder.addTestClass(JUnit3StyleClass.class.getName()).setPerTestTimeout(200).build();
    JUnitCore junitCore = new JUnitCore();
    Result result = junitCore.run(request);
    assertEquals(2, result.getFailures().size());
    assertEquals("This is a failing Test", result.getFailures().get(0).getMessage());
    assertEquals("Test threw RuntimeException", result.getFailures().get(1).getMessage());
  }

  private Matcher<List<?>> isEmpty() {
    return new TypeSafeMatcher<List<?>>() {
      @Override
      public void describeTo(org.hamcrest.Description description) {
        description.appendText("is empty");
      }

      @Override
      public boolean matchesSafely(List<?> item) {
        return item.size() == 0;
      }
    };
  }

  /**
   * Test various timeout functionality without @RunWith(AndroidJUnit4.class) annotation. By default
   * the Android specific runner should be used.
   */
  public static class JUnit4WithRuleClass {
    @Rule public Timeout globalTimeout = new Timeout(GLOBAL_RULE_TIMEOUT);
    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkGlobalRuleTimeoutInterruptsOverArgTimeout() throws InterruptedException {
      thrown.expectMessage(getTimeoutExceptionMessage(GLOBAL_RULE_TIMEOUT));
      Thread.sleep(GLOBAL_ARG_TIMEOUT);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void checkTestTimeoutInterruptsOverAllOthers() throws InterruptedException {
      thrown.expectMessage(getTimeoutExceptionMessage(TEST_TIMEOUT));
      Thread.sleep(GLOBAL_RULE_TIMEOUT);
    }

    private String getTimeoutExceptionMessage(int millis) {
      return String.format("test timed out after %s milliseconds", millis);
    }
  }

  /**
   * Test various timeout functionality with @RunWith(AndroidJUnit4.class) annotation. All Android
   * specific features should still work when RunWith explicitly defined.
   */
  public static class JUnit4NoRuleClass {
    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkArgTimeoutInterrupts() throws InterruptedException {
      thrown.expectMessage(getTimeoutExceptionMessage(GLOBAL_ARG_TIMEOUT));
      Thread.sleep(GLOBAL_ARG_TIMEOUT + WAIT_FOR_TIMEOUT);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void checkTestTimeoutInterruptsOverArgTimeout() throws InterruptedException {
      thrown.expectMessage(getTimeoutExceptionMessage(TEST_TIMEOUT));
      Thread.sleep(GLOBAL_ARG_TIMEOUT);
    }

    private String getTimeoutExceptionMessage(int millis) {
      return String.format("test timed out after %s milliseconds", millis);
    }
  }

  /** Fixture to test timeout functionality for a JUnit3 style test. */
  public static class JUnit3StyleClass extends TestCase {
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
}
