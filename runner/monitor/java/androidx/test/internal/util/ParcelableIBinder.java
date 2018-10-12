/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package androidx.test.internal.util;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper class to help create parcelable IBinder.
 *
 * <p>Required to maintain backwards comparability back to API level 8. Since {@code
 * Bundle.putBinder(...)} was only introduced in API level 18.
 */
public class ParcelableIBinder implements Parcelable {

  private final IBinder iBinder;

  public ParcelableIBinder(IBinder iBinder) {
    this.iBinder = checkNotNull(iBinder);
  }

  public IBinder getIBinder() {
    return iBinder;
  }

  protected ParcelableIBinder(Parcel in) {
    iBinder = in.readStrongBinder();
  }

  public static final Creator<ParcelableIBinder> CREATOR =
      new Creator<ParcelableIBinder>() {
        @Override
        public ParcelableIBinder createFromParcel(Parcel in) {
          return new ParcelableIBinder(in);
        }

        @Override
        public ParcelableIBinder[] newArray(int size) {
          return new ParcelableIBinder[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeStrongBinder(iBinder);
  }
}
