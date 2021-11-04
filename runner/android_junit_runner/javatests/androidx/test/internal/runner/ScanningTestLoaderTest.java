/*
 * Copyright (C) 2012 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.testing.fixtures.AbstractTest;
import androidx.test.testing.fixtures.CollectingRunListener;
import androidx.test.testing.fixtures.CustomTest;
import androidx.test.testing.fixtures.EmptyJUnit3Test;
import androidx.test.testing.fixtures.EmptyJUnit4Test;
import androidx.test.testing.fixtures.JUnit3Test;
import androidx.test.testing.fixtures.JUnit4Test;
import androidx.test.testing.fixtures.NotATest;
import androidx.test.testing.fixtures.SubClassAbstractTest;
import androidx.test.testing.fixtures.SubClassJUnit4Test;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

/** Unit tests for {@link DirectTestLoader}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class ScanningTestLoaderTest {

  private TestLoader loader;

  @Before
  public void setUp() throws Exception {
    AndroidRunnerParams runnerParams = new AndroidRunnerParams(null, null, -1, false);
    AndroidRunnerBuilder runnerBuilder =
        new AndroidRunnerBuilder(
            runnerParams, Collections.<Class<? extends RunnerBuilder>>emptyList());
    loader = TestLoader.Factory.create(null, runnerBuilder, true);
  }

  private CollectingRunListener loadAndRun(String... classNames) {
    CollectingRunListener listener = new CollectingRunListener();
    RunNotifier runNotifier = new RunNotifier();
    runNotifier.addListener(listener);
    for (Runner runner : loader.getRunnersFor(Arrays.asList(classNames))) {
      runner.run(runNotifier);
    }
    return listener;
  }

  private void assertFailure(Class<?> clazz) {
    assertFailure(clazz.getName());
  }

  private void assertFailure(String className) {
    CollectingRunListener listener = loadAndRun(className);
    assertThat(listener.tests).hasSize(1);
    assertThat(listener.failures).hasSize(1);
    assertThat(listener.failures.get(0).getDescription().getMethodName()).isNotEmpty();
  }

  private void assertSuccess(Class<?> clazz, String methodName) {
    CollectingRunListener listener = loadAndRun(clazz.getName());
    assertThat(listener.tests).hasSize(1);
    assertThat(listener.tests.get(0).getMethodName()).isEqualTo(methodName);
    assertThat(listener.failures).isEmpty();
  }

  private void assertNoResults(Class<?> clazz) {
    assertNoResults(clazz.getName());
  }

  private void assertNoResults(String className) {
    CollectingRunListener listener = loadAndRun(className);
    assertThat(listener.tests).isEmpty();
    assertThat(listener.failures).isEmpty();
  }

  @Test
  public void testLoadTests_junit3() {
    assertSuccess(JUnit3Test.class, "testFoo");
  }

  @Test
  public void testLoadTests_emptyjunit3() {
    // for unremembered historical reasons this is not treated as an error. b/203614578
    assertNoResults(EmptyJUnit3Test.class);
  }

  @Test
  public void testLoadTests_junit4() {
    assertSuccess(JUnit4Test.class, "thisIsATest");
  }

  @Test
  public void testLoadTests_runWith() {
    assertFailure(EmptyJUnit4Test.class);
  }

  @Test
  public void testLoadTests_notATest() {
    assertNoResults(NotATest.class);
  }

  @Test
  public void testLoadTests_customTest() {
    assertNoResults(CustomTest.class);
  }

  @Test
  public void testLoadTests_notExist() {
    assertNoResults("notexist");
  }

  @Test
  public void testLoadTests_abstract() {
    assertNoResults(AbstractTest.class);
  }

  @Test
  public void testLoadTests_junit4SubclassAbstract() {
    assertSuccess(SubClassJUnit4Test.class, "thisIsATest");
  }

  @Test
  public void testLoadTests_junit3SubclassAbstract() {
    assertSuccess(SubClassAbstractTest.class, "testFoo");
  }

  /** Verify loading a class that has already been loaded */
  @Test
  public void testLoadTests_loadAlreadyLoadedClass() {
    CollectingRunListener listener =
        loadAndRun(JUnit4Test.class.getName(), JUnit4Test.class.getName());
    assertThat(listener.tests).hasSize(1);
    assertThat(listener.tests.get(0).getMethodName()).isEqualTo("thisIsATest");
  }
}
