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
package androidx.test.orchestrator.listeners;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import java.io.PrintStream;

/**
 * A line by line reimplementation of {@link
 * androidx.test.internal.runner.listener.InstrumentationResultPrinter}
 *
 * <p>{@link androidx.test.orchestrator.AndroidTestOrchestrator} needs to mirror the output
 * of a non-orchestrated AndroidJUnitRunner multi-test run, and thus requires a result printer.
 * InstrumentationResultPrinter cannot be reused because it extends from the JUnit RunListener,
 * which passes in JUnit specific objects which {@link
 * androidx.test.orchestrator.AndroidTestOrchestrator} cannot access.
 *
 * <p>TODO(b/35394729): Refactor expertise from this and InstrumentationResultPrinter to one place.
 */
public class OrchestrationResultPrinter extends OrchestrationRunListener {

  private static final String LOG_TAG = "OdoInstrResultPrinter";

  /**
   * This value, if stored with key {@link android.app.Instrumentation#REPORT_KEY_IDENTIFIER},
   * identifies AndroidJUnitRunner as the source of the report. This is sent with all status
   * messages.
   */
  public static final String REPORT_VALUE_ID = "AndroidJUnitRunner";
  /**
   * If included in the status or final bundle sent to an IInstrumentationWatcher, this key
   * identifies the total number of tests that are being run. This is sent with all status messages.
   */
  public static final String REPORT_KEY_NUM_TOTAL = "numtests";
  /**
   * If included in the status or final bundle sent to an IInstrumentationWatcher, this key
   * identifies the sequence number of the current test. This is sent with any status message
   * describing a specific test being started or completed.
   */
  public static final String REPORT_KEY_NUM_CURRENT = "current";
  /**
   * If included in the status or final bundle sent to an IInstrumentationWatcher, this key
   * identifies the name of the current test class. This is sent with any status message describing
   * a specific test being started or completed.
   */
  public static final String REPORT_KEY_NAME_CLASS = "class";
  /**
   * If included in the status or final bundle sent to an IInstrumentationWatcher, this key
   * identifies the name of the current test. This is sent with any status message describing a
   * specific test being started or completed.
   */
  public static final String REPORT_KEY_NAME_TEST = "test";

  /** The test is starting. */
  public static final int REPORT_VALUE_RESULT_START = 1;
  /** The test completed successfully. */
  public static final int REPORT_VALUE_RESULT_OK = 0;
  /**
   * The test completed with an error.
   *
   * @deprecated not supported in JUnit4, use REPORT_VALUE_RESULT_FAILURE instead
   */
  @Deprecated public static final int REPORT_VALUE_RESULT_ERROR = -1;
  /** The test completed with a failure. */
  public static final int REPORT_VALUE_RESULT_FAILURE = -2;
  /** The test was ignored. */
  public static final int REPORT_VALUE_RESULT_IGNORED = -3;
  /** The test completed with an assumption failure. */
  public static final int REPORT_VALUE_RESULT_ASSUMPTION_FAILURE = -4;

  /**
   * If included in the status bundle sent to an IInstrumentationWatcher, this key identifies a
   * stack trace describing an error or failure. This is sent with any status message describing a
   * specific test being completed.
   */
  public static final String REPORT_KEY_STACK = "stack";

  private final Bundle resultTemplate;
  private Bundle testResult;
  int testNum = 0;
  int testResultCode = -999;
  String testClass = null;
  private ParcelableDescription description;

  public OrchestrationResultPrinter() {
    resultTemplate = new Bundle();
    testResult = new Bundle(resultTemplate);
  }

  @Override
  public void orchestrationRunStarted(int testCount) {
    resultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
    resultTemplate.putInt(REPORT_KEY_NUM_TOTAL, testCount);
  }

  /** send a status for the start of a each test, so long tests can be seen as "running" */
  @Override
  public void testStarted(ParcelableDescription description) {
    this.description = description; // cache ParcelableDescription in case of a crash
    String testClass = description.getClassName();
    String testName = description.getMethodName();
    testResult = new Bundle(resultTemplate);
    testResult.putString(REPORT_KEY_NAME_CLASS, testClass);
    testResult.putString(REPORT_KEY_NAME_TEST, testName);
    testResult.putInt(REPORT_KEY_NUM_CURRENT, ++testNum);
    // pretty printing
    if (testClass != null && !testClass.equals(this.testClass)) {
      testResult.putString(
          Instrumentation.REPORT_KEY_STREAMRESULT, String.format("\n%s:", testClass));
      this.testClass = testClass;
    } else {
      testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "");
    }

    sendStatus(REPORT_VALUE_RESULT_START, testResult);
    testResultCode = REPORT_VALUE_RESULT_OK;
  }

  @Override
  public void testFinished(ParcelableDescription description) {
    if (testResultCode == REPORT_VALUE_RESULT_OK) {
      testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
    }
    sendStatus(testResultCode, testResult);
  }

  @Override
  public void testFailure(ParcelableFailure failure) {
    testResultCode = REPORT_VALUE_RESULT_FAILURE;
    reportFailure(failure);
  }

  @Override
  public void testAssumptionFailure(ParcelableFailure failure) {
    testResultCode = REPORT_VALUE_RESULT_ASSUMPTION_FAILURE;
    testResult.putString(REPORT_KEY_STACK, failure.getTrace());
  }

  private void reportFailure(ParcelableFailure failure) {
    testResult.putString(REPORT_KEY_STACK, failure.getTrace());
    // pretty printing
    testResult.putString(
        Instrumentation.REPORT_KEY_STREAMRESULT,
        String.format(
            "\nError in %s:\n%s", failure.getDescription().getDisplayName(), failure.getTrace()));
  }

  @Override
  public void testIgnored(ParcelableDescription description) {
    testStarted(description);
    testResultCode = REPORT_VALUE_RESULT_IGNORED;
    testFinished(description);
  }

  /**
   * Produce a more meaningful crash report including stack trace and report it back to
   * Instrumentation results.
   */
  public void reportProcessCrash(Throwable t) {
    try {
      testResultCode = REPORT_VALUE_RESULT_FAILURE;
      ParcelableFailure failure = new ParcelableFailure(description, t);
      testResult.putString(REPORT_KEY_STACK, failure.getTrace());
      // pretty printing
      testResult.putString(
          Instrumentation.REPORT_KEY_STREAMRESULT,
          String.format(
              "\nProcess crashed while executing %s:\n%s",
              description.getDisplayName(), failure.getTrace()));
      testFinished(description);
    } catch (Exception e) {
      if (null == description) {
        Log.e(LOG_TAG, "Failed to initialize test before process crash");
      } else {
        Log.e(
            LOG_TAG,
            "Failed to mark test "
                + description.getDisplayName()
                + " as finished after process crash");
      }
    }
  }

  /** Convenience method for {@link #getInstrumentation()#sendStatus()} */
  public void sendStatus(int code, Bundle bundle) {
    getInstrumentation().sendStatus(code, bundle);
  }

  public void orchestrationRunFinished(
      PrintStream streamResult, OrchestrationResult orchestrationResults) {
    // reuse TextListener to display a summary of the run
    new TextListener(streamResult).testRunFinished(orchestrationResults);
  }
}
