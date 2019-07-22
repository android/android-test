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
package androidx.test.services.events.discovery;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parcelable TestCase. Contains all the information for a test case. Each test method
 * inside a java test is considered a separate {@link TestCase}. The {@link TestCase} is used to
 * serialize and send java test case information between test runners. See <a
 * href="https://developer.android.com/reference/android/os/Parcelable.html">Android Parcelable </a>
 */
public class TestCase implements Parcelable {
  private final String className;
  private final String methodName;
  private List<Annotation> methodAnnotations = new ArrayList<>();
  private List<Annotation> classAnnotations = new ArrayList<>();

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<Annotation> getMethodAnnotations() {
    return methodAnnotations;
  }

  public List<Annotation> getClassAnnotations() {
    return classAnnotations;
  }

  /**
   * Constructor to create an {@link Annotation} given an Android Parcel.
   *
   * @param source Android {@link Parcel} to read from.
   */
  private TestCase(Parcel source) {
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
   * @param methodJavaAnnotations Annotations on the test method.
   * @param classJavaAnnotations Annotations on the test class.
   */
  public TestCase(
      String className,
      String methodName,
      List<java.lang.annotation.Annotation> methodJavaAnnotations,
      List<java.lang.annotation.Annotation> classJavaAnnotations) {
    this.className = className;
    this.methodName = methodName;
    AnnotationToParcelableParser annotationToParcelableParser = new AnnotationToParcelableParser();

    for (java.lang.annotation.Annotation annotation : methodJavaAnnotations) {
      annotationToParcelableParser.setJavaAnnotation(annotation);
      methodAnnotations.add(annotationToParcelableParser.parse());
    }

    for (java.lang.annotation.Annotation annotation : classJavaAnnotations) {
      annotationToParcelableParser.setJavaAnnotation(annotation);
      classAnnotations.add(annotationToParcelableParser.parse());
    }
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
