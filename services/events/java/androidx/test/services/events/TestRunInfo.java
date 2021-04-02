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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parcelable TestRun. Contains all the information for a test run. Each test class or
 * test suite inside a Java test is considered a separate {@link TestRunInfo}.
 *
 * <p>See <a href="https://developer.android.com/reference/android/os/Parcelable.html">Android
 * Parcelable</a>.
 */
public final class TestRunInfo implements Parcelable {
  /** Name of this test run */
  @NonNull public final String testRunName;
  /** Test cases within this test run */
  @NonNull public final List<TestCaseInfo> testCases;

  /**
   * Creates a {@link TestRunInfo}.
   *
   * @param testRunName Name of this test run. Usually the class containing the tests to be run,
   *     (e.g. foo.bar.MyTests)
   * @param testCases Tests that are part of this test run.
   */
  public TestRunInfo(@NonNull String testRunName, @NonNull List<TestCaseInfo> testCases) {
    this.testRunName = checkNotNull(testRunName, "testRunName cannot be null");
    this.testCases = checkNotNull(testCases, "testCases cannot be null");
  }

  /**
   * Creates an {@link TestRunInfo} from an Android {@link Parcel}.
   *
   * @param source Android {@link Parcel} to read from
   */
  public TestRunInfo(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    testRunName = checkNotNull(source.readString(), "className cannot be null");
    testCases = new ArrayList<>();
    source.readTypedList(testCases, TestCaseInfo.CREATOR);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(testRunName);
    parcel.writeTypedList(testCases);
  }

  public static final Parcelable.Creator<TestRunInfo> CREATOR =
      new Parcelable.Creator<TestRunInfo>() {
        @Override
        public TestRunInfo createFromParcel(Parcel source) {
          return new TestRunInfo(source);
        }

        @Override
        public TestRunInfo[] newArray(int size) {
          return new TestRunInfo[size];
        }
      };
}
