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

import static junit.framework.Assert.assertEquals;

import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.TestLoader.UnloadableClassRunner;
import androidx.test.internal.runner.junit3.JUnit38ClassRunner;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.RunnerBuilder;

/** Unit tests for {@link TestLoader}. */
// @RunWith(Parameterized.class) // TODO(b/26110951) not supported yet
@SmallTest
public abstract class TestLoaderTest {

  public static class JUnit3Test extends TestCase {
    public void testFoo() {}
  }

  public static class EmptyJUnit3Test extends TestCase {}

  public abstract static class AbstractTest extends TestCase {
    public void testFoo() {}
  }

  public static class SubClassAbstractTest extends AbstractTest {}

  public static class JUnit4Test {
    @Test
    public void thisIsATest() {}
  }

  public static class SubClassJUnit4Test extends JUnit4Test {}

  @RunWith(value = Parameterized.class)
  public static class JUnit4RunTest {
    public void thisIsMayBeATest() {}
  }

  public static class NotATest {
    public void thisIsNotATest() {}
  }

  public static class CustomTest implements junit.framework.Test {

    @Override
    public int countTestCases() {
      return 0;
    }

    @Override
    public void run(TestResult testResult) {}
  }

  private TestLoader loader;

  public boolean scanningPath;

  public TestLoaderTest(boolean scanningPath) {
    this.scanningPath = scanningPath;
  }

  @Parameters
  public static Object[] parameters() {
    return new Object[] {true, false};
  }

  @Before
  public void setUp() throws Exception {
    AndroidRunnerParams runnerParams = new AndroidRunnerParams(null, null, -1, false);
    AndroidRunnerBuilder runnerBuilder =
        new AndroidRunnerBuilder(
            runnerParams, scanningPath, Collections.<Class<? extends RunnerBuilder>>emptyList());
    loader = TestLoader.testLoader(null, runnerBuilder, scanningPath);
  }

  private void assertScanningLoadsAnyClass(
      Class<?> testClass, Class<? extends Runner> expectedRunnerClass) {
    Collection<String> classNames = Collections.singleton(testClass.getName());
    List<Runner> runners = loader.getRunnersFor(classNames, true);
    int expectedClassCount = scanningPath ? 0 : 1;
    assertEquals(expectedClassCount, runners.size());
    if (expectedClassCount == 1) {
      Runner runner = runners.get(0);
      assertEquals(expectedRunnerClass, runner.getClass());
    }
  }

  private void assertLoadTestSuccess(Class<?> clazz, Class<? extends Runner> expectedRunnerClass) {
    Collection<String> classNames = Collections.singleton(clazz.getName());
    List<Runner> runners = loader.getRunnersFor(classNames, true);
    assertEquals(1, runners.size());
    Runner runner = runners.get(0);
    assertEquals(expectedRunnerClass, runner.getClass());
  }

  @Test
  public void testLoadTests_junit3() {
    assertLoadTestSuccess(JUnit3Test.class, JUnit38ClassRunner.class);
  }

  @Test
  public void testLoadTests_emptyjunit3() {
    Class<?> testClass = EmptyJUnit3Test.class;
    assertScanningLoadsAnyClass(testClass, JUnit38ClassRunner.class);
  }

  @Test
  public void testLoadTests_junit4() {
    assertLoadTestSuccess(JUnit4Test.class, AndroidJUnit4ClassRunner.class);
  }

  @Test
  public void testLoadTests_runWith() {
    // This fails because it has no @Test annotated methods.
    assertLoadTestSuccess(JUnit4RunTest.class, ErrorReportingRunner.class);
  }

  @Test
  public void testLoadTests_all() {
    Collection<String> classNames =
        Arrays.asList(
            JUnit3Test.class.getName(),
            EmptyJUnit3Test.class.getName(),
            JUnit4Test.class.getName(),
            JUnit4RunTest.class.getName(),
            NotATest.class.getName(),
            CustomTest.class.getName(),
            "notexist",
            AbstractTest.class.getName(),
            SubClassJUnit4Test.class.getName(),
            SubClassAbstractTest.class.getName());

    List<Runner> runners = loader.getRunnersFor(classNames, false);
    List<Class<? extends Runner>> runnerClasses = new ArrayList<>();
    for (Runner runner : runners) {
      runnerClasses.add(runner == null ? null : runner.getClass());
    }

    List<Class<? extends Runner>> expectedRunnerClasses;
    if (scanningPath) {
      // When scanning path TestLoader is stricter about what it will accept as a test.
      expectedRunnerClasses =
          Arrays.asList(
              JUnit38ClassRunner.class,
              AndroidJUnit4ClassRunner.class,
              ErrorReportingRunner.class,
              UnloadableClassRunner.class,
              AndroidJUnit4ClassRunner.class,
              JUnit38ClassRunner.class);
    } else {
      expectedRunnerClasses =
          Arrays.asList(
              JUnit38ClassRunner.class,
              JUnit38ClassRunner.class,
              AndroidJUnit4ClassRunner.class,
              ErrorReportingRunner.class,
              ErrorReportingRunner.class,
              ErrorReportingRunner.class,
              UnloadableClassRunner.class,
              JUnit38ClassRunner.class,
              AndroidJUnit4ClassRunner.class,
              JUnit38ClassRunner.class);
    }
    assertEquals(expectedRunnerClasses, runnerClasses);
  }

  @Test
  public void testLoadTests_notATest() {
    assertScanningLoadsAnyClass(NotATest.class, ErrorReportingRunner.class);
  }

  @Test
  public void testLoadTests_CustomTest() {
    assertScanningLoadsAnyClass(CustomTest.class, ErrorReportingRunner.class);
  }

  @Test
  public void testLoadTests_notExist() {
    Collection<String> classNames = Collections.singleton("notexist");
    List<Runner> runners = loader.getRunnersFor(classNames, /* isScanningPath */ false);
    assertEquals(1, runners.size());
    Runner runner = runners.get(0);
    // only when users pass in a specific class via -e class should it be treated as a runner
    assertEquals(UnloadableClassRunner.class, runner.getClass());
  }

  @Test
  public void testLoadTests_abstract() {
    assertScanningLoadsAnyClass(AbstractTest.class, JUnit38ClassRunner.class);
  }

  @Test
  public void testLoadTests_junit4SubclassAbstract() {
    assertLoadTestSuccess(SubClassJUnit4Test.class, AndroidJUnit4ClassRunner.class);
  }

  @Test
  public void testLoadTests_junit3SubclassAbstract() {
    assertLoadTestSuccess(SubClassAbstractTest.class, JUnit38ClassRunner.class);
  }

  /** Verify loading a class that has already been loaded */
  @Test
  public void testLoadTests_loadAlreadyLoadedClass() {
    Class<?> clazz = SubClassAbstractTest.class;
    assertLoadTestSuccess(clazz, JUnit38ClassRunner.class);
    assertLoadTestSuccess(clazz, JUnit38ClassRunner.class);
  }
}
