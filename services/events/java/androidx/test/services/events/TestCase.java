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
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parcelable TestCase. Contains all the information for a test case. Each test method
 * inside a java test is considered a separate {@link TestCase}. The {@link TestCase} is used to
 * serialize and send java test case information between test runners. See <a
 * href="https://developer.android.com/reference/android/os/Parcelable.html">Android Parcelable </a>
 */
public final class TestCase implements Parcelable {
  public final String className;
  public final String methodName;
  public final List<Annotation> methodAnnotations;
  public final List<Annotation> classAnnotations;

  /**
   * Constructor to create an {@link Annotation} given an Android Parcel.
   *
   * @param source Android {@link Parcel} to read from.
   */
  public TestCase(@NonNull Parcel source) {
    className = source.readString();
    methodName = source.readString();
    methodAnnotations = new ArrayList<>();
    source.readTypedList(methodAnnotations, Annotation.CREATOR);
    classAnnotations = new ArrayList<>();
    source.readTypedList(classAnnotations, Annotation.CREATOR);
  }

  /**
   * Constructor to create a {@link TestCase}.
   *
   * @param className Name of the test class.
   * @param methodName Name of the test method.
   * @param methodAnnotations Annotations on the test method.
   * @param classAnnotations Annotations on the test class.
   */
  public TestCase(
      @NonNull String className,
      @NonNull String methodName,
      @NonNull List<Annotation> methodAnnotations,
      @NonNull List<Annotation> classAnnotations) {
    this.className = className;
    this.methodName = methodName;
    this.classAnnotations = classAnnotations;
    this.methodAnnotations = methodAnnotations;
  }

  @NonNull
  public String getClassAndMethodName() {
    return className + "#" + methodName;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(className);
    parcel.writeString(methodName);
    parcel.writeTypedList(methodAnnotations);
    parcel.writeTypedList(classAnnotations);
  }

  public static final Parcelable.Creator<TestCase> CREATOR =
      new Parcelable.Creator<TestCase>() {
        @Override
        public TestCase createFromParcel(Parcel source) {
          return new TestCase(source);
        }

        @Override
        public TestCase[] newArray(int size) {
          return new TestCase[size];
        }
      };
}
