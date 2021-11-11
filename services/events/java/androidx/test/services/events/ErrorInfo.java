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
import androidx.annotation.Nullable;
import androidx.test.services.events.internal.StackTrimmer;
import org.junit.runner.notification.Failure;

/**
 * Denotes an android test error. Has details of the error including stack trace, type, and message.
 */
public final class ErrorInfo implements Parcelable {

  /** The message associated with the error. */
  @Nullable public final String errorMessage;

  /** The type of error. E.g {@code NullPointerException}. */
  @Nullable public final String errorType;

  /** The stack trace associated with the error. */
  @NonNull public final String stackTrace;

  /** Constructor to create a {@link ErrorInfo}. */
  public ErrorInfo(
      @Nullable String errorMessage, @Nullable String errorType, @NonNull String stackTrace) {
    this.errorMessage = errorMessage;
    this.errorType = errorType;
    this.stackTrace = checkNotNull(stackTrace, "stackTrace cannot be null");
  }

  /**
   * Creates a {@link ErrorInfo} from android {@link Parcel}.
   *
   * @param source the android Parcel.
   */
  public ErrorInfo(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    errorMessage = source.readString();
    errorType = source.readString();
    stackTrace = checkNotNull(source.readString(), "stackTrace cannot be null");
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(errorMessage);
    parcel.writeString(errorType);
    parcel.writeString(stackTrace);
  }

  public static final Parcelable.Creator<ErrorInfo> CREATOR =
      new Parcelable.Creator<ErrorInfo>() {
        @Override
        public ErrorInfo createFromParcel(Parcel source) {
          return new ErrorInfo(source);
        }

        @Override
        public ErrorInfo[] newArray(int size) {
          return new ErrorInfo[size];
        }
      };

  public static ErrorInfo createFromFailure(Failure failure) {
    return new ErrorInfo(
        StackTrimmer.getTrimmedMessage(failure),
        failure.getException().getClass().getName(),
        StackTrimmer.getTrimmedStackTrace(failure));
  }
}
