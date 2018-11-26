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
package androidx.test.core.content.pm;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import androidx.annotation.Nullable;

/** Builder for {@link PackageInfo}. */
public final class PackageInfoBuilder {
  @Nullable private String packageName;
  @Nullable private ApplicationInfo applicationInfo;

  private PackageInfoBuilder() {}

  /**
   * Start building a new {@link PackageInfo}.
   *
   * @return a new instance of {@link PackageInfoBuilder}.
   */
  public static PackageInfoBuilder newBuilder() {
    return new PackageInfoBuilder();
  }

  /**
   * Sets the package name.
   *
   * <p>Default is {@code null}.
   *
   * @see PackageInfo#packageName
   */
  public PackageInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /**
   * Sets the application info.
   *
   * <p>Default is {@code null}
   *
   * @see PackageInfo#applicationInfo
   */
  public PackageInfoBuilder setApplicationInfo(ApplicationInfo applicationInfo) {
    this.applicationInfo = applicationInfo;
    return this;
  }

  /** Returns a {@link PackageInfo} with the provided data. */
  public PackageInfo build() {
    // Check mandatory fields and correctness.
    checkNotNull(packageName, "Mandatory field 'packageName' missing.");

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    if (applicationInfo == null) {
      applicationInfo = ApplicationInfoBuilder.newBuilder().setPackageName(packageName).build();
    }
    packageInfo.applicationInfo = applicationInfo;

    checkState(
        packageInfo.packageName.equals(packageInfo.applicationInfo.packageName),
        "Field 'packageName' must match field 'applicationInfo.packageName'");

    return packageInfo;
  }
}
