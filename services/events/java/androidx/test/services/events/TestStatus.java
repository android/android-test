/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.services.events;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/** The final status of a Test Case or Test Run. */
public final class TestStatus implements Parcelable {
  /**
   * Valid test statuses for individual test cases and test runs. Check the documentation on
   * individual statuses for more information.
   */
  public enum Status {
    /**
     * This operation was supposed to run but was abandoned before it was able to execute. This
     * should be the default test status for receivers. If no updates were sent for tests cases that
     * were expected to run, it is safer to assume that the client terminated unexpectedly.
     */
    CANCELLED,
    /**
     * This operation did not run or trigger any other operations. If this or {@link SKIPPED} is not
     * emitted for tests that were not run, the receiver should mark them as {@link CANCELLED} since
     * the underlying test runner may have unexpectedly crashed in the middle of running these
     * tests.
     *
     * @see SKIPPED for ignored tests that trigger other operations.
     */
    IGNORED,
    /**
     * This operation did not run, but other operations may have been run to prepare for this
     * action. An example of this would be if a test threw an {@link
     * org.junit.AssumptionViolatedException} - indicating that it should not be run. This does
     * prevent other pre & post test operations from running.
     *
     * @see IGNORED for tests that do not trigger setup/teardown operations.
     */
    SKIPPED,
    /**
     * This operation was started and was terminated before completion. This status should be set by
     * receiver if a test start was emitted by a client but the client does not emit an event
     * marking the tests completion. It is safer to assume the client terminated unexpectedly.
     */
    ABORTED,
    /** This operation executed normally and succeeded. */
    PASSED,
    /** This operation finished execution, but did not succeed. */
    FAILED
  }

  /* The status */
  @NonNull public Status status;

  /**
   * Creates a parcelable wrapper {@link TestStatus} for {@link Status}.
   *
   * @param status the chosen status
   */
  public TestStatus(@NonNull Status status) {
    this.status = checkNotNull(status, "status cannot be null");
  }

  /**
   * Creates a {@link TestStatus} from an Android {@link Parcel}.
   *
   * @param source Android {@link Parcel} to read from
   */
  public TestStatus(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    status = Status.valueOf(checkNotNull(source.readString(), "status cannot be null"));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(status.name());
  }

  public static final Parcelable.Creator<TestStatus> CREATOR =
      new Parcelable.Creator<TestStatus>() {
        @Override
        public TestStatus createFromParcel(Parcel source) {
          return new TestStatus(source);
        }

        @Override
        public TestStatus[] newArray(int size) {
          return new TestStatus[size];
        }
      };
}
