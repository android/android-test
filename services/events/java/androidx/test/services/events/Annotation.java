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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parcelable {@link java.lang.annotation.Annotation} to be used by Android Services.
 * See <a href="https://developer.android.com/reference/android/os/Parcelable.html">Android
 * Parcelable </a>
 */
public final class Annotation implements Parcelable {

  public final String name;
  public final List<AnnotationValue> values;

  /**
   * Constructor to create an {@link Annotation}.
   *
   * @param annotationName Name of the annotation E.g "com.java.Foo"
   * @param annotationValues Data class containing the values of the annotation.
   */
  public Annotation(String annotationName, List<AnnotationValue> annotationValues) {
    this.name = annotationName;
    this.values = annotationValues;
  }

  private Annotation(Parcel source) {
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

  public static final Creator<Annotation> CREATOR =
      new Creator<Annotation>() {
        @Override
        public Annotation createFromParcel(Parcel source) {
          return new Annotation(source);
        }

        @Override
        public Annotation[] newArray(int size) {
          return new Annotation[size];
        }
      };
}
