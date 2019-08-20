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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Denotes an android test failure, has details of the failure including stack trace / type and
 * message.
 */
public final class Failure implements Parcelable {

  /** The failure message associated with the failure. */
  private final String failureMessage;

  /** The Type of failure exception. E.g NullPointerException */
  private final String failureType;

  /** The stack trace associated with the failure. */
  private final String stackTrace;

  public String getFailureMessage() {
    return failureMessage;
  }

  public String getFailureType() {
    return failureType;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  /** Constructor to create a {@link Failure}. */
  public Failure(String failureMessage, String failureType, String stackTrace) {
    this.failureMessage = failureMessage;
    this.failureType = failureType;
    this.stackTrace = stackTrace;
  }

  /**
   * Creates a {@link Failure} from android {@link Parcel}.
   *
   * @param source the android Parcel.
   */
  public Failure(Parcel source) {
    failureMessage = source.readString();
    failureType = source.readString();
    stackTrace = source.readString();
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
  }

  public static final Parcelable.Creator<Failure> CREATOR =
      new Parcelable.Creator<Failure>() {
        @Override
        public Failure createFromParcel(Parcel source) {
          return new Failure(source);
        }

        @Override
        public Failure[] newArray(int size) {
          return new Failure[size];
        }
      };
}
