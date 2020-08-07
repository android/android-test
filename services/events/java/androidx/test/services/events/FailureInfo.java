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

package androidx.test.services.events;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Denotes an android test failure, has details of the failure including stack trace / type and
 * message.
 */
public final class FailureInfo implements Parcelable {

  /** The failure message associated with the failure. */
  @Nullable public final String failureMessage;

  /** The type of failure exception. E.g {@code NullPointerException}. */
  @Nullable public final String failureType;

  /** The stack trace associated with the failure. */
  @NonNull public final String stackTrace;

  /** The test case that caused the failure. */
  @NonNull public final TestCaseInfo testCase;

  /** Constructor to create a {@link FailureInfo}. */
  public FailureInfo(
      @Nullable String failureMessage,
      @Nullable String failureType,
      @NonNull String stackTrace,
      @NonNull TestCaseInfo testCase) {
    checkNotNull(stackTrace, "stackTrace cannot be null");
    checkNotNull(testCase, "testCase cannot be null");
    this.failureMessage = failureMessage;
    this.failureType = failureType;
    this.stackTrace = stackTrace;
    this.testCase = testCase;
  }

  /**
   * Creates a {@link FailureInfo} from android {@link Parcel}.
   *
   * @param source the android Parcel.
   */
  public FailureInfo(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    failureMessage = source.readString();
    failureType = source.readString();
    stackTrace = source.readString();
    testCase = source.readParcelable(TestCaseInfo.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(failureMessage);
    parcel.writeString(failureType);
    parcel.writeString(stackTrace);
    parcel.writeParcelable(testCase, i);
  }

  public static final Parcelable.Creator<FailureInfo> CREATOR =
      new Parcelable.Creator<FailureInfo>() {
        @Override
        public FailureInfo createFromParcel(Parcel source) {
          return new FailureInfo(source);
        }

        @Override
        public FailureInfo[] newArray(int size) {
          return new FailureInfo[size];
        }
      };
}
