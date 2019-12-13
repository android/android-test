/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.test.services.events.run;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.test.services.events.Failure;
import java.util.ArrayList;
import java.util.List;

/** Denotes that the test ended with a TEST_RUN_FINISHED event. */
public class TestRunFinishedEvent extends TestRunEvent {
  public final int count;
  public final int ignoreCount;
  public final long runTime;
  public final List<Failure> failures;

  /**
   * Creates a {@link TestRunFinishedEvent}.
   *
   * @param count total number of tests run
   * @param ignoreCount the number of tests ignored during the run
   * @param runTime the number of milliseconds it took to run the entire suite to run
   * @param failures the tests that failed
   */
  public TestRunFinishedEvent(int count, int ignoreCount, long runTime, List<Failure> failures) {
    this.count = count;
    this.ignoreCount = ignoreCount;
    this.runTime = runTime;
    this.failures = failures;
  }

  private TestRunFinishedEvent(Parcel source) {
    count = source.readInt();
    ignoreCount = source.readInt();
    runTime = source.readLong();
    this.failures = new ArrayList<>();
    Parcelable[] failures = source.readParcelableArray(Failure[].class.getClassLoader());
    for (Object failure : failures) {
      this.failures.add((Failure) failure);
    }
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    super.writeToParcel(parcel, i);
    parcel.writeInt(count);
    parcel.writeInt(ignoreCount);
    parcel.writeLong(runTime);
    parcel.writeParcelableArray((Failure[]) failures.toArray(), i);
  }

  public static final Creator<TestRunFinishedEvent> CREATOR =
      new Creator<TestRunFinishedEvent>() {
        @Override
        public TestRunFinishedEvent createFromParcel(Parcel source) {
          return new TestRunFinishedEvent(source);
        }

        @Override
        public TestRunFinishedEvent[] newArray(int size) {
          return new TestRunFinishedEvent[size];
        }
      };
}
