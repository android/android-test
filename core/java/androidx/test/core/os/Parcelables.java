/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.core.os;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/** Testing utilities for {@link Parcelable}s. */
public final class Parcelables {
  /**
   * Parcelables are lazily marshalled, meaning that in typical testing, no marshalling would occur
   * and would therefore go untested. This forces marshalling to happen for a Parcelable.
   *
   * <p>This utility will marshall the provided Parcelable, and attempt to recreate it with the
   * given CREATOR. It is up to the caller to validate the two instances are equivalent.
   *
   * @param parcelable the parcelable to marshall.
   * @param creator the CREATOR field for that parcelable.
   * @return a new instance of the parcelable that has been unmarshalled.
   */
  public static <T extends Parcelable> T forceParcel(T parcelable, Creator<T> creator) {
    Parcel parcel = Parcel.obtain();
    try {
      parcelable.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      return creator.createFromParcel(parcel);
    } finally {
      parcel.recycle();
    }
  }

  private Parcelables() {}
}

