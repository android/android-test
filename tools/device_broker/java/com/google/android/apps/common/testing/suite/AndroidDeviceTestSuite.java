/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.suite;

import static com.google.common.base.Preconditions.checkNotNull;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.AdbController;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.broker.DeviceBroker;
import com.google.android.apps.common.testing.broker.DeviceBrokerFactory;
import com.google.android.apps.common.testing.broker.Instrumentation;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.android.apps.common.testing.proto.TestInfo.TestSuitePb;
import com.google.android.apps.common.testing.suite.filter.Filters;
import com.google.android.apps.common.testing.testrunner.testsuitepbutil.TestSuitePbUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

// For Copybara OSS
import java.util.Arrays;
// */

/**
 * Skeleton implementation of the Android Device test suite.
 *
 * <p>This will interrogate a device for tests to run, create test cases representing those tests
 * and execute them.
 */
public class AndroidDeviceTestSuite {

  private static final Logger logger = Logger.getLogger(AndroidGoogleTest.class.getName());

  public static TestSuite suite() {
    String argvFromEnv = System.getProperty("argv");
    String[] cleanedFlags =
        Arrays.stream(argvFromEnv.split(" |="))
            .map(s -> s.replaceAll("^\"|\"$", ""))
            .toArray(String[]::new);
    return new Builder().withCommandLineArgs(cleanedFlags).build();
    // */
  }

  static class Builder {
    private TestArgs testArgs = new TestArgs();
    private DeviceBroker baseBroker = null;
    private boolean allowEmptySuite = testArgs.havingNoTestsToRunIsFineWithMe;
    private AndroidTestHarness androidTestHarness = new NoopAndroidTestHarness();
    private String[] commandLineArgs = {};
    private BrokeredDevice brokeredDevice;

    public Builder withAllowEmptySuite(boolean allow) {
      allowEmptySuite = allow;
      return this;
    }

    public Builder withBaseDeviceBroker(DeviceBroker baseBroker) {
      this.baseBroker = baseBroker;
      return this;
    }

    public Builder withAndroidTestHarness(AndroidTestHarness androidTestHarness) {
      this.androidTestHarness = androidTestHarness;
      return this;
    }

    @VisibleForTesting
    DeviceBroker makeDeviceBroker() {
      if (null == baseBroker) {
        baseBroker = DeviceBrokerFactory.getInstance(commandLineArgs);
      }
      return baseBroker;
    }

    public TestSuite build() {
      TestSuite rootSuite = new TestSuite();
      final DeviceBroker myBroker = makeDeviceBroker();
      try {
        rootSuite.addTest(makeNativeTestSuite(myBroker));
      } catch (RuntimeException re) {
        logger.log(Level.WARNING, "AndroidDeviceTestSuite Native suite creation failed!");
        if (null != brokeredDevice) {
          myBroker.freeDevice(brokeredDevice);
        }
        throw re;
      }
      return new TestSuiteWrapper(rootSuite) {
        @Override
        public void run(TestResult testResult) {
          androidTestHarness.beforeAllTests();
          try {
            super.run(testResult);
          } finally {
            androidTestHarness.afterAllTests();
            myBroker.freeDevice(brokeredDevice);
          }
        }
      };
    }

    Builder withCommandLineArgs(String[] args) {
      JCommander jCommander = new JCommander(testArgs);
      jCommander.setAcceptUnknownOptions(true);
      jCommander.parse(args);
      this.allowEmptySuite = testArgs.havingNoTestsToRunIsFineWithMe;
      this.commandLineArgs = args;
      return this;
    }

    TestSuite makeNativeTestSuite(DeviceBroker myBroker) {
      List<InfoPb> nativeTestList;
      Map<String, String> deviceEnvVars;
      String externalStorageDirectory;
      String stackTraceFile;
      Instrumentation testInstrumentation;
      brokeredDevice = myBroker.leaseDevice();

      AdbController controller = brokeredDevice.getAdbController();
      testInstrumentation = controller.getGoogleTestInstrumentation();
      deviceEnvVars = brokeredDevice.getShellVariables();
      externalStorageDirectory = deviceEnvVars.get("EXTERNAL_STORAGE");
      stackTraceFile = controller.getDeviceProperties().get("dalvik.vm.stack-trace-file");
      checkNotNull(externalStorageDirectory, "No external storage on device? %s", deviceEnvVars);
      nativeTestList = listTests(brokeredDevice, controller, testInstrumentation);

      TestSuite nativeSuiteRoot = new TestSuite();

      TestSuite testSubPackageSuite = null;
      String curPackageName = null;
      TestSuite testClassSuite = null;
      String curClassName = null;

      try {
        for (InfoPb testInfo : nativeTestList) {
          System.out.println(testInfo);
          if (!testInfo.getTestPackage().equals(curPackageName)) {
            curPackageName = testInfo.getTestPackage();
            testSubPackageSuite = new TestSuite(curPackageName);
            nativeSuiteRoot.addTest(testSubPackageSuite);
          }

          if (!testInfo.getTestClass().equals(curClassName)) {
            curClassName = testInfo.getTestClass();
            testClassSuite = new TestSuite(curClassName);
            testSubPackageSuite.addTest(testClassSuite);
          }

          AndroidGoogleTest test =
              new AndroidGoogleTest.Builder()
                  .withTestInfo(testInfo)
                  .withBrokeredDevice(brokeredDevice)
                  .withInstrumentation(testInstrumentation)
                  .withExternalStorageDirectory(externalStorageDirectory)
                  .withStackTraceFile(stackTraceFile)
                  .withCommandLineArgs(commandLineArgs)
                  .withAndroidTestHarness(androidTestHarness)
                  .build();

          testClassSuite.addTest(test);
        }
      } catch (RuntimeException re) {
        throw new RuntimeException(
            "Failed to create tests for given list of methods:\n"
                + Joiner.on("\n").join(TestSuitePbUtil.getCanonicalTestMethods(nativeTestList)),
            re);
      }
      if (nativeTestList.isEmpty() && !allowEmptySuite) {
        nativeSuiteRoot.addTestSuite(EmptyTestSuiteTest.class);
      }

      return nativeSuiteRoot;
    }

    List<InfoPb> listTests(
        BrokeredDevice brokeredDevice,
        AdbController controller,
        Instrumentation testInstrumentation) {
      TestSuitePb testSuite = controller.getTestMethods(testInstrumentation);
      List<InfoPb> sortedTests = TestSuitePbUtil.sortTestSuite(testSuite).getInfoList();
      List<InfoPb> filteredTests = Lists.newArrayList();
      for (InfoPb testInfo : sortedTests) {
        if (filteredTests.isEmpty()) {
          filteredTests.add(testInfo);
        } else {
          InfoPb previousTest = filteredTests.get(filteredTests.size() - 1);

          if (!TestSuitePbUtil.getCanonicalTestMethod(previousTest)
              .equals(TestSuitePbUtil.getCanonicalTestMethod(testInfo))) {
            filteredTests.add(testInfo);
          }
        }
      }
      return Lists.newArrayList(
          Iterables.filter(
              filteredTests,
              Filters.getTestFilter(commandLineArgs).createTestFilterPredicateFor(brokeredDevice)));
    }

    static class TestArgs {
      @Parameter(
        names = "--having_no_tests_to_run_is_fine_with_me",
        description = "Allows a test suite to pass if it has no tests to run."
      )
      public boolean havingNoTestsToRunIsFineWithMe = false;
    }

    /** A test case to signal the empty suite. */
    public static class EmptyTestSuiteTest extends TestCase {

      public void testSuiteIsEmpty() {
        fail(
            "The test suite has no tests cases in it. Some reasons for this may be: "
                + "overly aggressive test filtering, multiple APK files with the same "
                + "package name in their android manifest, tting to annotate test "
                + "methods with @Test, or a typo in your junit3 test method name. "
                + "\n\nIf you have a valid reason to run a test target with no tests "
                + "in it, add  "
                + "--having_no_tests_to_run_is_fine_with_me "
                + "to your android_test's args attribute.\n "
                + "Please, do not add this argument just to get tests to pass "
                + "only add it if there is a good reason to not have tests in this "
                + "suite.");
      }
    }
  }
}
