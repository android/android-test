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

package com.google.android.apps.common.testing.broker;

import com.google.auto.value.AutoValue;

/**
 * Represents an instrumentation installed on an android device.
 *
 */
@AutoValue
public abstract class Instrumentation {

  private static Instrumentation create(Builder builder) {
    return new AutoValue_Instrumentation(
        builder.androidPackage,
        builder.targetPackage,
        builder.instrumentationClass);
  }

  Instrumentation() {}

  /** The Android package specified in the Android manifest that installed the instrumentation. */
  public abstract String getAndroidPackage();

  /** The Android package this instrumentation targets. */
  public abstract String getTargetPackage();

  /** The actual java class of the instrumentation. */
  public abstract String getInstrumentationClass();

  /** Returns the package name and instrumentation class name separated by '/'. */
  public String getFullName() {
    return getAndroidPackage() + "/" + getInstrumentationClass();
  }

  /**
   * Return the fully qualified class name of the instrumentation.
   */
  public String getFullInstrumentationClass() {
    if (getInstrumentationClass().startsWith(".")) {
      return getAndroidPackage() + getInstrumentationClass();
    }
    return getInstrumentationClass();
  }

  @Override
  public String toString() {
    return getFullName();
  }

  /**
   * Builds a new Instrumentation object.
   */
  public static class Builder {
    private String androidPackage;
    private String instrumentationClass;
    private String targetPackage;

    public Builder withAndroidPackage(String androidPackage) {
      this.androidPackage = androidPackage;
      return this;
    }

    public Builder withTargetPackage(String targetPackage) {
      this.targetPackage = targetPackage;
      return this;
    }

    public Builder withInstrumentationClass(String instrumentationClass) {
      this.instrumentationClass = instrumentationClass;
      return this;
    }

    public Instrumentation build() {
      return Instrumentation.create(this);
    }
  }
}
