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
package androidx.test.orchestrator.listeners;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.xml.sax.InputSource;

/** Unit tests for {@link OrchestrationXmlTestRunListener}. */
@RunWith(AndroidJUnit4.class)
public class OrchestrationXmlTestRunListenerTest {

  private OrchestrationXmlTestRunListener resultReporter;
  private ByteArrayOutputStream outputStream;
  private File reportDir;
  private static final String TRACE_PREFIX = "java.lang.Throwable: ";
  private static final String CLASS_NAME = "FooTest";
  private static final String TEST_NAME = "testFoo";

  @Before
  public void setUp() throws Exception {

    outputStream = new ByteArrayOutputStream();
    resultReporter =
        new OrchestrationXmlTestRunListener() {
          @Override
          OutputStream createOutputResultStream(File reportDir) throws IOException {
            return outputStream;
          }

          @Override
          String getTimestamp() {
            return "ignore";
          }
        };
    // TODO: use mock file dir instead
    this.reportDir = createTmpDir();
    resultReporter.setReportDir(this.reportDir);
  }

  private File createTmpDir() throws IOException {
    // create a temp file with unique name, then make it a directory
    File outputDir = getApplicationContext().getCacheDir();
    File tmpDir = File.createTempFile("foo", "dir", outputDir);
    tmpDir.delete();
    if (!tmpDir.mkdirs()) {
      throw new IOException("unable to create directory");
    }
    return tmpDir;
  }

  /** Recursively delete given file and all its contents */
  private static void recursiveDelete(File rootDir) {
    if (rootDir.isDirectory()) {
      File[] childFiles = rootDir.listFiles();
      if (childFiles != null) {
        for (File child : childFiles) {
          recursiveDelete(child);
        }
      }
    }
    rootDir.delete();
  }

  @After
  public void tearDown() throws Exception {
    if (reportDir != null) {
      recursiveDelete(reportDir);
    }
  }

  /** A simple test to ensure expected output is generated for test run with no tests. */
  @Test
  public void testEmptyGeneration() {
    final String expectedOutput =
        "<?xml version='1.0' encoding='UTF-8' ?>"
            + "<testsuite name=\"\" tests=\"0\" failures=\"0\" errors=\"0\" skipped=\"0\" "
            + "time=\"#TIMEVALUE#\" "
            + "timestamp=\"ignore\" hostname=\"localhost\"> "
            + "<properties />"
            + "</testsuite>";
    resultReporter.orchestrationRunStarted(0);
    runTestSucceed(null, true);
    resultReporter.orchestrationRunFinished();

    // because the timestamp is impossible to hardcode, look for the actual timestamp and
    // replace it in the expected string.
    String output = getOutput();
    String time = getTime(output);
    assertNotNull(time);

    String expectedTimedOutput = expectedOutput.replaceFirst("#TIMEVALUE#", time);
    assertEquals(expectedTimedOutput, output);
  }

  /**
   * A simple test to ensure expected output is generated for test run with a single passed test.
   */
  @Test
  public void testSinglePass() {
    Description testDescription = Description.createTestDescription(CLASS_NAME, TEST_NAME, 1);
    resultReporter.orchestrationRunStarted(1);
    runTestSucceed(testDescription, true);
    resultReporter.orchestrationRunFinished();
    String output = getOutput();
    // TODO: consider doing xml based compare
    assertTrue(output.contains("tests=\"1\" failures=\"0\" errors=\"0\""));
    final String testCaseTag =
        String.format("<testcase name=\"%s\" classname=\"%s\"", TEST_NAME, CLASS_NAME);
    assertTrue(output.contains(testCaseTag));
  }

  /**
   * A simple test to ensure expected output is generated for test run with a single failed test.
   */
  @Test
  public void testSingleFail() {
    Description testDescription = Description.createTestDescription(CLASS_NAME, TEST_NAME, 1);
    final String trace = "this is a trace";
    Throwable cause = new Throwable(trace);
    cause.setStackTrace(new StackTraceElement[0]);
    resultReporter.orchestrationRunStarted(1);
    runTestFailed(testDescription, cause, true);
    resultReporter.orchestrationRunFinished();
    String output = getOutput();
    // TODO: consider doing xml based compare
    assertTrue(output.contains("tests=\"1\" failures=\"1\" errors=\"0\""));
    final String testCaseTag =
        String.format("<testcase name=\"%s\" classname=\"%s\"", TEST_NAME, CLASS_NAME);
    assertTrue(output.contains(testCaseTag));
    final String failureTag = String.format("<failure>%s</failure>", TRACE_PREFIX + trace);
    assertTrue(output.contains(failureTag));
  }

  /** A simple test to ensure expected output is generated for 3 tests with 1 failed. */
  @Test
  public void testThreeTestsSingleFail() {
    boolean isolated = true;
    List<Description> testDescriptions = new ArrayList<>();
    testDescriptions.add(Description.createTestDescription("testClass1", "testName1", 1));
    testDescriptions.add(Description.createTestDescription("testClass1", "testName2", 1));
    testDescriptions.add(Description.createTestDescription("testClass2", "testName1", 1));
    final String trace = "this is a trace";
    Throwable cause = new Throwable(trace);
    cause.setStackTrace(new StackTraceElement[0]);

    resultReporter.orchestrationRunStarted(testDescriptions.size());
    runTestSucceed(testDescriptions.get(0), isolated);
    runTestFailed(testDescriptions.get(1), cause, isolated);
    runTestSucceed(testDescriptions.get(2), isolated);
    resultReporter.orchestrationRunFinished();

    String output = getOutput();
    assertTrue(output.contains("tests=\"3\" failures=\"1\" errors=\"0\""));
    for (Description testDescription : testDescriptions) {
      final String testCaseTag =
          String.format(
              "<testcase name=\"%s\" classname=\"%s\"",
              testDescription.getMethodName(), testDescription.getClassName());
      assertTrue(output.contains(testCaseTag));
    }
    final String failureTag = String.format("<failure>%s</failure>", TRACE_PREFIX + trace);
    assertTrue(output.contains(failureTag));
  }

  /** A simple test to ensure expected outputs are the same in isolated mode or not. */
  @Test
  public void testThreeTestsSingleFailSameOutput() throws Exception {
    boolean isolated = true;
    List<Description> testDescriptions = new ArrayList<>();
    testDescriptions.add(Description.createTestDescription("testClass1", "testName1", 1));
    testDescriptions.add(Description.createTestDescription("testClass1", "testName2", 1));
    testDescriptions.add(Description.createTestDescription("testClass2", "testName1", 1));
    final String trace = "this is a trace";
    Throwable cause = new Throwable(trace);
    cause.setStackTrace(new StackTraceElement[0]);

    resultReporter.orchestrationRunStarted(testDescriptions.size());
    runTestSucceed(testDescriptions.get(0), isolated);
    runTestFailed(testDescriptions.get(1), cause, isolated);
    runTestSucceed(testDescriptions.get(2), isolated);
    resultReporter.orchestrationRunFinished();
    String isolatedOutput = replaceTime(getOutput());

    setUp();
    isolated = false;
    resultReporter.orchestrationRunStarted(testDescriptions.size());
    testRunStarted();
    runTestSucceed(testDescriptions.get(0), isolated);
    runTestFailed(testDescriptions.get(1), cause, isolated);
    runTestSucceed(testDescriptions.get(2), isolated);
    testRunFinished();
    resultReporter.orchestrationRunFinished();
    String unisolatedOutput = replaceTime(getOutput());

    assertEquals(isolatedOutput, unisolatedOutput);
  }

  private String replaceTime(String output) {
    return output
        .replaceAll("start-time=.* end-time=.* ", "")
        .replaceAll("time=.* ", "")
        .replaceAll("timestamp=.* ", "");
  }

  private void runTestSucceed(Description testDescription, boolean isolated) {
    if (isolated) {
      testRunStarted();
    }
    if (testDescription != null) {
      resultReporter.testStarted(new ParcelableDescription(testDescription));
      resultReporter.testFinished(new ParcelableDescription(testDescription));
    }
    if (isolated) {
      testRunFinished();
    }
  }

  private void runTestFailed(Description testDescription, Throwable cause, boolean isolated) {
    if (isolated) {
      testRunStarted();
    }
    resultReporter.testStarted(new ParcelableDescription(testDescription));
    resultReporter.testFailure(new ParcelableFailure(new Failure(testDescription, cause)));
    resultReporter.testFinished(new ParcelableDescription(testDescription));
    if (isolated) {
      testRunFinished();
    }
  }

  private void testRunStarted() {
    resultReporter.testRunStarted(
        new ParcelableDescription(Description.createSuiteDescription("null")));
  }

  private void testRunFinished() {
    resultReporter.testRunFinished(new ParcelableResult(new Result()));
  }

  /** Gets the output produced, stripping it of extraneous whitespace characters. */
  private String getOutput() {
    String output = outputStream.toString();
    // ignore newlines and tabs whitespace
    output = output.replaceAll("[\\r\\n\\t]", "");
    // replace two ws chars with one
    return output.replaceAll("  ", " ");
  }

  /**
   * Returns the value if the time attribute from the given XML content
   *
   * <p>Actual XPATH: /testsuite/@time
   *
   * @param xml XML content.
   * @return
   */
  private String getTime(String xml) {
    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      return xpath.evaluate("/testsuite/@time", new InputSource(new StringReader(xml)));
    } catch (XPathExpressionException e) {
      // won't happen.
    }

    return null;
  }
}
