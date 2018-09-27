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

import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import java.util.ArrayList;
import java.util.List;

/** A representation of the end result of an orchestrated test run. */
public final class OrchestrationResult {

  /** Constructs an {@link OrchestrationResult} as tests run. */
  public static final class Builder extends OrchestrationRunListener {
    private long runCount = 0;
    private long expectedCount;
    private long ignoredCount = 0;
    private long startTime;
    private long finishTime;
    private final List<ParcelableFailure> failures = new ArrayList<>();

    @Override
    public void orchestrationRunStarted(int testCount) {
      expectedCount = testCount;
      startTime = System.currentTimeMillis();
    }

    public void orchestrationRunFinished() {
      finishTime = System.currentTimeMillis();
    }

    @Override
    public void testStarted(ParcelableDescription description) {
      runCount++;
    }

    @Override
    public void testFailure(ParcelableFailure failure) {
      failures.add(failure);
    }

    @Override
    public void testIgnored(ParcelableDescription description) {
      ignoredCount++;
    }

    public OrchestrationResult build() {
      return new OrchestrationResult(
          runCount, expectedCount - ignoredCount, startTime, finishTime, failures);
    }
  }

  private final long runCount;
  private final long expectedCount;
  private final long startTime;
  private final long finishTime;
  private final List<ParcelableFailure> failures;

  OrchestrationResult(
      long runCount,
      long expectedCount,
      long startTime,
      long finishTime,
      List<ParcelableFailure> failures) {
    this.failures = failures;
    this.runCount = runCount;
    this.expectedCount = expectedCount;
    this.startTime = startTime;
    this.finishTime = finishTime;
  }

  public long getRunTime() {
    return finishTime - startTime;
  }

  public boolean wasSuccessful() {
    return (getFailureCount() == 0 && getRunCount() == getExpectedCount());
  }

  public long getRunCount() {
    return runCount;
  }

  public long getExpectedCount() {
    return expectedCount;
  }

  public int getFailureCount() {
    return failures.size();
  }

  public List<ParcelableFailure> getFailures() {
    return failures;
  }
}
