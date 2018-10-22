/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.internal.runner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.junit3.JUnit38ClassRunner;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// TODO(b/26110951) not supported yet (note this class is ignored in BUILD file as well)
// @RunWith(Parameterized.class)
@SmallTest
public abstract class AndroidLogOnlyBuilderTest {

  @Mock private AndroidRunnerParams mockAndroidRunnerParams;

  private AndroidLogOnlyBuilder androidLogOnlyBuilder;

  @Parameter public boolean scanningPath;

  public AndroidLogOnlyBuilderTest(boolean scanningPath) {
    this.scanningPath = scanningPath;
  }

  @Parameters
  public static Object[] parameters() {
    return new Object[] {true, false};
  }

  public static class JUnit4Test {
    @Test
    public void testFoo() {
      fail("broken");
    }
  }

  public static class AnotherJUnit4Test {
    @Test
    public void testFoo() {
      fail("broken");
    }
  }

  @RunWith(Parameterized.class)
  public static class JUnit4ParameterizedTest {
    @Parameterized.Parameters(name = "{index}: foo({0})={1}")
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {{0, 0}, {1, 1}, {2, 2}});
    }

    @Test
    public void testFoo() {
      fail("broken");
    }
  }

  @RunWith(Enclosed.class)
  public static class JUnit4EnclosedTest {
    public static class JUnit4Test {
      @Test
      public void testFoo() {
        fail("broken");
      }
    }
  }

  @RunWith(Suite.class)
  @Suite.SuiteClasses({JUnit4Test.class, AnotherJUnit4Test.class})
  public static class JUnit4TestSuite {}

  public static class JUnit3Test extends TestCase {
    public void testFoo() {
      fail("broken");
    }
  }

  public static class JUnit3Suite {
    public static junit.framework.Test suite() {
      TestSuite testSuite = new TestSuite();
      testSuite.addTestSuite(JUnit3Test.class);
      return testSuite;
    }
  }

  @RunWith(AndroidJUnit4.class)
  public static class JUnit4Class {

    @Test
    public void someTest() {
      fail("broken");
    }
  }

  public static class NotATest {}

  @Before
  public void initBuilder() {
    MockitoAnnotations.initMocks(this);
    androidLogOnlyBuilder =
        new AndroidLogOnlyBuilder(
            mockAndroidRunnerParams,
            scanningPath,
            Collections.<Class<? extends RunnerBuilder>>emptyList());
  }

  @Test
  public void builderHandlesNotATest() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(NotATest.class);
    if (scanningPath) {
      assertThat(selectedRunner, nullValue());
    } else {
      assertThat(selectedRunner, notNullValue());
      assertThat(selectedRunner.getClass(), typeCompatibleWith(ErrorReportingRunner.class));
      runWithRunner(selectedRunner, 1, 1);
    }
  }

  @Test
  public void builderHandlesJUnit3Tests() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit3Test.class);
    assertThat(selectedRunner, notNullValue());
    assertThat(selectedRunner.getClass(), typeCompatibleWith(JUnit38ClassRunner.class));
    runWithRunner(selectedRunner, 1, 0);
  }

  @Test
  public void builderHandlesJUnit3TestSuites() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit3Suite.class);
    assertThat(selectedRunner, notNullValue());
    assertThat(selectedRunner.getClass(), typeCompatibleWith(JUnit38ClassRunner.class));
    runWithRunner(selectedRunner, 1, 0);
  }

  @Test
  public void builderHandlesJUnit4Tests() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit4Test.class);
    assertThat(selectedRunner, notNullValue());
    assertThat(selectedRunner.getClass(), typeCompatibleWith(NonExecutingRunner.class));
    runWithRunner(selectedRunner, 1, 0);
  }

  @Test
  public void builderHandlesParameterizedRunnerTests() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit4ParameterizedTest.class);
    assertThat(selectedRunner, notNullValue());
    assertThat(selectedRunner.getClass(), typeCompatibleWith(NonExecutingRunner.class));
    runWithRunner(selectedRunner, 3, 0);
  }

  @Test
  public void builderHandlesJunit4SuitesTests() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit4TestSuite.class);
    assertThat(selectedRunner, notNullValue());
    // Although this returns a Suite all the nested Runner implementations will be
    // NonExecutingRunner otherwise there will be failures when run.
    assertThat(selectedRunner.getClass(), typeCompatibleWith(Suite.class));
    runWithRunner(selectedRunner, 2, 0);
  }

  @Test
  public void builderHandlesJunit4EnclosedRunnerTests() throws Throwable {
    Runner selectedRunner = androidLogOnlyBuilder.runnerForClass(JUnit4EnclosedTest.class);
    assertThat(selectedRunner, notNullValue());
    // Although this returns an Enclosed all the nested Runner implementations will be
    // NonExecutingRunner otherwise there will be failures when run.
    assertThat(selectedRunner.getClass(), typeCompatibleWith(Enclosed.class));
    runWithRunner(selectedRunner, 1, 0);
  }

  private static void runWithRunner(Runner runner, int runCount, int failureCount) {
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(runner);
    assertThat(result.getRunCount(), is(equalTo(runCount)));
    assertThat(result.getFailureCount(), is(equalTo(failureCount)));
  }
}
