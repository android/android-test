/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.internal.runner;

import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.junit3.JUnit38ClassRunner;
import androidx.test.internal.util.AndroidRunnerParams;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AndroidRunnerBuilderTest {

  @Mock public AndroidRunnerParams mockAndroidRunnerParams;

  private AndroidRunnerBuilder androidRunnerBuilder;

  public static class JUnit3Class extends TestCase {
    public void testSome() {}
  }

  public static class JUnit3Suite extends TestSuite {
    public JUnit3Suite() {
      super();
      addTestSuite(JUnit3Class.class);
    }

    public static junit.framework.Test suite() {
      return new JUnit3Suite();
    }
  }

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    androidRunnerBuilder = new AndroidRunnerBuilder(mockAndroidRunnerParams);
  }

  @Test
  public void jUnit3SuitePicksJUnit38ClassRunner() throws Throwable {
    Runner runner = androidRunnerBuilder.runnerForClass(JUnit3Suite.class);
    assertThat(runner.getClass(), typeCompatibleWith(JUnit38ClassRunner.class));
  }

  @Test
  public void jUnit3Suite_skippedExecutionTrue_jUnit3SuitePicksJUnit38ClassRunner()
      throws Throwable {
    // mock skip execution flag to return true
    when(mockAndroidRunnerParams.isSkipExecution()).thenReturn(true);

    Runner runner = androidRunnerBuilder.runnerForClass(JUnit3Suite.class);
    assertThat(runner.getClass(), typeCompatibleWith(JUnit38ClassRunner.class));
  }
}
