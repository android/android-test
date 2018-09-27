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

package androidx.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.junit.runner.notification.Failure;

/** Parcelable imitation of a JUnit ParcelableFailure */
public final class ParcelableFailure implements Parcelable {

  private static final String TAG = "ParcelableFailure";

  private static final int MAX_STREAM_LENGTH = 16 * 1024;

  private final ParcelableDescription mDescription;
  private final String mTrace;

  public ParcelableFailure(Failure failure) {
    this.mDescription = new ParcelableDescription(failure.getDescription());
    this.mTrace = failure.getTrace();
  }

  private ParcelableFailure(Parcel in) {
    mDescription = in.readParcelable(ParcelableDescription.class.getClassLoader());
    mTrace = in.readString();
  }

  public ParcelableFailure(ParcelableDescription description, Throwable t) {
    mDescription = description;
    mTrace = trimToLength(t.getMessage());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeParcelable(mDescription, 0);
    out.writeString(mTrace);
  }

  public static final Creator<ParcelableFailure> CREATOR =
      new Creator<ParcelableFailure>() {
        @Override
        public ParcelableFailure createFromParcel(Parcel in) {
          return new ParcelableFailure(in);
        }

        @Override
        public ParcelableFailure[] newArray(int size) {
          return new ParcelableFailure[size];
        }
      };

  private static String trimToLength(String trace) {
    if (trace.length() > MAX_STREAM_LENGTH) {
      Log.i(
          TAG,
          String.format(
              "Stack trace too long, trimmed to first %s characters.", MAX_STREAM_LENGTH));
      return trace.substring(0, MAX_STREAM_LENGTH) + "\n";
    } else {
      return trace + "\n";
    }
  }

  public String getTrace() {
    return mTrace;
  }

  public ParcelableDescription getDescription() {
    return mDescription;
  }
}
