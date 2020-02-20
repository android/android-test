/*
 * Copyright (C) 2009 The Android Open Source Project
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

package androidx.test.orchestrator.listeners;

import android.os.Build;
import android.os.Environment;
import android.os.UserManager;
import android.util.Log;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;
import androidx.test.orchestrator.listeners.result.TestIdentifier;
import androidx.test.orchestrator.listeners.result.TestResult;
import androidx.test.orchestrator.listeners.result.TestResult.TestStatus;
import androidx.test.orchestrator.listeners.result.TestRunResult;
import com.google.common.io.ByteStreams;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * Writes JUnit results to an XML files in a format consistent with Ant's XMLJUnitResultFormatter.
 *
 * <p>Creates a separate XML file per test run.
 *
 * <p>This is a copy of {@code com.android.ddmlib.testrunner.XmlTestRunListener} made specificly for
 * AndroidTestOrchestrator. Changes include:
 *
 * <ul>
 *   <li>makes code android-compatible.
 *   <li>extends {@link OrchestrationRunListener} instead of implementing the similar {@code
 *       com.android.ddmlib.testrunner.ITestRunListener} interface to easily attach it to {@link
 *       androidx.test.orchestrator.AndroidTestOrchestrator}.
 *   <li>writes XML to /sdcard/odo/ by default.
 *   <li>prints start-time and end-time of each test in XML.
 *   <li>fixes a few java coding style problems.
 * </ul>
 *
 * @see <a
 *     href="https://svn.jenkins-ci.org/trunk/hudson/dtkit/dtkit-format/dtkit-junit-model/src/main/resources/com/thalesgroup/dtkit/junit/model/xsd/junit-4.xsd">https://svn.jenkins-ci.org/trunk/hudson/dtkit/dtkit-format/dtkit-junit-model/src/main/resources/com/thalesgroup/dtkit/junit/model/xsd/junit-4.xsd</a>
 */
public class OrchestrationXmlTestRunListener extends OrchestrationRunListener {

  private static final String LOG_TAG = "OrchestrationXmlTestRunListener";

  private static final String TEST_RESULT_FILE_SUFFIX = ".xml";
  private static final String TEST_RESULT_FILE_PREFIX = "test_result_";

  private static final String UTF_8 = "UTF-8";

  private static final String TESTSUITE = "testsuite";
  private static final String TESTCASE = "testcase";
  private static final String ERROR = "error";
  private static final String FAILURE = "failure";
  private static final String SKIPPED_TAG = "skipped";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_TIME = "time";
  private static final String ATTR_START_TIME = "start-time";
  private static final String ATTR_END_TIME = "end-time";
  private static final String ATTR_ERRORS = "errors";
  private static final String ATTR_FAILURES = "failures";
  private static final String ATTR_SKIPPED = "skipped";
  private static final String ATTR_ASSERTIOMS = "assertions";
  private static final String ATTR_TESTS = "tests";
  // private static final String ATTR_TYPE = "type";
  // private static final String ATTR_MESSAGE = "message";
  private static final String PROPERTIES = "properties";
  private static final String PROPERTY = "property";
  private static final String ATTR_CLASSNAME = "classname";
  private static final String TIMESTAMP = "timestamp";
  private static final String HOSTNAME = "hostname";

  /** the XML namespace */
  private static final String ns = null;

  private String hostName = "localhost";

  public static final String REPORT_DIRECTORY_NAME = "odo";

  private File reportDir =
      new File(Environment.getExternalStorageDirectory(), REPORT_DIRECTORY_NAME);

  private String reportPath = "";

  private TestRunResult runResult = new TestRunResult();

  private int numTests = 0;
  private long startTime;
  private long finishTime;

  /** Sets the report file to use. */
  public void setReportDir(File file) {
    reportDir = file;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * Returns the {@link TestRunResult}
   *
   * @return the test run results.
   */
  public TestRunResult getRunResult() {
    return runResult;
  }

  @Override
  public void orchestrationRunStarted(int testCount) {
    startTime = System.currentTimeMillis();
    runResult = new androidx.test.orchestrator.listeners.result.TestRunResult();
    numTests = testCount;
    runResult.testRunStarted("", numTests);
  }

  @Override
  public void testRunStarted(ParcelableDescription description) {}

  @Override
  public void testStarted(ParcelableDescription description) {
    runResult.testStarted(toTestIdentifier(description));
  }

  @Override
  public void testFinished(ParcelableDescription description) {
    runResult.testEnded(toTestIdentifier(description), new HashMap<String, String>());
  }

  @Override
  public void testFailure(ParcelableFailure failure) {
    runResult.testFailed(toTestIdentifier(failure.getDescription()), failure.getTrace());
  }

  @Override
  public void testAssumptionFailure(ParcelableFailure failure) {
    runResult.testAssumptionFailure(toTestIdentifier(failure.getDescription()), failure.getTrace());
  }

  @Override
  public void testIgnored(ParcelableDescription description) {
    runResult.testIgnored(toTestIdentifier(description));
  }

  @Override
  public void testRunFinished(ParcelableResult result) {}

  @Override
  public void testProcessFinished(String message) {}

  public void orchestrationRunFinished() {
    finishTime = System.currentTimeMillis();
    long elapsedTime = finishTime - startTime;
    runResult.testRunEnded(elapsedTime, new HashMap<String, String>());
    generateDocument(reportDir, elapsedTime);
  }

  private static TestIdentifier toTestIdentifier(ParcelableDescription description) {
    return new TestIdentifier(description.getClassName(), description.getMethodName());
  }

  /** Creates a report file and populates it with the report data from the completed tests. */
  private void generateDocument(File reportDir, long elapsedTime) {
    String timestamp = getTimestamp();

    OutputStream stream = null;
    try {
      stream = createOutputResultStream(reportDir);
      XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
      serializer.setOutput(stream, UTF_8);
      serializer.startDocument(UTF_8, null);
      serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
      // TODO: insert build info
      printTestResults(serializer, timestamp, elapsedTime);
      serializer.endDocument();
      String msg =
          String.format(
              "XML test result file generated at %s. %s",
              getAbsoluteReportPath(), runResult.getTextSummary());
      Log.i(LOG_TAG, msg);
    } catch (IOException | XmlPullParserException e) {
      Log.e(LOG_TAG, "Failed to generate report data", e);
      // TODO: consider throwing exception
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ignored) {
        }
      }
    }
  }

  private String getAbsoluteReportPath() {
    return reportPath;
  }

  /** Return the current timestamp as a {@link String}. */
  String getTimestamp() {
    SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    TimeZone gmt = TimeZone.getTimeZone("UTC");
    dateFormat.setTimeZone(gmt);
    dateFormat.setLenient(true);
    String timestamp = dateFormat.format(new Date());
    return timestamp;
  }

  /**
   * Creates a {@link File} where the report will be created.
   *
   * @param reportDir the root directory of the report.
   * @return a file
   * @throws IOException
   */
  protected File getResultFile(File reportDir) throws IOException {
    File reportFile =
        File.createTempFile(TEST_RESULT_FILE_PREFIX, TEST_RESULT_FILE_SUFFIX, reportDir);
    Log.i(LOG_TAG, String.format("Created xml report file at %s", reportFile.getAbsolutePath()));

    return reportFile;
  }

  /** Creates the output stream to use for test results. Exposed for mocking. */
  OutputStream createOutputResultStream(File reportDir) throws IOException {
    if (!reportDir.exists() && !reportDir.mkdirs()) {
      // TODO: Add Support for directBoot mode for FTL.
      // Right now it returns back a empty OutputStream if the device is in directBootMode.
      if (Build.VERSION.SDK_INT >= 24) {
        if (!((UserManager) getInstrumentation().getContext().getSystemService(UserManager.class))
            .isUserUnlocked()) {
          Log.e(LOG_TAG, "Currently no way to write output streams in direct boot mode.");
          return ByteStreams.nullOutputStream();
        }
      }
      throw new IOException("Failed to prepare report directory.");
    }
    File reportFile = getResultFile(reportDir);
    reportPath = reportFile.getAbsolutePath();
    return new BufferedOutputStream(new FileOutputStream(reportFile));
  }

  protected String getTestSuiteName() {
    return runResult.getName();
  }

  void printTestResults(XmlSerializer serializer, String timestamp, long elapsedTime)
      throws IOException {
    serializer.startTag(ns, TESTSUITE);
    String name = getTestSuiteName();
    if (name != null) {
      serializer.attribute(ns, ATTR_NAME, name);
    }
    serializer.attribute(ns, ATTR_TESTS, Integer.toString(runResult.getNumTests()));
    serializer.attribute(ns, ATTR_FAILURES, Integer.toString(runResult.getNumAllFailedTests()));
    // legacy - there are no errors in JUnit4
    serializer.attribute(ns, ATTR_ERRORS, "0");
    serializer.attribute(
        ns, ATTR_SKIPPED, Integer.toString(runResult.getNumTestsInState(TestStatus.IGNORED)));

    serializer.attribute(ns, ATTR_TIME, Double.toString((double) elapsedTime / 1000.f));
    serializer.attribute(ns, TIMESTAMP, timestamp);
    serializer.attribute(ns, HOSTNAME, hostName);

    serializer.startTag(ns, PROPERTIES);
    serializer.endTag(ns, PROPERTIES);

    Map<TestIdentifier, TestResult> testResults = runResult.getTestResults();
    for (Map.Entry<TestIdentifier, TestResult> testEntry : testResults.entrySet()) {
      print(serializer, testEntry.getKey(), testEntry.getValue());
    }

    serializer.endTag(ns, TESTSUITE);
  }

  protected String getTestName(TestIdentifier testId) {
    return testId.getTestName();
  }

  void print(XmlSerializer serializer, TestIdentifier testId, TestResult testResult)
      throws IOException {

    serializer.startTag(ns, TESTCASE);
    serializer.attribute(ns, ATTR_NAME, getTestName(testId));
    serializer.attribute(ns, ATTR_CLASSNAME, testId.getClassName());
    serializer.attribute(
        ns, ATTR_START_TIME, String.format("%.3f", (double) testResult.getStartTime() / 1000.f));
    serializer.attribute(
        ns, ATTR_END_TIME, String.format("%.3f", (double) testResult.getEndTime() / 1000.f));
    long elapsedTimeMs = testResult.getEndTime() - testResult.getStartTime();
    serializer.attribute(ns, ATTR_TIME, Double.toString((double) elapsedTimeMs / 1000.f));

    switch (testResult.getStatus()) {
      case FAILURE:
        printFailedTest(serializer, FAILURE, testResult.getStackTrace());
        break;
      case ASSUMPTION_FAILURE:
        printFailedTest(serializer, SKIPPED_TAG, testResult.getStackTrace());
        break;
      case IGNORED:
        serializer.startTag(ns, SKIPPED_TAG);
        serializer.endTag(ns, SKIPPED_TAG);
        break;
      default:
    }

    serializer.endTag(ns, TESTCASE);
  }

  private void printFailedTest(XmlSerializer serializer, String tag, String stack)
      throws IOException {
    serializer.startTag(ns, tag);
    // TODO: get message of stack trace ?
    // String msg = testResult.getStackTrace();
    // if (msg != null && msg.length() > 0) {
    //     serializer.attribute(ns, ATTR_MESSAGE, msg);
    // }
    // TODO: get class name of stackTrace exception
    // serializer.attribute(ns, ATTR_TYPE, testId.getClassName());
    serializer.text(sanitize(stack));
    serializer.endTag(ns, tag);
  }

  /** Returns the text in a format that is safe for use in an XML document. */
  private String sanitize(String text) {
    return text.replace("\0", "<\\0>");
  }
}
