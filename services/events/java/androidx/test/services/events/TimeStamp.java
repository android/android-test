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
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Denotes an android test error. Has details of the error including stack trace, type, and message.
 */
public final class TimeStamp implements Parcelable {

  /** Represents sends of UTC time since the Unix epoch. */
  @NonNull public final Long seconds;

  /** Timestamp nanoseconds - must be non negative even if seconds is negative. */
  @NonNull public final Integer nanos;

  /** Constructor to create a {@link TimeStamp}. */
  public TimeStamp(@NonNull Long seconds, @NonNull Integer nanos) {
    this.seconds = checkNotNull(seconds, "seconds cannot be null");
    this.nanos = checkNotNull(nanos, "nanos cannot be null");
  }

  /**
   * Creates a {@link TimeStamp} from android {@link Parcel}.
   *
   * @param source the android Parcel.
   */
  public TimeStamp(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    seconds = checkNotNull(source.readLong(), "seconds cannot be null");
    nanos = checkNotNull(source.readInt(), "nanos cannot be null");
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeLong(seconds);
    parcel.writeInt(nanos);
  }

  public static final Parcelable.Creator<TimeStamp> CREATOR =
      new Parcelable.Creator<TimeStamp>() {
        @Override
        public TimeStamp createFromParcel(Parcel source) {
          return new TimeStamp(source);
        }

        @Override
        public TimeStamp[] newArray(int size) {
          return new TimeStamp[size];
        }
      };

  public static TimeStamp now() {
    long epochNanos = System.nanoTime();
    long epochSeconds = NANOSECONDS.toSeconds(epochNanos);
    return new TimeStamp(epochSeconds, (int) (epochNanos - SECONDS.toNanos(epochSeconds)));
  }
}
