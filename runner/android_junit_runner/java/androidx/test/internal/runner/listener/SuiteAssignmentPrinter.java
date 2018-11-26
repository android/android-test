/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.internal.runner.listener;

import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.internal.runner.TestSize;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * This class measures the elapsed run time of each test, and used it to report back to the user
 * which suite ({@link androidx.test.filters.SmallTest}, {@link
 * androidx.test.filters.MediumTest}, {@link androidx.test.filters.LargeTest}) the
 * test should belong to.
 */
public class SuiteAssignmentPrinter extends InstrumentationRunListener {

  @VisibleForTesting long startTime;
  @VisibleForTesting long endTime;
  @VisibleForTesting boolean timingValid;

  @Override
  public void testStarted(Description description) throws Exception {
    timingValid = true;
    startTime = getCurrentTimeMillis();
  }

  @Override
  public void testFinished(Description description) throws Exception {
    long runTime;
    endTime = getCurrentTimeMillis();

    if (!timingValid || startTime < 0) {
      sendString("F");
      Log.d(
          "SuiteAssignmentPrinter",
          String.format(
              "%s#%s: skipping suite assignment due to test failure\n",
              description.getClassName(), description.getMethodName()));
    } else {
      runTime = endTime - startTime;
      TestSize assignmentSuite = TestSize.getTestSizeForRunTime(runTime);
      TestSize currentRenameSize = TestSize.fromDescription(description);
      if (!assignmentSuite.equals(currentRenameSize)) {
        // test size != runtime
        sendString(
            String.format(
                "\n%s#%s: current size: %s. suggested: %s runTime: %d ms\n",
                description.getClassName(),
                description.getMethodName(),
                currentRenameSize,
                assignmentSuite.getSizeQualifierName(),
                runTime));
      } else {
        sendString(".");
        Log.d(
            "SuiteAssignmentPrinter",
            String.format(
                "%s#%s assigned correctly as %s. runTime: %d ms\n",
                description.getClassName(),
                description.getMethodName(),
                assignmentSuite.getSizeQualifierName(),
                runTime));
      }
    }
    // Clear startTime so that we can verify that it gets set next time.
    startTime = -1;
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    timingValid = false;
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    timingValid = false;
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    timingValid = false;
  }

  @VisibleForTesting
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }
}
