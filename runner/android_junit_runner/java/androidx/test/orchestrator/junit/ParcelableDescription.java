/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import org.junit.runner.Description;

/** Parcelable imitation of a JUnit ParcelableDescription */
public final class ParcelableDescription implements Parcelable {
  private final String className;
  private final String methodName;
  private final String displayName;

  public ParcelableDescription(Description description) {
    this.className = description.getClassName();
    this.methodName = description.getMethodName();
    this.displayName = description.getDisplayName();
  }

  public ParcelableDescription(String classAndMethodName) {
    String[] classAndMethodNames = classAndMethodName.split("#");
    if (classAndMethodNames.length > 0) {
      this.className = classAndMethodNames[0];
      this.methodName = classAndMethodNames.length > 1 ? classAndMethodNames[1] : "";
    } else {
      this.className = "";
      this.methodName = "";
    }
    this.displayName = String.format("%s(%s)", methodName, className);
  }

  private ParcelableDescription(Parcel in) {
    className = getNonNullString(in);
    methodName = getNonNullString(in);
    displayName = getNonNullString(in);
  }

  private String getNonNullString(Parcel in) {
    String str = in.readString();
    return str == null ? "" : str;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(className);
    out.writeString(methodName);
    out.writeString(displayName);
  }

  public static final Parcelable.Creator<ParcelableDescription> CREATOR =
      new Creator<ParcelableDescription>() {
        @Override
        public ParcelableDescription createFromParcel(Parcel in) {
          return new ParcelableDescription(in);
        }

        @Override
        public ParcelableDescription[] newArray(int size) {
          return new ParcelableDescription[size];
        }
      };

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
