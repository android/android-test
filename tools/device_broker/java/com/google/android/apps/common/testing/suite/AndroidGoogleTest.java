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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.android.apps.common.testing.broker.AdbController;
import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.broker.ExecutedTest;
import com.google.android.apps.common.testing.broker.HostTestSize;
import com.google.android.apps.common.testing.broker.Instrumentation;
import com.google.android.apps.common.testing.broker.LogcatFilter;
import com.google.android.apps.common.testing.broker.LogcatStreamer;
import com.google.android.apps.common.testing.broker.LogcatStreamer.Buffer;
import com.google.android.apps.common.testing.broker.LogcatStreamer.OutputFormat;
import com.google.android.apps.common.testing.broker.SharedEnvironment;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.android.apps.common.testing.testrunner.testsuitepbutil.TestSuitePbUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/** Test case for running android tests on bazel. */
class AndroidGoogleTest extends TestCase implements TestRecorderProperties {

  private static final ImmutableList<Integer> CLEAR_LOG_MAY_FAIL = ImmutableList.of(21, 22);

  private static boolean logdRestarted = false;

  private final List<Buffer> buffers;
  private final BrokeredDevice brokeredDevice;
  private final String name;
  private final String testMethodTarget;
  private final List<LogcatFilter> testFilters;
  private final OutputFormat outputFormat;
  private final Instrumentation testInstrumentation;
  private final String externalStorageDirectory;
  private final boolean testDebug;
  private final String stackTraceFile;
  private final HostTestSize testSize;
  private final File perTestOutputDir;
  private final AndroidTestHarness androidTestHarness;
  private final boolean gatherOutputs;

  private Map<String, Object> exportedProperties = Maps.newHashMap();
  private int testOutputIdx = 0;

  private ExecutedTest.Status testStatus = null;

  private static final Logger logger = Logger.getLogger(AndroidGoogleTest.class.getName());

  private AndroidGoogleTest(Builder builder) {
    this.buffers = checkNotNull(builder.buffers);
    this.brokeredDevice = checkNotNull(builder.brokeredDevice);
    this.name = checkNotNull(builder.name);
    this.testFilters = ImmutableList.copyOf(builder.testFilters);
    this.testMethodTarget = checkNotNull(builder.testMethodTarget);
    this.outputFormat = checkNotNull(builder.outputFormat);
    this.testInstrumentation = checkNotNull(builder.testInstrumentation);
    this.externalStorageDirectory = checkNotNull(builder.externalStorageDirectory);
    this.testDebug = builder.testDebug;
    this.stackTraceFile = checkNotNull(builder.stackTraceFile);
    this.testSize = checkNotNull(builder.testSize);
    this.perTestOutputDir = checkNotNull(builder.perTestOutputDir);
    this.androidTestHarness = builder.androidTestHarness;
    this.gatherOutputs = builder.gatherOutputs;
  }

  @Override
  public Map<String, Object> getTestRecorderProperties() {
    return ImmutableMap.copyOf(exportedProperties);
  }

  @Override
  public String getName() {
    return name;
  }

  private String nextTestOutputName() {
    return "test_output" + testOutputIdx++;
  }

  static final class PullFromSdcardException extends RuntimeException {
    PullFromSdcardException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  static class Builder {
    private List<Buffer> buffers;
    private List<LogcatFilter> testFilters;
    private OutputFormat outputFormat;
    private boolean gatherOutputs;
    private boolean testDebug;
    private BrokeredDevice brokeredDevice;
    private InfoPb infoPb;
    private Instrumentation testInstrumentation;
    private String externalStorageDirectory;
    private String stackTraceFile;
    private HostTestSize testSize;
    private File perTestOutputDir;
    private String testMethodTarget;
    private String name;
    private AndroidTestHarness androidTestHarness;
    private TestArgs testArgs;

    public Builder() {
      reset();
    }

    public AndroidGoogleTest build() {
      checkNotNull(infoPb);
      checkArgument(infoPb.hasTestClass());
      checkArgument(infoPb.hasTestMethod());

      name =
          String.format("%s-%s", infoPb.getTestClass(), infoPb.getTestMethod()).replace("$", "_");
      testMethodTarget = checkNotNull(TestSuitePbUtil.getCanonicalTestMethod(infoPb));

      if (null == perTestOutputDir) {
        perTestOutputDir = new File(SharedEnvironment.ENVIRONMENT.getTmpDir(), name);
      }

      if (null == testSize) {
        testSize = HostTestSize.LARGE;
      }

      return new AndroidGoogleTest(this);
    }

    Builder withCommandLineArgs(String[] args) {
      JCommander jCommander = new JCommander(testArgs);
      jCommander.setAcceptUnknownOptions(true);
      jCommander.parse(args);
      this.buffers = testArgs.testLogcatBuffer;
      this.testFilters = testArgs.testLogcatFilters;
      this.outputFormat = testArgs.testLogcatFormat;
      this.testDebug = testArgs.enableDebug;
      this.gatherOutputs = testArgs.gatherTestOutputs;
      return this;
    }

    public Builder reset() {
      testArgs = new TestArgs();
      buffers = testArgs.testLogcatBuffer;
      testFilters = testArgs.testLogcatFilters;
      outputFormat = testArgs.testLogcatFormat;
      testDebug = testArgs.enableDebug;
      gatherOutputs = testArgs.gatherTestOutputs;
      brokeredDevice = null;
      infoPb = null;
      testInstrumentation = null;
      externalStorageDirectory = null;
      stackTraceFile = null;
      testSize = null;
      perTestOutputDir = null;
      name = null;
      testMethodTarget = null;
      androidTestHarness = null;
      return this;
    }

    public Builder withEnableDebug(boolean testDebug) {
      this.testDebug = testDebug;
      return this;
    }

    public Builder withTestSize(HostTestSize size) {
      this.testSize = size;
      return this;
    }

    public Builder withExternalStorageDirectory(String externalStorageDirectory) {
      this.externalStorageDirectory = externalStorageDirectory;
      return this;
    }

    public Builder withStackTraceFile(String stackTraceFile) {
      this.stackTraceFile = stackTraceFile;
      return this;
    }

    public Builder withPerTestOutputDir(File perTestOutputDir) {
      this.perTestOutputDir = perTestOutputDir;
      return this;
    }

    public Builder withLogcatBuffer(Buffer buffer) {
      this.buffers = Collections.singletonList(buffer);
      return this;
    }

    public Builder withLogcatBuffers(List<Buffer> buffers) {
      this.buffers = buffers;
      return this;
    }

    public Builder withLogcatFilters(List<LogcatFilter> testLogcatFilters) {
      this.testFilters = testLogcatFilters;
      return this;
    }

    public Builder withLogcatOutputFormat(OutputFormat outputFormat) {
      this.outputFormat = outputFormat;
      return this;
    }

    public Builder withBrokeredDevice(BrokeredDevice brokeredDevice) {
      this.brokeredDevice = brokeredDevice;
      return this;
    }

    public Builder withTestInfo(InfoPb infoPb) {
      this.infoPb = infoPb;
      return this;
    }

    public Builder withInstrumentation(Instrumentation instrumentation) {
      this.testInstrumentation = instrumentation;
      return this;
    }

    public Builder withAndroidTestHarness(AndroidTestHarness androidTestHarness) {
      this.androidTestHarness = androidTestHarness;
      return this;
    }
  }

  @VisibleForTesting
  void runGoogleAndroidTestCase() throws Exception {
    if (!perTestOutputDir.exists()) {
      checkState(
          perTestOutputDir.mkdirs(),
          "Per test output dir (%s) could not be created.",
          perTestOutputDir.getAbsolutePath());
    }

    if (brokeredDevice.getApiVersion() < 25 && buffers.size() > 1) {
      fail(
          "Multiple logcat buffers only supported on API 25+ "
              + "(current API level is "
              + brokeredDevice.getApiVersion()
              + ")");
    }

    AdbController adbController = brokeredDevice.getAdbController();

    LogcatStreamer streamer = null;

    try {
      if (!logdRestarted && CLEAR_LOG_MAY_FAIL.contains(brokeredDevice.getApiVersion())) {
        // restart logd on device to kill possible already-running logcat process on device.
        // This is a workaround for one bug fixed in M with this CL:
        // https://android-review.googlesource.com/#/c/119673/
        // Otherwise "logcat -c" could fail if there is another logcat process running.
        adbController.restartLogd();
        logdRestarted = true;
      }

      streamer =
          adbController.startLogcatStream(getTestLogcatFile(), buffers, outputFormat, testFilters);

      List<ExecutedTest> results;
      try {
        results =
            adbController.runTest(
                testInstrumentation,
                testMethodTarget,
                testDebug,
                testSize,
                false /* no hprof dump */,
                false /* no animation */,
                Collections.emptyMap());
      } catch (RuntimeException rte) {
        if (rte.getCause() instanceof TimeoutException) {
          throw rte;
        } else {
          //            TestingInfrastructureUtil.reportTestingInfrastructureFailure(
          //                "AndroidGoogleTest", rte.getMessage());
          throw rte;
        }
      }

      if (results.size() != 2) {
        throw new RuntimeException(
            String.format(
                "Something went wrong during test instrumentation execution "
                    + "(check \""
                    + String.format("logcat-%s.txt", getName())
                    + "\" file below to see if your test threw any uncaught exceptions).\n"
                    + "Debug Info: Instrumentation results are expected to have only 1 test, "
                    + "with two parts: Test started, and Test completed (passed, failed, errored)."
                    + "\nTest method: %s\nTest output: [%s]\n",
                testMethodTarget,
                getAllTestResults(results)));
      }

      Iterator<ExecutedTest> resultsIterator = results.iterator();
      ExecutedTest test = resultsIterator.next();

      if (test.getStatus() != ExecutedTest.Status.STARTED) {
        throw new RuntimeException("Failed to start test: \n" + getAllTestResults(results));
      }

      test = resultsIterator.next();
      testStatus = test.getStatus();

      if (testStatus == ExecutedTest.Status.FAILED) {
        // If the stack trace already begins with "junit.framework.AssertionFailedError", take it
        // out since "fail" will report it with this prefix. It's unclear if getStackTrace is
        // nullable, guarding against that just in case.
        String stack = test.getStackTrace();
        if (stack != null) {
          stack = stack.replaceFirst("junit.framework.AssertionFailedError[:]?", "");
        }
        fail(stack);
      }

      if (testStatus == ExecutedTest.Status.ERROR) {
        throw new RuntimeException(test.getStackTrace());
      }

      if (testStatus == ExecutedTest.Status.STARTED) {
        throw new RuntimeException(
            "Test result is invalid, contains two consecutive test STARTED states:\n"
                + getAllTestResults(results));
      }

      if (testStatus == ExecutedTest.Status.PASSED
          || testStatus == ExecutedTest.Status.ASSUMPTION_FAILURE) {
        logger.info("Instrumentation stream:\n" + getAllTestResults(results));
      } else {
        throw new RuntimeException(
            "Test result status not \"PASSED\", result contents: " + getAllTestResults(results));
      }
    } finally {

      exportedProperties.put(nextTestOutputName(), getTestLogcatFile().getName());
      if (gatherOutputs) {
        pullExportedProperties(adbController);
        pullAndRecordTestOutputs(adbController);
      }

      exportedProperties.putAll(adbController.getExportedProperties());
      if (null != streamer) {
        try {
          streamer.stopStream();
        } catch (RuntimeException rte) {
          throw rte;
        }
      }
    }
  }

  @Override
  protected void runTest() throws Throwable {
    androidTestHarness.beforeEachTest(brokeredDevice, getName());
    try {
      runGoogleAndroidTestCase();
    } finally {
      androidTestHarness.afterEachTest();
    }
  }

  private List<File> pullFiles(AdbController adbController, String path, File localDir) {
    List<File> outputs;
    try {
      outputs = adbController.pull(path, localDir);
    } catch (IllegalStateException ise) {
      logger.log(
          Level.INFO,
          "Adb pull failed when getting outputs. Normally this means there are none. Ignoring.");
      outputs = Lists.newArrayList();
    }
    return outputs;
  }

  private void pullExportedProperties(AdbController adbController) {
    if (null == externalStorageDirectory) {
      return;
    }

    String testOutputDirOnDevicePath =
        new File(externalStorageDirectory, SharedEnvironment.ON_DEVICE_PATH_TEST_PROPERTIES)
            .getPath();

    File tmpDirectory = new File(SharedEnvironment.ENVIRONMENT.getTmpDir(), getName());
    if (!tmpDirectory.exists()) {
      tmpDirectory.mkdir();
    }
    List<File> outputs = pullFiles(adbController, testOutputDirOnDevicePath, tmpDirectory);
    boolean testOutputPulled = false;
    for (File outputFile : outputs) {
      testOutputPulled = true;
      ObjectInputStream in = null;
      try {
        in = new ObjectInputStream(new FileInputStream(outputFile));
        Map<String, Serializable> recordedStats = (Map<String, Serializable>) in.readObject();
        exportedProperties.putAll(recordedStats);
      } catch (IOException ioe) {
        Closeables.closeQuietly(in);
      } catch (ClassNotFoundException cnfe) {
        Closeables.closeQuietly(in);
      }
    }

    if (testOutputPulled) {
      // Clear properties file, so it doesn't pollute the next test run.
      adbController.makeAdbCall("shell", "rm", "-R", testOutputDirOnDevicePath);
    }
  }

  /**
   * Recursively record all the files in {@code fileOrDirectory}.
   *
   * @param fileOrDirectory The file or directory to be recorded.
   * @param relativePathStart The start position of the relative path of test output files.
   */
  private void recordTestOutputsRecursively(File fileOrDirectory, int relativePathStart) {
    if (fileOrDirectory.isFile()) {
      String relativeOutputPath = fileOrDirectory.getPath().substring(relativePathStart + 1);
      exportedProperties.put(nextTestOutputName(), relativeOutputPath);
    } else if (fileOrDirectory.isDirectory()) {
      for (File file : fileOrDirectory.listFiles()) {
        recordTestOutputsRecursively(file, relativePathStart);
      }
    }
  }

  private void pullAndRecordTestOutputs(AdbController adbController) {
    try {
      if (null != externalStorageDirectory) {
        String testOutputDirOnDevicePath =
            new File(externalStorageDirectory, "googletest/test_outputfiles").getPath();
        List<File> outputs = pullFiles(adbController, testOutputDirOnDevicePath, perTestOutputDir);
        List<File> anrFile =
            pullFiles(adbController, stackTraceFile, new File(perTestOutputDir, "anr-stack.txt"));
        List<File> tombstones =
            pullFiles(adbController, "/data/tombstones", new File(perTestOutputDir, "tombstone"));

        int relativePathStart =
            new File(SharedEnvironment.ENVIRONMENT.getTmpDir()).getPath().length();
        recordTestOutputsRecursively(perTestOutputDir, relativePathStart);

        // Clear output directory, so it doesn't pollute the next test run.
        if (!outputs.isEmpty()) {
          adbController.makeAdbCall(
              "shell", "rm", "-R", testOutputDirOnDevicePath + "/*", "||", "true");
        }

        if (!anrFile.isEmpty()) {
          adbController.makeAdbCall("shell", "rm", stackTraceFile, "||", "true");
        }

        if (!tombstones.isEmpty()) {
          adbController.makeAdbCall("shell", "rm", "-R", "/data/tombstones/*", "||", "true");
        }
      }
    } catch (RuntimeException e) {
      throw new PullFromSdcardException("AndroidGoogleTest: Failed pulling files from sdcard", e);
    }
  }

  private File getTestLogcatFile() {
    return new File(
        SharedEnvironment.ENVIRONMENT.getTmpDir(), String.format("logcat-%s.txt", getName()));
  }

  private String getAllTestResults(List<ExecutedTest> results) {
    StringBuilder allTestOutput = new StringBuilder();

    for (ExecutedTest executedTest : results) {
      allTestOutput.append(executedTest.getAllLines());
    }

    return allTestOutput.toString();
  }

  static class TestArgs {
    @Parameter(
      names = "--test_logcat_buffer",
      description = "the logcat buffers to dump during the test"
    )
    public List<Buffer> testLogcatBuffer = Lists.newArrayList(Buffer.MAIN);

    @Parameter(
      names = "--test_logcat_format",
      description = "the logcat format to use during the test"
    )
    public OutputFormat testLogcatFormat = OutputFormat.THREADTIME;

    @Parameter(
      names = "--gather_test_outputs",
      description = "Controls whether outputs, ANRs, and tombstones are pulled from the device"
    )
    public boolean gatherTestOutputs = true;

    @Parameter(
      names = "--test_logcat_filter",
      description = "the logcat filters to use during the test"
    )
    public List<LogcatFilter> testLogcatFilters =
        Lists.newArrayList(
            LogcatFilter.fromString("*:V"),
            LogcatFilter.fromString(SharedEnvironment.AAG_TRACE_TAG + ":I"));

    @Parameter(
      names = "--enable_debug",
      description = "passes debug instruction to running the test case"
    )
    public boolean enableDebug = false;
  }
}
