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
 * Represents a parcelable {@link java.lang.annotation.Annotation} to be used by the {@link
 * androidx.test.services.events.run.ITestRunEvent} and {@link
 * androidx.test.services.events.discovery.ITestDiscoveryEvent} Android Services.
 *
 * <p>See <a href="https://developer.android.com/reference/android/os/Parcelable.html">Android
 * Parcelable</a>.
 */
public final class AnnotationInfo implements Parcelable {
  /** Name of the annotation. For example: {@code com.java.Foo}. */
  @NonNull public final String name;
  /** The annotation's field values, if any. */
  @NonNull public final List<AnnotationValue> values;

  /**
   * Constructor to create an {@link AnnotationInfo}.
   *
   * @param annotationName Name of the annotation e.g. {@code com.java.Foo}
   * @param annotationValues the values of the annotation's fields
   */
  public AnnotationInfo(
      @NonNull String annotationName, @NonNull List<AnnotationValue> annotationValues) {
    checkNotNull(annotationName, "annotationName cannot be null");
    checkNotNull(annotationName, "annotationValues cannot be null");
    this.name = annotationName;
    this.values = annotationValues;
  }

  private AnnotationInfo(@NonNull Parcel source) {
    name = source.readString();
    values = new ArrayList<>();
    source.readTypedList(values, AnnotationValue.CREATOR);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(name);
    parcel.writeTypedList(values);
  }

  public static final Creator<AnnotationInfo> CREATOR =
      new Creator<AnnotationInfo>() {
        @Override
        public AnnotationInfo createFromParcel(Parcel source) {
          return new AnnotationInfo(source);
        }

        @Override
        public AnnotationInfo[] newArray(int size) {
          return new AnnotationInfo[size];
        }
      };
}
