/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.runner.junit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AndroidAnnotatedBuilderTest {

  @Mock RunnerBuilder mockRunnerBuilder;

  @RunWith(AndroidJUnit4.class)
  public static class RunWithAndroidJUnit4Class {
    @Test
    public void someTest() {}
  }

  @RunWith(JUnit4.class)
  public static class RunWithJUnit4Class {}

  public static class NoRunWithClass {}

  @Suppress // b/26110951
  @RunWith(Parameterized.class)
  public static class RunWithParameterizedClass {
    @Parameterized.Parameters(name = "{index}: someTest({0})={1}")
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {{0, 0}, {1, 1}});
    }

    private final int input;
    private final int expected;

    public RunWithParameterizedClass(int input, int expected) {
      this.input = input;
      this.expected = expected;
    }

    @Test
    public void someTest() {
      Assert.assertEquals(expected, input);
    }
  }

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void successfullyCreateAndroidRunner() throws Exception {
    final Runner mockedRunner = mock(Runner.class);
    AndroidAnnotatedBuilder ab =
        new AndroidAnnotatedBuilder(mockRunnerBuilder, 0) {
          @Override
          public Runner buildAndroidRunner(Class<? extends Runner> runnerClass, Class<?> testClass)
              throws Exception {
            assertEquals(runnerClass, AndroidJUnit4.class);
            assertEquals(testClass, RunWithAndroidJUnit4Class.class);
            return mockedRunner;
          }
        };
    // attempt to create a runner for a class annotated with @RunWith(AndroidJUnit4.class)
    Runner runner = ab.runnerForClass(RunWithAndroidJUnit4Class.class);
    assertEquals(1, runner.testCount());
  }

  @Test
  public void nonAndroidJUnit4RunWithAnnotation_DefaultsToDefaultAnnotatedBuilder()
      throws Exception {
    AndroidAnnotatedBuilder ab =
        new AndroidAnnotatedBuilder(mockRunnerBuilder, 0) {
          @Override
          public Runner buildAndroidRunner(Class<? extends Runner> runnerClass, Class<?> testClass)
              throws Exception {
            Assert.fail(
                "Should not attempt to build Android Runner when test class annotated"
                    + " with @RunWith(JUnit4.class)");
            return null;
          }
        };
    // attempt to create a runner for a class annotated with @RunWith(JUnit4.class)
    assertThrows(
        InvocationTargetException.class, () -> ab.runnerForClass(RunWithJUnit4Class.class));
  }

  @Test
  public void testNoRunWith() throws Exception {
    AndroidAnnotatedBuilder ab =
        new AndroidAnnotatedBuilder(mockRunnerBuilder, 0) {
          @Override
          public Runner buildAndroidRunner(Class<? extends Runner> runnerClass, Class<?> testClass)
              throws Exception {
            Assert.fail(
                "Should not attempt to build Android Runner no @RunWith " + "annotation is used");
            return null;
          }
        };
    // attempt to create a runner for a class with no @RunWith annotation
    ab.runnerForClass(NoRunWithClass.class);
  }
}
