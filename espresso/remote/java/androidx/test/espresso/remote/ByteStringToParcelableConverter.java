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
package androidx.test.espresso.remote;

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import androidx.annotation.NonNull;
import com.google.protobuf.ByteString;
import java.lang.reflect.Field;
import java.util.Locale;

/** Converts a {@link ByteString} into a {@link Parcelable}. */
final class ByteStringToParcelableConverter implements Converter<ByteString, Parcelable> {

  private final Class<Parcelable> parcelableClass;

  /**
   * Returns an instance of {@link ByteStringToParcelableConverter}.
   *
   * @param parcelableClass the class of the {@link Parcelable}
   */
  ByteStringToParcelableConverter(@NonNull Class<Parcelable> parcelableClass) {
    this.parcelableClass = checkNotNull(parcelableClass, "parcelableClass cannot be null!");
  }

  /**
   * Converts a {@link ByteString} into a {@link Parcelable}.
   *
   * @param byteString the {@link ByteString} encoding of the {@link Parcelable}
   * @return an instance of {@link Parcelable}
   */
  @Override
  public Parcelable convert(@NonNull ByteString byteString) {
    Parcel parcel = Parcel.obtain();
    byte[] bytes = byteString.toByteArray();
    Parcelable fromParcel = null;
    try {
      parcel.unmarshall(bytes, 0, bytes.length);
      // Move the current read/write position to the beginning
      parcel.setDataPosition(0);
      Field creatorField = parcelableClass.getField("CREATOR");
      Parcelable.Creator<?> creator = (Creator) creatorField.get(null);
      fromParcel = parcelableClass.cast(creator.createFromParcel(parcel));
    } catch (NoSuchFieldException nsfe) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Cannot find CREATOR field for Parcelable %s",
              parcelableClass.getName()),
          nsfe);
    } catch (IllegalAccessException iae) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Cannot create instance of %s. CREATOR field is inaccessible",
              parcelableClass.getName()),
          iae);
    } finally {
      if (parcel != null) {
        parcel.recycle();
      }
    }
    return fromParcel;
  }
}
