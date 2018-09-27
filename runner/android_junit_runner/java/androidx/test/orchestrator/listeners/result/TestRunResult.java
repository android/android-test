/*
 * Copyright (C) 2010 The Android Open Source Project
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
package androidx.test.orchestrator.listeners.result;

import android.util.Log;
import androidx.test.orchestrator.listeners.result.TestResult.TestStatus;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds results from a single test run.
 *
 * <p>Maintains an accurate count of tests, and tracks incomplete tests.
 *
 * <p>Not thread safe! The test* callbacks must be called in order
 *
 * <p>This is an android-compatible copy of {@code com.android.ddmlib.testrunner.TestRunResult}.
 */
public class TestRunResult implements ITestRunListener {
  private static final String LOG_TAG =
      androidx.test.orchestrator.listeners.result.TestRunResult.class.getSimpleName();
  private String mTestRunName;
  // Uses a LinkedHashMap to have predictable iteration order
  private Map<TestIdentifier, TestResult> mTestResults =
      new LinkedHashMap<TestIdentifier, TestResult>();
  private Map<String, String> mRunMetrics = new HashMap<String, String>();
  private boolean mIsRunComplete = false;
  private long mElapsedTime = 0;

  /** represents sums of tests in each TestStatus state. Indexed by TestStatus.ordinal() */
  private int[] mStatusCounts = new int[TestStatus.values().length];
  /** tracks if mStatusCounts is accurate, or if it needs to be recalculated */
  private boolean mIsCountDirty = true;

  private String mRunFailureError = null;

  private boolean mAggregateMetrics = false;

  /** Create an empty{@link androidx.test.orchestrator.listeners.result.TestRunResult}. */
  public TestRunResult() {
    mTestRunName = "not started";
  }

  public void setAggregateMetrics(boolean metricAggregation) {
    mAggregateMetrics = metricAggregation;
  }

  /** @return the test run name */
  public String getName() {
    return mTestRunName;
  }

  /**
   * Gets a map of the test results.
   *
   * @return
   */
  public Map<TestIdentifier, TestResult> getTestResults() {
    return mTestResults;
  }

  /** @return a {@link Map} of the test test run metrics. */
  public Map<String, String> getRunMetrics() {
    return mRunMetrics;
  }

  /** Gets the set of completed tests. */
  public Set<TestIdentifier> getCompletedTests() {
    Set<TestIdentifier> completedTests = new LinkedHashSet<TestIdentifier>();
    for (Map.Entry<TestIdentifier, TestResult> testEntry : getTestResults().entrySet()) {
      if (!testEntry.getValue().getStatus().equals(TestStatus.INCOMPLETE)) {
        completedTests.add(testEntry.getKey());
      }
    }
    return completedTests;
  }

  /** @return <code>true</code> if test run failed. */
  public boolean isRunFailure() {
    return mRunFailureError != null;
  }

  /** @return <code>true</code> if test run finished. */
  public boolean isRunComplete() {
    return mIsRunComplete;
  }

  public void setRunComplete(boolean runComplete) {
    mIsRunComplete = runComplete;
  }

  /** Gets the number of tests in given state for this run. */
  public int getNumTestsInState(TestStatus status) {
    if (mIsCountDirty) {
      // clear counts
      for (int i = 0; i < mStatusCounts.length; i++) {
        mStatusCounts[i] = 0;
      }
      // now recalculate
      for (TestResult r : mTestResults.values()) {
        mStatusCounts[r.getStatus().ordinal()]++;
      }
      mIsCountDirty = false;
    }
    return mStatusCounts[status.ordinal()];
  }

  /** Gets the number of tests in this run. */
  public int getNumTests() {
    return mTestResults.size();
  }

  /** Gets the number of complete tests in this run ie with status != incomplete. */
  public int getNumCompleteTests() {
    return getNumTests() - getNumTestsInState(TestStatus.INCOMPLETE);
  }

  /** @return <code>true</code> if test run had any failed or error tests. */
  public boolean hasFailedTests() {
    return getNumAllFailedTests() > 0;
  }

  /** Return total number of tests in a failure state (failed, assumption failure) */
  public int getNumAllFailedTests() {
    return getNumTestsInState(TestStatus.FAILURE);
  }

  /** @return */
  public long getElapsedTime() {
    return mElapsedTime;
  }

  /** Return the run failure error message, <code>null</code> if run did not fail. */
  public String getRunFailureMessage() {
    return mRunFailureError;
  }

  @Override
  public void testRunStarted(String runName, int testCount) {
    mTestRunName = runName;
    mIsRunComplete = false;
    mRunFailureError = null;
  }

  @Override
  public void testStarted(TestIdentifier test) {
    addTestResult(test, new TestResult());
  }

  private void addTestResult(TestIdentifier test, TestResult testResult) {
    mIsCountDirty = true;
    mTestResults.put(test, testResult);
  }

  private void updateTestResult(TestIdentifier test, TestStatus status, String trace) {
    TestResult r = mTestResults.get(test);
    if (r == null) {
      Log.d(LOG_TAG, String.format("received test event without test start for %s", test));
      r = new TestResult();
    }
    r.setStatus(status);
    r.setStackTrace(trace);
    addTestResult(test, r);
  }

  @Override
  public void testFailed(TestIdentifier test, String trace) {
    updateTestResult(test, TestStatus.FAILURE, trace);
  }

  @Override
  public void testAssumptionFailure(TestIdentifier test, String trace) {
    updateTestResult(test, TestStatus.ASSUMPTION_FAILURE, trace);
  }

  @Override
  public void testIgnored(TestIdentifier test) {
    updateTestResult(test, TestStatus.IGNORED, null);
  }

  @Override
  public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
    TestResult result = mTestResults.get(test);
    if (result == null) {
      result = new TestResult();
    }
    if (result.getStatus().equals(TestStatus.INCOMPLETE)) {
      result.setStatus(TestStatus.PASSED);
    }
    result.setEndTime(System.currentTimeMillis());
    result.setMetrics(testMetrics);
    addTestResult(test, result);
  }

  @Override
  public void testRunFailed(String errorMessage) {
    mRunFailureError = errorMessage;
  }

  @Override
  public void testRunStopped(long elapsedTime) {
    mElapsedTime += elapsedTime;
    mIsRunComplete = true;
  }

  @Override
  public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
    if (mAggregateMetrics) {
      for (Map.Entry<String, String> entry : runMetrics.entrySet()) {
        String existingValue = mRunMetrics.get(entry.getKey());
        String combinedValue = combineValues(existingValue, entry.getValue());
        mRunMetrics.put(entry.getKey(), combinedValue);
      }
    } else {
      mRunMetrics.putAll(runMetrics);
    }
    mElapsedTime += elapsedTime;
    mIsRunComplete = true;
  }

  /**
   * Combine old and new metrics value
   *
   * @param existingValue
   * @param value
   * @return
   */
  private String combineValues(String existingValue, String newValue) {
    if (existingValue != null) {
      try {
        Long existingLong = Long.parseLong(existingValue);
        Long newLong = Long.parseLong(newValue);
        return Long.toString(existingLong + newLong);
      } catch (NumberFormatException e) {
        // not a long, skip to next
      }
      try {
        Double existingDouble = Double.parseDouble(existingValue);
        Double newDouble = Double.parseDouble(newValue);
        return Double.toString(existingDouble + newDouble);
      } catch (NumberFormatException e) {
        // not a double either, fall through
      }
    }
    // default to overriding existingValue
    return newValue;
  }

  /**
   * Return a user friendly string describing results.
   *
   * @return
   */
  public String getTextSummary() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Total tests %d, ", getNumTests()));
    for (TestStatus status : TestStatus.values()) {
      int count = getNumTestsInState(status);
      // only add descriptive state for states that have non zero values, to avoid cluttering
      // the response
      if (count > 0) {
        builder.append(String.format("%s %d, ", status.toString().toLowerCase(), count));
      }
    }
    return builder.toString();
  }
}
