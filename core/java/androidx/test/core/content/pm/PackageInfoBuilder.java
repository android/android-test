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
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/** Builder for {@link PackageInfo}. */
public final class PackageInfoBuilder {
  @Nullable private String packageName;
  @Nullable private ApplicationInfo applicationInfo;
  private int versionCode = 0;
  private String versionName = "";
  @Nullable private String[] requestedPermissions;

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  @Nullable
  private int[] requestedPermissionsFlags;

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
   * Sets the version code.
   *
   * <p>Default is 0.
   *
   * @see PackageInfo#versionCode
   */
  public PackageInfoBuilder setVersionCode(int versionCode) {
    this.versionCode = versionCode;
    return this;
  }

  /**
   * Sets the version name.
   *
   * <p>Default is an empty string.
   *
   * @see PackageInfo#versionName
   */
  public PackageInfoBuilder setVersionName(String versionName) {
    this.versionName = versionName;
    return this;
  }

  /**
   * Sets the requested permissions by the app.
   *
   * <p>Default is {@code null}.
   *
   * @see PackageInfo#requestedPermissions
   */
  public PackageInfoBuilder setRequestedPermissions(String[] requestedPermissions) {
    this.requestedPermissions = requestedPermissions;
    return this;
  }

  /**
   * Sets the flags for the requested permissions by the app.
   *
   * <p>Default is {@code null}.
   *
   * @see PackageInfo#requestedPermissionsFlags
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  public PackageInfoBuilder setRequestedPermissionsFlags(int[] requestedPermissionsFlags) {
    this.requestedPermissionsFlags = requestedPermissionsFlags;
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
    packageInfo.versionCode = versionCode;
    packageInfo.versionName = versionName;

    if (applicationInfo == null) {
      applicationInfo = ApplicationInfoBuilder.newBuilder().setPackageName(packageName).build();
    }
    packageInfo.applicationInfo = applicationInfo;
    packageInfo.requestedPermissions = requestedPermissions;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      packageInfo.requestedPermissionsFlags = requestedPermissionsFlags;
      if (requestedPermissions != null && requestedPermissionsFlags != null) {
        checkState(
            requestedPermissions.length == requestedPermissionsFlags.length,
            "Field 'requestedPermissions' must match size of 'requestedPermissionsFlags'");
      }
    }

    checkState(
        packageInfo.packageName.equals(packageInfo.applicationInfo.packageName),
        "Field 'packageName' must match field 'applicationInfo.packageName'");

    return packageInfo;
  }
}
