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

import static androidx.test.internal.runner.TestRequestBuilder.RequiresDeviceFilter.EMULATOR_HARDWARE_GOLDFISH;
import static androidx.test.internal.runner.TestRequestBuilder.RequiresDeviceFilter.EMULATOR_HARDWARE_RANCHU;
import static androidx.test.platform.app.InstrumentationRegistry.getArguments;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.FlakyTest;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;
import androidx.test.internal.runner.TestRequestBuilder.DeviceBuild;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Protectable;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TestRequestBuilder}. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestRequestBuilderTest {

  public static class SampleRunnerFilterSizeTest {

    @SmallTest
    @Test
    public void testSmall() {}

    @Test
    public void testOther() {}
  }

  public static class SamplePlatformSizeTest {

    @android.test.suitebuilder.annotation.SmallTest
    @Test
    public void testSmall() {}

    @Test
    public void testOther() {}
  }

  @SmallTest
  public static class SampleRunnerFilterClassSize {

    @Test
    public void testSmall() {}

    @Test
    public void testSmallToo() {}
  }

  @android.test.suitebuilder.annotation.SmallTest
  public static class SamplePlatformClassSize {

    @Test
    public void testSmall() {}

    @Test
    public void testSmallToo() {}
  }

  public static class SampleNoSize extends TestCase {

    public void testOther() {}

    public void testOther2() {}
  }

  public static class SampleJUnit3Test extends TestCase {

    @SmallTest
    public void testSmall() {}

    @SmallTest
    public void testSmall2() {}

    public void testOther() {}
  }

  @SmallTest
  public static class SampleJUnit3ClassSize extends TestCase {

    public void testSmall() {}

    public void testSmall2() {}
  }

  @SmallTest
  public static class SampleOverrideSize extends TestCase {

    public void testSmall() {}

    @MediumTest
    public void testMedium() {}
  }

  @SmallTest
  public static class SampleSameSize extends TestCase {

    @SmallTest
    public void testSmall() {}

    @MediumTest
    public void testMedium() {}
  }

  public static class SampleJUnit3Suppressed extends TestCase {

    public void testRun() {}

    public void testRun2() {}

    @Suppress
    public void testSuppressed() {}
  }

  public static class SampleSizeWithSuppress extends TestCase {

    public void testNoSize() {}

    @SmallTest
    @Suppress
    public void testSmallAndSuppressed() {}

    /** Use platform annotation to see if it works with both annotations */
    @android.test.suitebuilder.annotation.Suppress
    public void testSuppressed() {}
  }

  public static class SampleAllSuppressed extends TestCase {

    @Suppress
    public void testSuppressed2() {}

    /** Use platform annotation to see if it works with both annotations */
    @android.test.suitebuilder.annotation.Suppress
    public void testSuppressed() {}
  }

  public static class SampleSizeAndSuppress extends TestCase {

    @MediumTest
    public void testMedium() {}

    @Suppress
    public void testSuppressed() {}
  }

  public static class SampleJUnit3 extends TestCase {
    public void testFromSuper() {}
  }

  public static class SampleJUnit3SuppressedWithSuper extends SampleJUnit3 {

    public void testRun() {}

    public void testRun2() {}

    @Suppress
    public void testSuppressed() {}
  }

  // test fixtures for super-class annotation processing
  public static class InheritedAnnnotation extends SampleJUnit3Test {}

  public static class SampleMultipleAnnotation {

    @Test
    @SmallTest
    @FlakyTest
    public void testSmallSkipped() {
      Assert.fail("should not run");
    }

    @Test
    @MediumTest
    public void testMediumSkipped() {
      Assert.fail("should not run");
    }

    @Test
    public void testRunThis() {
      // fail this test too to make it easier to check it was run
      Assert.fail("should run");
    }
  }

  public static class SampleRequiresDevice {
    @RequiresDevice
    @Test
    public void skipThis() {}

    @RequiresDevice
    @Test
    public void skipThat() {}

    @Test
    public void runMe() {}

    @Test
    public void runMe2() {}
  }

  public static class SampleSdkSuppress {
    @SdkSuppress(minSdkVersion = 15)
    @Test
    public void min15() {
      fail("min15");
    }

    @SdkSuppress(minSdkVersion = 16)
    @Test
    public void min16() {
      fail("min16");
    }

    @SdkSuppress(minSdkVersion = 17)
    @Test
    public void min17() {
      fail("min17");
    }

    @Test
    public void noSdkSuppress() {
      fail("noSdkSuppress");
    }

    @SdkSuppress(maxSdkVersion = 19)
    @Test
    public void max19() {
      fail("max19");
    }

    @SdkSuppress(minSdkVersion = 17, maxSdkVersion = 19)
    @Test
    public void min17max19() {
      fail("min17max19");
    }

    @SdkSuppress(minSdkVersion = 14, maxSdkVersion = 16)
    @Test
    public void min14max16() {
      fail("min14max16");
    }
  }

  public static class DollarMethod {

    @Test
    public void testWith$() {}

    @Test
    public void testSkipped() {}
  }

  public static class SampleTwoTestsClass {
    @Test
    public void test1of2() {}

    @Test
    public void test2of2() {}
  }

  public static class SampleThreeTestsClass {
    @Test
    public void test1of3() {}

    @Test
    public void test2of3() {}

    @Test
    public void test3of3() {}
  }

  @RunWith(value = Parameterized.class)
  public static class ParameterizedTest {

    public ParameterizedTest(int data) {}

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      Object[][] data = new Object[][] {{1}, {2}, {3}};
      return Arrays.asList(data);
    }

    @Test
    @Ignore
    public void testParameterized() {}
  }

  /** Test fixture for verifying support for suite() methods */
  public static class JUnit3Suite {
    public static junit.framework.Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(SampleJUnit3Test.class);
      return suite;
    }
  }

  /** Test fixture for verifying support for suite() methods with tests. */
  public static class JUnit3SuiteWithTest extends TestCase {
    public static junit.framework.Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(SampleJUnit3Test.class);
      return suite;
    }

    public void testPass() {}
  }

  public static class JUnit4TestInitFailure {

    // this is an invalid method - trying to run test will fail with init error
    @Before
    protected void setUp() {}

    @Test
    public void testWillFailOnClassInit() {}
  }

  public static class JUnit4Failing {
    @Test
    public void testBroken() {
      fail("broken");
    }
  }

  @Ignore
  public static class JUnit4Ignored {
    @Test
    public void testBroken() {
      fail("expected this test to be ignored");
    }
  }

  @RunWith(JUnit4.class)
  public static class RunWithJUnit4Failing {
    @Test
    public void testBroken() {
      fail("broken");
    }
  }

  @RunWith(AndroidJUnit4.class)
  public static class RunWithAndroidJUnit4Failing {
    @Test
    public void testBroken() {
      fail("broken");
    }
  }

  public static class JUnit3FailingTestCase extends TestCase {
    public void testBroken() {
      fail("broken");
    }
  }

  public static class JUnit3FailingTestSuite extends TestSuite {

    public JUnit3FailingTestSuite() {
      addTestSuite(JUnit3FailingTestCase.class);
    }
  }

  public static class JUnit3FailingCustomTest implements junit.framework.Test {

    @Override
    public int countTestCases() {
      return 1;
    }

    @Override
    public void run(TestResult testResult) {
      testResult.startTest(this);
      try {
        fail("broken");
      } finally {
        testResult.endTest(this);
      }
    }
  }

  public static class JUnit3FailingCustomTest_UsingProtectable implements junit.framework.Test {

    @Override
    public int countTestCases() {
      return 1;
    }

    @Override
    public void run(TestResult testResult) {
      testResult.startTest(this);
      testResult.runProtected(
          this,
          new Protectable() {
            @Override
            public void protect() throws Throwable {
              fail("broken");
            }
          });
      testResult.endTest(this);
    }
  }

  public static class JUnit3SuiteMethod_ReturnsTestSuite {

    public static junit.framework.Test suite() {
      return new JUnit3FailingTestSuite();
    }
  }

  public static class JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest {

    public static junit.framework.Test suite() {
      TestSuite testSuite = new TestSuite();
      testSuite.addTest(new JUnit3FailingCustomTest());
      return testSuite;
    }
  }

  public static class JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable {

    public static junit.framework.Test suite() {
      TestSuite testSuite = new TestSuite();
      testSuite.addTest(new JUnit3FailingCustomTest_UsingProtectable());
      return testSuite;
    }
  }

  public static class JUnit3SuiteMethod_ReturnsTestCase {

    public static junit.framework.Test suite() {
      return new JUnit3FailingTestCase();
    }
  }

  public static class JUnit3SuiteMethod_ReturnsCustomTest {

    public static junit.framework.Test suite() {
      return new JUnit3FailingCustomTest();
    }
  }

  @RunWith(Suite.class)
  @Suite.SuiteClasses({
    JUnit4Failing.class,
    RunWithJUnit4Failing.class,
    RunWithAndroidJUnit4Failing.class,
    JUnit3FailingTestCase.class,
    JUnit3SuiteMethod_ReturnsTestSuite.class,
    JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable.class,
  })
  public static class RunWithSuite {}

  @RunWith(Suite.class)
  @Suite.SuiteClasses({
    JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest.class,
  })
  public static class RunWithSuite_WithCustomTest {}

  /** Test for custom runner builders. */
  public static class BrokenRunnableTest implements Runnable {

    @Override
    public void run() {
      fail("Broken");
    }
  }

  @Mock private DeviceBuild mockDeviceBuild;
  @Mock private ClassPathScanner mockClassPathScanner;

  private TestRequestBuilder builder;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    builder = createBuilder();
  }

  private TestRequestBuilder createBuilder() {
    return new TestRequestBuilder(getInstrumentation(), getArguments()) {
      @Override
      ClassPathScanner createClassPathScanner(List<String> paths) {
        return mockClassPathScanner;
      }
    };
  }

  private TestRequestBuilder createBuilder(DeviceBuild deviceBuild) {
    return new TestRequestBuilder(deviceBuild, getInstrumentation(), getArguments()) {
      @Override
      ClassPathScanner createClassPathScanner(List<String> paths) {
        return mockClassPathScanner;
      }
    };
  }

  /** Test initial condition for size filtering - that all tests run when no filter is attached */
  @Test
  public void testNoSize() {
    Request request = builder.addTestClass(SampleRunnerFilterSizeTest.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Test that platform size annotation filtering works */
  @Test
  public void testPlatformSize() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  @Test
  public void testAnnotationSizeFilteringWorks() {
    Request request =
        builder
            .addTestClass(SamplePlatformSizeTest.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Test that platform size annotation filtering by class works */
  @Test
  public void testPlatfromSize_class() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleRunnerFilterClassSize.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Test that platform size annotation filtering by class works */
  @Test
  public void testRunnerSize_class() {
    Request request =
        builder
            .addTestClass(SamplePlatformSizeTest.class.getName())
            .addTestClass(SamplePlatformClassSize.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Test case where entire JUnit3 test class has been filtered out */
  @Test
  public void testSize_classFiltered() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleNoSize.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    MyRunListener l = new MyRunListener();
    JUnitCore testRunner = new JUnitCore();
    testRunner.addListener(l);
    testRunner.run(request);
    Assert.assertEquals(1, l.testCount);
  }

  private static class MyRunListener extends RunListener {
    private int testCount = -1;

    @Override
    public void testRunStarted(Description description) throws Exception {
      testCount = description.testCount();
    }
  }

  /** Test size annotations with JUnit3 test methods */
  @Test
  public void testSize_junit3Method() {
    Request request =
        builder
            .addTestClass(SampleJUnit3Test.class.getName())
            .addTestClass(SampleNoSize.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
  }

  /** Test @Suppress with JUnit3 tests */
  @Test
  public void testSuppress_junit3Method() {
    Request request = builder.addTestClass(SampleJUnit3Suppressed.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
  }

  /** Test @Suppress in combination with size that filters out all methods */
  @Test
  public void testSuppress_withSize() {
    Request request =
        builder
            .addTestClass(SampleJUnit3Suppressed.class.getName())
            .addTestClass(SampleJUnit3Test.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    MyRunListener l = new MyRunListener();
    testRunner.addListener(l);
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
    Assert.assertEquals(2, l.testCount);
  }

  /** Test @Suppress in combination with size that filters out all methods, with super class. */
  @Test
  public void testSuppress_withSizeAndSuper() {
    Request request =
        builder
            .addTestClass(SampleJUnit3SuppressedWithSuper.class.getName())
            .addTestClass(SampleJUnit3Test.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    MyRunListener l = new MyRunListener();
    testRunner.addListener(l);
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
    Assert.assertEquals(2, l.testCount);
  }

  /** Test @Suppress when all methods have been filtered */
  @Test
  public void testSuppress_all() {
    Request request =
        builder
            .addTestClass(SampleAllSuppressed.class.getName())
            .addTestClass(SampleJUnit3Suppressed.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    MyRunListener l = new MyRunListener();
    testRunner.addListener(l);
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
    Assert.assertEquals(2, l.testCount);
  }

  /**
   * Test case where all methods are filtered out by combination of @Suppress and size when all
   * methods have been filtered.
   */
  @Test
  public void testSizeAndSuppress() {
    Request request =
        builder
            .addTestClass(SampleSizeAndSuppress.class.getName())
            .addTestClass(SampleJUnit3Test.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    MyRunListener l = new MyRunListener();
    testRunner.addListener(l);
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
    Assert.assertEquals(2, l.testCount);
  }

  /**
   * Test case where method has both a size annotation and suppress annotation. Expect suppress to
   * overrule the size.
   */
  @Test
  public void testSizeWithSuppress() {
    Request request =
        builder
            .addTestClass(SampleSizeWithSuppress.class.getName())
            .addTestClass(SampleJUnit3Test.class.getName())
            .addTestSizeFilter(TestSize.SMALL)
            .build();
    JUnitCore testRunner = new JUnitCore();
    MyRunListener l = new MyRunListener();
    testRunner.addListener(l);
    Result r = testRunner.run(request);
    Assert.assertEquals(2, r.getRunCount());
    Assert.assertEquals(2, l.testCount);
  }

  /** Test that annotation filtering by class works */
  @Test
  public void testAddAnnotationInclusionFilter() {
    Request request =
        builder
            .addAnnotationInclusionFilter(SmallTest.class.getName())
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleRunnerFilterClassSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Test that annotation filtering by class works */
  @Test
  public void testAddAnnotationExclusionFilter() {
    Request request =
        builder
            .addAnnotationExclusionFilter(SmallTest.class.getName())
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleRunnerFilterClassSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /**
   * Test that annotation filtering by class works when methods are from superclass.
   *
   * <p>TODO: add a similar test to upstream junit.
   */
  @Test
  public void testAddAnnotationInclusionFilter_super() {
    Request request =
        builder
            .addAnnotationInclusionFilter(SmallTest.class.getName())
            .addTestClass(InheritedAnnnotation.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Test that a method size annotation overrides a class size annotation. */
  @Test
  public void testTestSizeFilter_override() {
    Request request =
        builder
            .addTestSizeFilter(TestSize.SMALL)
            .addTestClass(SampleOverrideSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());

    request =
        createBuilder()
            .addTestSizeFilter(TestSize.MEDIUM)
            .addTestClass(SampleOverrideSize.class.getName())
            .build();
    testRunner = new JUnitCore();
    result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /**
   * Test that a method size annotation of same type as class level annotation is correctly
   * filtered.
   */
  @Test
  public void testTestSizeFilter_sameAnnotation() {
    Request request =
        builder
            .addTestSizeFilter(TestSize.SMALL)
            .addTestClass(SampleSameSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Test provided multiple annotations to exclude. */
  @Test
  public void testTestSizeFilter_multipleNotAnnotation() {
    Request request =
        builder
            .addAnnotationExclusionFilter(SmallTest.class.getName())
            .addAnnotationExclusionFilter(MediumTest.class.getName())
            .addTestClass(SampleMultipleAnnotation.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    // expect 1 test that failed
    Assert.assertEquals(1, result.getRunCount());
    Assert.assertEquals(
        "testRunThis", result.getFailures().get(0).getDescription().getMethodName());
  }

  /** Test provided multiple annotations to include. */
  @Test
  public void testTestSizeFilter_multipleAnnotation() {
    Request request =
        builder
            .addAnnotationInclusionFilter(SmallTest.class.getName())
            .addAnnotationInclusionFilter(FlakyTest.class.getName())
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleMultipleAnnotation.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    // expect 1 test that failed
    Assert.assertEquals(1, result.getRunCount());
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertEquals(
        "testSmallSkipped", result.getFailures().get(0).getDescription().getMethodName());
  }

  /** Test provided both include and exclude annotations. */
  @Test
  public void testTestSizeFilter_annotationAndNotAnnotationAtMethod() {
    Request request =
        builder
            .addAnnotationInclusionFilter(SmallTest.class.getName())
            .addAnnotationExclusionFilter(FlakyTest.class.getName())
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestClass(SampleMultipleAnnotation.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    // expect 1 test that passed.
    Assert.assertEquals(1, result.getRunCount());
    Assert.assertEquals(0, result.getFailureCount());
  }

  /** Test the sharding filter. */
  @Test
  public void testShardingFilter() {
    JUnitCore testRunner = new JUnitCore();

    Result[] results = new Result[4];
    int totalRun = 0;
    // The last iteration through the loop doesn't add a ShardingFilter - it runs all the
    // tests to establish a baseline for the total number that should have been run.
    for (int i = 0; i < 5; i++) {
      TestRequestBuilder b = createBuilder();
      if (i < 4) {
        b.addShardingFilter(4, i);
      }
      Request request =
          b.addTestClass(SampleRunnerFilterSizeTest.class.getName())
              .addTestClass(SampleNoSize.class.getName())
              .addTestClass(SampleRunnerFilterClassSize.class.getName())
              .addTestClass(SampleJUnit3Test.class.getName())
              .addTestClass(SampleOverrideSize.class.getName())
              .addTestClass(SampleJUnit3ClassSize.class.getName())
              .addTestClass(SampleMultipleAnnotation.class.getName())
              .build();
      Result result = testRunner.run(request);
      if (i == 4) {
        Assert.assertEquals(result.getRunCount(), totalRun);
      } else {
        totalRun += result.getRunCount();
        results[i] = result;
      }
    }
    for (int i = 0; i < 4; i++) {
      // Theoretically everything could collide into one shard, but, we'll trust that
      // the implementation of hashCode() is random enough to avoid that.
      assertTrue(results[i].getRunCount() < totalRun);
    }
  }

  public static class TestShardingFilterTest {
    @Test
    public void test0() {}

    @Test
    public void test1() {}
  }

  @Test
  public void testShardingFilter_Empty() {
    Request request =
        createBuilder()
            .addShardingFilter(97, 0)
            .addTestClass(TestShardingFilterTest.class.getName())
            .build();

    Runner runner = request.getRunner();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(runner);
    Assert.assertEquals(0, result.getRunCount());
  }

  /** Verify that filtering out all tests is not treated as an error */
  @Test
  public void testNoTests() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestSizeFilter(TestSize.MEDIUM)
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(0, result.getRunCount());
  }

  /** Test that {@link SdkSuppress} filters tests as appropriate */
  @Test
  public void testSdkSuppress() throws Exception {
    MockitoAnnotations.initMocks(this);
    TestRequestBuilder b = createBuilder(mockDeviceBuild);
    when(mockDeviceBuild.getSdkVersionInt()).thenReturn(16);
    Request request = b.addTestClass(SampleSdkSuppress.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);

    Set<String> expected =
        new HashSet<>(Arrays.asList("min15", "min16", "noSdkSuppress", "max19", "min14max16"));
    Assert.assertEquals(expected.size(), result.getRunCount());
    for (Failure f : result.getFailures()) {
      assertTrue(
          "Fail! " + expected + " doesn't contain \"" + f.getMessage() + "\" ",
          expected.contains(f.getMessage()));
    }
  }

  /** Test that {@link RequiresDevice} filters tests as appropriate */
  @Test
  public void testRequiresDevice() {
    MockitoAnnotations.initMocks(this);
    TestRequestBuilder b = createBuilder(mockDeviceBuild);
    when(mockDeviceBuild.getHardware())
        .thenReturn(EMULATOR_HARDWARE_GOLDFISH, EMULATOR_HARDWARE_RANCHU);
    Request request = b.addTestClass(SampleRequiresDevice.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Test that a custom filter is applied */
  @Test
  public void testCustomFilter() {
    Request request =
        builder
            .addFilter(new CustomTestFilter())
            .addTestClass(SampleNoSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Test that a custom RunnerBuilder is used. */
  @Test
  public void testCustomRunnerBuilder() {
    Request request =
        builder
            .addCustomRunnerBuilderClass(CustomRunnerBuilder.class)
            .addTestClass(BrokenRunnableTest.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    assertEquals("unexpected run count", 1, result.getRunCount());
    assertEquals("unexpected failure count", 1, result.getFailureCount());
    Failure failure = result.getFailures().get(0);
    assertEquals("unexpected failure", "Broken", failure.getMessage());
  }

  /** Test that a custom RunnerBuilder is used. */
  @Test
  public void testBrokenRunnerBuilder() {
    try {
      builder
          .addCustomRunnerBuilderClass(BrokenRunnerBuilder.class)
          .addTestClass(BrokenRunnableTest.class.getName())
          .build();
      fail("Did not detect broken RunnerBuilder");
    } catch (RuntimeException e) {
      assertEquals(
          "Unexpected exception",
          "Could not create instance of "
              + BrokenRunnerBuilder.class
              + ", make sure that it is a public concrete class with a public no-argument"
              + " constructor",
          e.getMessage());
    }
  }

  /** Test method filters with dollar signs are allowed */
  @Test
  public void testMethodFilterWithDollar() {
    Request request = builder.addTestMethod(DollarMethod.class.getName(), "testWith$").build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Test filtering by two methods in single class */
  @Test
  public void testMultipleMethodsFilter() {
    Request request =
        builder
            .addTestMethod(SampleJUnit3Test.class.getName(), "testSmall")
            .addTestMethod(SampleJUnit3Test.class.getName(), "testSmall2")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Test filtering by two methods in separate classes */
  @Test
  public void testTwoMethodsDiffClassFilter() {
    Request request =
        builder
            .addTestMethod(SampleJUnit3Test.class.getName(), "testSmall")
            .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testOther")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Test filtering a parameterized method */
  @Ignore // TODO(b/26110951) not supported yet
  @Test
  public void testParameterizedMethods() throws Exception {
    Request request =
        builder.addTestMethod(ParameterizedTest.class.getName(), "testParameterized").build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Verify adding and removing same class is rejected */
  @Test
  public void testFilterClassAddMethod() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(TestRequestBuilder.MISSING_ARGUMENTS_MSG);
    builder
        .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
        .removeTestClass(SampleRunnerFilterSizeTest.class.getName())
        .build();
  }

  /** Verify that including and excluding different methods leaves 1 method. */
  @Test
  public void testMethodAndNotMethod_different() {
    Request request =
        builder
            .removeTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
            .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testOther")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Verify that including and excluding the same method leaves no tests. */
  @Test
  public void testMethodAndNotMethod_same() {
    Request request =
        builder
            .removeTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
            .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(0, result.getRunCount());
  }

  /** Verify that filtering out all but one test in a class gives one test */
  @Test
  public void testClassAndMethod() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Verify that including and excluding different classes leaves that class's methods. */
  @Test
  public void testClassAndNotClass_different() {
    Request request =
        builder
            .addTestClass(SampleRunnerFilterSizeTest.class.getName())
            .removeTestClass(SampleRunnerFilterClassSize.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Verify that including and excluding the same class leaves no tests. */
  @Test
  public void testClassAndNotClass_same() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(TestRequestBuilder.MISSING_ARGUMENTS_MSG);
    builder
        .addTestClass(SampleRunnerFilterSizeTest.class.getName())
        .removeTestClass(SampleRunnerFilterSizeTest.class.getName())
        .build();
  }

  /** Verify that exclusion filter is filtering out a single test in a class and leaves the rest */
  @Test
  public void testOneMethodExclusion() {
    Request request =
        builder
            .addTestClass(SampleTwoTestsClass.class.getName())
            .addTestClass(SampleThreeTestsClass.class.getName())
            .removeTestMethod(SampleThreeTestsClass.class.getName(), "test1of3")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(4, result.getRunCount());
  }

  /**
   * Verify that inclusion filter is filtering out all other tests in the same class and leaves the
   * rest of the inclusion filters
   */
  @Test
  public void testOneMethodInclusion() {
    Request request =
        builder
            .addTestClass(SampleTwoTestsClass.class.getName())
            .addTestMethod(SampleThreeTestsClass.class.getName(), "test1of3")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /**
   * Verify that inclusion filter is filtering out all other tests in the same class and leaves the
   * rest of the inclusion filters
   */
  @Test
  public void testMultipleMethodInclusions() {
    Request request =
        builder
            .addTestClass(SampleTwoTestsClass.class.getName())
            .addTestMethod(SampleThreeTestsClass.class.getName(), "test1of3")
            .addTestMethod(SampleThreeTestsClass.class.getName(), "test3of3")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(4, result.getRunCount());
  }

  @Test
  public void testMultipleMethodExclusions() {
    Request request =
        builder
            .addTestClass(SampleTwoTestsClass.class.getName())
            .addTestClass(SampleThreeTestsClass.class.getName())
            .removeTestMethod(SampleThreeTestsClass.class.getName(), "test2of3")
            .removeTestMethod(SampleThreeTestsClass.class.getName(), "test3of3")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  @Test
  public void testBothMethodInclusionAndExclusion() {
    Request request =
        builder
            .addTestClass(SampleTwoTestsClass.class.getName())
            .removeTestMethod(SampleTwoTestsClass.class.getName(), "test1of2")
            .removeTestMethod(SampleThreeTestsClass.class.getName(), "test1of3")
            .addTestMethod(SampleThreeTestsClass.class.getName(), "test3of3")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(2, result.getRunCount());
  }

  /** Verify that filtering out a single test in a package leaves the rest */
  @Test
  @Suppress // until figure out a way to load dummy package that contains tests
  public void testPackageAndNotMethod() {
    Request request =
        builder
            .addTestPackage("androidx.test.internal.runner")
            .removeTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Verify including and excluding different packages. */
  @Test
  public void testPackageAndNotPackage_different() throws IOException {
    // just assert that the correct package filters are passed to class path scanner
    ArgumentCaptor<ClassPathScanner.ClassNameFilter> filterCapture =
        ArgumentCaptor.forClass(ClassPathScanner.ClassNameFilter.class);

    builder
        .addPathToScan("foo")
        .addTestPackage("com.foo")
        .removeTestPackage("com.foo.internal")
        .build();
    verify(mockClassPathScanner).getClassPathEntries(filterCapture.capture());
    ClassPathScanner.ClassNameFilter filter = filterCapture.getValue();
    assertTrue(filter.accept("com.foo.Foo"));
    assertFalse(filter.accept("com.foo.internal.Foo"));
  }

  /** Verify including multiple packages. */
  @Test
  public void testMultiplePackages() throws IOException {
    // just assert that the correct package filters are passed to class path scanner
    ArgumentCaptor<ClassPathScanner.ClassNameFilter> filterCapture =
        ArgumentCaptor.forClass(ClassPathScanner.ClassNameFilter.class);

    builder.addPathToScan("foo").addTestPackage("com.foo").addTestPackage("com.bar").build();
    verify(mockClassPathScanner).getClassPathEntries(filterCapture.capture());
    ClassPathScanner.ClassNameFilter filter = filterCapture.getValue();
    assertTrue(filter.accept("com.foo.Foo"));
    assertTrue(filter.accept("com.bar.Bar"));
    assertFalse(filter.accept("com.excludeme"));
  }

  /** Verify that including and excluding the same package leaves no tests. */
  @Test
  public void testPackageAndNotPackage_same() {
    builder.addPathToScan(getInstrumentation().getTargetContext().getPackageCodePath());
    Request request =
        builder
            .addPathToScan("foo")
            .addTestPackage("androidx.test.internal.runner")
            .removeTestPackage("androidx.test.internal.runner")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(0, result.getRunCount());
  }

  /** Test exception is thrown when no apk path and no class has been provided */
  @Test(expected = IllegalArgumentException.class)
  public void testNoApkPath() throws Exception {
    builder.addTestPackage("androidx.test.internal.runner").build();
  }

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final String EXCEPTION_MESSAGE =
      "Ambiguous arguments: cannot provide both test package and test class(es) to run";

  /** Take intersection of test package and class */
  @Test
  public void testBothPackageAndClass() throws Exception {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addTestPackage("androidx.test.internal.runner")
                .addTestClass(SampleRunnerFilterSizeTest.class.getName())
                .build());

    assertThat(results)
        .containsExactly(
            SampleRunnerFilterSizeTest.class.getName() + "#testSmall",
            SampleRunnerFilterSizeTest.class.getName() + "#testOther");
  }

  /** Test providing a test package and notClass */
  @Test
  public void testBothPackageAndNotClass() throws IOException {
    // just assert that the correct filters are passed to class path scanner
    ArgumentCaptor<ClassPathScanner.ClassNameFilter> filterCapture =
        ArgumentCaptor.forClass(ClassPathScanner.ClassNameFilter.class);

    builder
        .addPathToScan("foo")
        .addTestPackage("androidx.test.internal.runner")
        .removeTestClass(SampleRunnerFilterSizeTest.class.getName())
        .build();

    verify(mockClassPathScanner).getClassPathEntries(filterCapture.capture());
    ClassPathScanner.ClassNameFilter filter = filterCapture.getValue();
    assertThat(filter.accept("androidx.test.internal.runner.IncludeMe")).isTrue();
    assertThat(filter.accept("androidx.test.excludeme")).isFalse();
    assertThat(filter.accept(SampleRunnerFilterSizeTest.class.getName())).isFalse();
  }

  @Test
  public void testBothPackageAndMethod() throws Exception {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addTestPackage("androidx.test.internal.runner")
                .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
                .build());

    assertThat(results).containsExactly(SampleRunnerFilterSizeTest.class.getName() + "#testSmall");
  }

  /** Test providing both test package and notMethod is allowed */
  @Test
  public void testBothPackageAndNotMethod() throws IOException {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addPathToScan("foo")
                .addTestPackage("androidx.test.internal.runner")
                .removeTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
                .build());

    assertThat(results)
        .containsExactly(
            SampleRunnerFilterSizeTest.class.getName() + "#testOther",
            SamplePlatformSizeTest.class.getName() + "#testSmall",
            SamplePlatformSizeTest.class.getName() + "#testOther");
  }

  @Test
  public void testPackageAndClassAndMethod() throws IOException {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addPathToScan("foo")
                .addTestPackage("androidx.test.internal.runner")
                .addTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
                .build());

    assertThat(results).containsExactly(SampleRunnerFilterSizeTest.class.getName() + "#testSmall");
  }

  @Test
  public void testPackageAndClassAndNotMethod() throws IOException {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addTestPackage("androidx.test.internal.runner")
                .addTestClass(SampleRunnerFilterSizeTest.class.getName())
                .removeTestMethod(SampleRunnerFilterSizeTest.class.getName(), "testSmall")
                .build());

    assertThat(results).containsExactly(SampleRunnerFilterSizeTest.class.getName() + "#testOther");
  }

  @Test
  public void testPackageAndNotClassAndMethod() throws IOException {
    setClassPathScanningResults(
        SampleRunnerFilterSizeTest.class.getName(), SamplePlatformSizeTest.class.getName());

    List<String> results =
        runRequest(
            builder
                .addTestPackage("androidx.test.internal.runner")
                .removeTestClass(SampleRunnerFilterClassSize.class.getName())
                .addTestMethod(SamplePlatformSizeTest.class.getName(), "testSmall")
                .build());

    assertThat(results).containsExactly(SamplePlatformSizeTest.class.getName() + "#testSmall");
  }

  @Test
  public void testUnit3Suite_IgnoreSuiteMethodsFlagSet_IgnoresSuiteMethods() {
    Request request =
        builder.addTestClass(JUnit3Suite.class.getName()).ignoreSuiteMethods(true).build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  @Test
  public void testJUnit3Suite() {
    Request request = builder.addTestClass(JUnit3Suite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Verify suite() methods are not ignored when method filter is used */
  @Test
  public void testJUnit3Suite_NotFiltered() {
    Request request =
        builder.addTestMethod(JUnit3SuiteWithTest.class.getName(), "testPass").build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(3, result.getRunCount());
  }

  /** Verify method filter does not filter out initialization errors */
  @Test
  public void testJUnit4FilterWithInitError() {
    Request request =
        builder
            .addTestMethod(JUnit4TestInitFailure.class.getName(), "testWillFailOnClassInit")
            .build();
    JUnitCore testRunner = new JUnitCore();
    Result result = testRunner.run(request);
    Assert.assertEquals(1, result.getRunCount());
  }

  /** Verify that a JUnit 4 test is run when skipExecution = false. */
  @Test
  public void testNoSkipExecution_JUnit4() {
    Request request = builder.addTestClass(JUnit4Failing.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /** Verify that a JUnit 4 test is not actually run when skipExecution = true. */
  @Test
  public void testSkipExecution_JUnit4() {
    Request request =
        builder.setSkipExecution(true).addTestClass(JUnit4Failing.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /** Verify that a JUnit 4 test class marked with @Ignore is not run when skipExecution = false. */
  @Test
  public void testNoSkipExecution_JUnit4Ignored_WithMethodFilter() {
    Request request =
        builder
            .addTestClass(JUnit4Ignored.class.getName())
            .addTestMethod(JUnit4Ignored.class.getName(), "testBroken")
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 0);
  }

  /** Verify that a JUnit 4 test class marked with @Ignore is not run when skipExecution = true. */
  @Test
  public void testSkipExecution_JUnit4Ignored_WithMethodFilter() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(JUnit4Ignored.class.getName())
            .addTestMethod(JUnit4Ignored.class.getName(), "testBroken")
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 0);
  }

  /** Verify that @RunWith(JUnit4.class) annotated test is run when skipExecution = false. */
  @Test
  public void testNoSkipExecution_RunWithJUnit4() {
    Request request = builder.addTestClass(RunWithJUnit4Failing.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that @RunWith(JUnit4.class) annotated test is not actually run when skipExecution =
   * true.
   */
  @Test
  public void testSkipExecution_RunWithJUnit4() {
    Request request =
        builder.setSkipExecution(true).addTestClass(RunWithJUnit4Failing.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /** Verify that @RunWith(Suite.class) annotated test is run when skipExecution = false. */
  @Test
  public void testNoSkipExecution_RunWithSuite() {
    Request request = builder.addTestClass(RunWithSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 6, "broken");
  }

  /**
   * Verify that @RunWith(Suite.class) annotated test is not actually run when skipExecution = true.
   */
  @Test
  public void testSkipExecution_RunWithSuite() {
    Request request =
        builder.setSkipExecution(true).addTestClass(RunWithSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 6);
  }

  /**
   * Verify that @RunWith(Suite.class) annotated test that contains a JUnit 3 suite method that
   * returns a TestSuite that contains a custom Test will execute the custom Test when skipExecution
   * = false.
   */
  @Test
  public void testNoSkipExecution_RunWithSuite_WithCustomTest() {
    Request request = builder.addTestClass(RunWithSuite_WithCustomTest.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that @RunWith(Suite.class) annotated test that contains a JUnit 3 suite method that
   * returns a TestSuite that contains a custom Test will execute the custom Test even when
   * skipExecution = true.
   */
  @Test
  public void testSkipExecution_RunWithSuite_WithCustomTest() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(RunWithSuite_WithCustomTest.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /** Verify that @RunWith(AndroidJUnit4.class) annotated test is run when skipExecution = false. */
  @Test
  public void testNoSkipExecution_RunWithAndroidJUnit4() {
    Request request = builder.addTestClass(RunWithAndroidJUnit4Failing.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that a @RunWith(AndroidJUnit4.class) annotated test is not actually run when
   * skipExecution = true.
   */
  @Test
  public void testSkipExecution_RunWithAndroidJUnit4() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(RunWithAndroidJUnit4Failing.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /** Verify that a JUnit 3 TestCase is executed when skipExecution = false. */
  @Test
  public void testNoSkipExecution_JUnit3TestCase() {
    Request request = builder.addTestClass(JUnit3FailingTestCase.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /** Verify that a JUnit 3 TestCase is not executed when skipExecution = true. */
  @Test
  public void testSkipExecution_JUnit3TestCase() {
    Request request =
        builder.setSkipExecution(true).addTestClass(JUnit3FailingTestCase.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite is executed when skipExecution =
   * false.
   */
  @Test
  public void testNoSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite() {
    Request request =
        builder.addTestClass(JUnit3SuiteMethod_ReturnsTestSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite is not executed when skipExecution
   * = true.
   */
  @Test
  public void testSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(JUnit3SuiteMethod_ReturnsTestSuite.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite that contains a custom Test will
   * execute the custom Test when skipExecution = false.
   */
  @Test
  public void testNoSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest() {
    Request request =
        builder
            .addTestClass(JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite that contains a custom Test will
   * execute the custom Test even when skipExecution = true.
   */
  @Test
  public void testSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite that contains a custom Test will
   * execute the custom Test when skipExecution = false.
   */
  @Test
  public void
      testNoSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable() {
    Request request =
        builder
            .addTestClass(
                JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable.class
                    .getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "broken");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestSuite that contains a custom Test will
   * execute the custom Test even when skipExecution = true.
   */
  @Test
  public void
      testSkipExecution_JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(
                JUnit3SuiteMethod_ReturnsTestSuite_ContainingCustomTest_UsingProtectable.class
                    .getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    ensureNoTestsFailed(testRunner.run(request), 1);
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestCase is executed when skipExecution =
   * false.
   */
  @Test
  public void testNoSkipExecution_JUnit3SuiteMethod_ReturnsTestCase() {
    Request request =
        builder.addTestClass(JUnit3SuiteMethod_ReturnsTestCase.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    // Differs from standard JUnit behavior; a suite() method can return any implementation of
    // junit.framework.Test not just TestSuite.
    ensureAllTestsFailed(
        testRunner.run(request),
        1,
        JUnit3SuiteMethod_ReturnsTestCase.class.getName() + "#suite() did not return a TestSuite");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a TestCase is not executed when skipExecution =
   * true.
   */
  @Test
  public void testSkipExecution_JUnit3SuiteMethod_ReturnsTestCase() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(JUnit3SuiteMethod_ReturnsTestCase.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    // Differs from standard JUnit behavior; a suite() method can return any implementation of
    // junit.framework.Test not just TestSuite.
    ensureAllTestsFailed(
        testRunner.run(request),
        1,
        JUnit3SuiteMethod_ReturnsTestCase.class.getName() + "#suite() did not return a TestSuite");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a custom Test is executed when skipExecution =
   * false.
   */
  @Test
  public void testNoSkipExecution_JUnit3SuiteMethod_ReturnsCustomTest() {
    Request request =
        builder.addTestClass(JUnit3SuiteMethod_ReturnsCustomTest.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    // Differs from standard JUnit behavior; a suite() method can return any implementation of
    // junit.framework.Test not just TestSuite.
    ensureAllTestsFailed(
        testRunner.run(request),
        1,
        JUnit3SuiteMethod_ReturnsCustomTest.class.getName()
            + "#suite() did not return a TestSuite");
  }

  /**
   * Verify that a JUnit 3 suite method that returns a custom Test is not executed when
   * skipExecution = true.
   */
  @Test
  public void testSkipExecution_JUnit3SuiteMethod_ReturnsCustomTest() {
    Request request =
        builder
            .setSkipExecution(true)
            .addTestClass(JUnit3SuiteMethod_ReturnsCustomTest.class.getName())
            .build();
    JUnitCore testRunner = new JUnitCore();
    // Differs from standard JUnit behavior; a suite() method can return any implementation of
    // junit.framework.Test not just TestSuite.
    ensureAllTestsFailed(
        testRunner.run(request),
        1,
        JUnit3SuiteMethod_ReturnsCustomTest.class.getName()
            + "#suite() did not return a TestSuite");
  }

  /** Verify that a JUnit 3 TestSuite cannot be executed because it has no runnable methods. */
  @Test
  public void testNoSkipExecution_JUnit3TestSuite() {
    Request request = builder.addTestClass(JUnit3FailingTestSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "No runnable methods");
  }

  /**
   * Verify that JUnit 3 TestSuites cannot be executed because it has no runnable methods, even when
   * skipExecution = true.
   */
  @Test
  public void testSkipExecution_JUnit3TestSuite() {
    Request request =
        builder.setSkipExecution(true).addTestClass(JUnit3FailingTestSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "No runnable methods");
  }

  /** Verify that a JUnit 3 custom Test cannot be executed because it has no runnable methods. */
  @Test
  public void testNoSkipExecution_JUnit3CustomTest() throws Throwable {
    Request request = builder.addTestClass(JUnit3FailingTestSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "No runnable methods");
  }

  /**
   * Verify that a JUnit 3 custom Test cannot be executed because it has no runnable methods, even
   * when skipExecution = true.
   */
  @Test
  public void testSkipExecution_JUnit3CustomTest() throws Throwable {
    Request request =
        builder.setSkipExecution(true).addTestClass(JUnit3FailingTestSuite.class.getName()).build();
    JUnitCore testRunner = new JUnitCore();
    ensureAllTestsFailed(testRunner.run(request), 1, "No runnable methods");
  }

  private static void ensureAllTestsFailed(
      Result result, int expectedTestCount, String expectedMessage) {
    Assert.assertEquals(expectedTestCount, result.getRunCount());
    List<Failure> failures = result.getFailures();
    try {
      Assert.assertEquals("Mismatch in the number of failures", expectedTestCount, failures.size());
      for (Failure failure : failures) {
        Assert.assertEquals(
            "Test failed for the wrong reason", expectedMessage, failure.getMessage());
      }
    } catch (AssertionError e) {
      for (Failure failure : failures) {
        e.addSuppressed(failure.getException());
      }
      throw e;
    }
  }

  private static void ensureNoTestsFailed(Result result, int expectedTestCount) {
    Assert.assertEquals(expectedTestCount, result.getRunCount());
    List<Failure> failures = result.getFailures();
    try {
      Assert.assertEquals("No tests should have been executed", 0, failures.size());
    } catch (AssertionError e) {
      for (Failure failure : failures) {
        e.addSuppressed(failure.getException());
      }
      throw e;
    }
  }

  /** Runs the test request and gets list of test methods run */
  private static ArrayList<String> runRequest(Request request) {
    JUnitCore testRunner = new JUnitCore();
    RecordingRunListener listener = new RecordingRunListener();
    testRunner.addListener(listener);
    testRunner.run(request);
    return listener.methods;
  }

  private void setClassPathScanningResults(String... names) throws IOException {
    when(mockClassPathScanner.getClassPathEntries(ArgumentMatchers.any()))
        .thenReturn(new HashSet<>(Arrays.asList(names)));
  }

  /** Records list of test methods executed */
  private static class RecordingRunListener extends RunListener {
    ArrayList<String> methods = new ArrayList<>();

    public void testFinished(Description description) {
      methods.add(description.getClassName() + "#" + description.getMethodName());
    }
  }
}
