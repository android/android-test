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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a field value on an {@link java.lang.annotation.Annotation}.
 *
 * <p>See <a href="https://developer.android.com/reference/android/os/Parcelable.html">Android
 * Parcelable</a>.
 */
public final class AnnotationValue implements Parcelable {
  /** Name of the field in a {@link java.lang.annotation.Annotation}. */
  @NonNull public final String fieldName;
  /** Values of the annotation's field represented as {@link String}s. */
  @NonNull public final List<String> fieldValues;
  /** The type of the values, e.g. {@link Integer}. */
  @NonNull public final String valueType;

  /**
   * Constructor to create an {@link AnnotationValue}.
   *
   * @param fieldName Name of the field in a {@link java.lang.annotation.Annotation}
   * @param fieldValues Values of the annotation's field represented as {@link String}s
   * @param valueType The type of the values, e.g. {@link Integer}
   */
  public AnnotationValue(
      @NonNull String fieldName, @NonNull List<String> fieldValues, @NonNull String valueType) {
    checkNotNull(fieldName, "fieldName cannot be null");
    checkNotNull(fieldValues, "fieldValues cannot be null");
    checkNotNull(valueType, "valueType cannot be null");
    this.fieldName = fieldName;
    this.fieldValues = fieldValues;
    this.valueType = valueType;
  }

  private AnnotationValue(Parcel source) {
    fieldName = source.readString();
    fieldValues = new ArrayList<>();
    source.readStringList(fieldValues);
    valueType = source.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(fieldName);
    parcel.writeStringList(fieldValues);
    parcel.writeString(valueType);
  }

  public static final Creator<AnnotationValue> CREATOR =
      new Creator<AnnotationValue>() {
        @Override
        public AnnotationValue createFromParcel(Parcel source) {
          return new AnnotationValue(source);
        }

        @Override
        public AnnotationValue[] newArray(int size) {
          return new AnnotationValue[size];
        }
      };
}
