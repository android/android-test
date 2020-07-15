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
 * Represents a parcelable TestCase. Contains all the information for a test case. Each test method
 * inside a Java test is considered a separate {@link TestCaseInfo}. The {@link TestCaseInfo} is
 * used to serialize and send Java test case information between the test runner and the remote
 * {@link androidx.test.services.events.run.ITestRunEvent} and {@link
 * androidx.test.services.events.discovery.ITestDiscoveryEvent} Android Services.
 *
 * <p>See <a href="https://developer.android.com/reference/android/os/Parcelable.html">Android
 * Parcelable</a>.
 */
public final class TestCaseInfo implements Parcelable {
  /** Name of the test class. */
  @NonNull public final String className;
  /** Name of the test method. */
  @NonNull public final String methodName;
  /** Annotations on the test method. */
  @NonNull public final List<AnnotationInfo> methodAnnotations;
  /** Annotations on the test class. */
  @NonNull public final List<AnnotationInfo> classAnnotations;

  /**
   * Creates an {@link AnnotationInfo} from an Android {@link Parcel}.
   *
   * @param source Android {@link Parcel} to read from
   */
  public TestCaseInfo(@NonNull Parcel source) {
    checkNotNull(source, "source cannot be null");
    className = source.readString();
    methodName = source.readString();
    methodAnnotations = new ArrayList<>();
    source.readTypedList(methodAnnotations, AnnotationInfo.CREATOR);
    classAnnotations = new ArrayList<>();
    source.readTypedList(classAnnotations, AnnotationInfo.CREATOR);
  }

  /**
   * Creates a {@link TestCaseInfo}.
   *
   * @param className Name of the test class
   * @param methodName Name of the test method
   * @param methodAnnotations Annotations on the test method
   * @param classAnnotations Annotations on the test class
   */
  public TestCaseInfo(
      @NonNull String className,
      @NonNull String methodName,
      @NonNull List<AnnotationInfo> methodAnnotations,
      @NonNull List<AnnotationInfo> classAnnotations) {
    checkNotNull(className, "className cannot be null");
    checkNotNull(methodName, "methodName cannot be null");
    checkNotNull(classAnnotations, "classAnnotations cannot be null");
    checkNotNull(methodAnnotations, "methodAnnotations cannot be null");
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

  public static final Parcelable.Creator<TestCaseInfo> CREATOR =
      new Parcelable.Creator<TestCaseInfo>() {
        @Override
        public TestCaseInfo createFromParcel(Parcel source) {
          return new TestCaseInfo(source);
        }

        @Override
        public TestCaseInfo[] newArray(int size) {
          return new TestCaseInfo[size];
        }
      };
}
