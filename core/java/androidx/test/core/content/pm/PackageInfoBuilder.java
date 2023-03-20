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

import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Builder for {@link PackageInfo}. */
public final class PackageInfoBuilder {
  @Nullable private String packageName;
  @Nullable private ApplicationInfo applicationInfo;
  private long longVersionCode = 0L;
  @Nullable private String versionName;
  /** Map of a requested permission to its requested permission flag. */
  private final Map<String, Integer> requestedPermissionsMap = new HashMap<>();

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
   * <p>On SDK P+, this value will be returned for both {@link PackageInfo#getLongVersionCode()} and
   * {@link PackageInfo#versionCode}. Note that the value of {@link PackageInfo#versionCode} will be
   * truncated if a value larger than Integer.MAX_VALUE is provided.
   *
   * <p>Default is 0L.
   *
   * @see PackageInfo#setLongVersionCode(long)
   * @see PackageInfo#versionCode
   */
  @TargetApi(Build.VERSION_CODES.P)
  public PackageInfoBuilder setVersionCode(long longVersionCode) {
    this.longVersionCode = longVersionCode;
    return this;
  }

  /**
   * Sets the version name.
   *
   * <p>Default is {@code null}.
   *
   * @see PackageInfo#versionName
   */
  public PackageInfoBuilder setVersionName(String versionName) {
    this.versionName = versionName;
    return this;
  }

  /**
   * Adds a requested permission and its flag for the app.
   *
   * <p>This can be called several times to add multiple permissions.
   *
   * @see PackageInfo#requestedPermissions
   * @see PackageInfo#requestedPermissionsFlags
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public PackageInfoBuilder addRequestedPermission(
      String requestedPermission, int requestedPermissionFlag) {
    requestedPermissionsMap.put(requestedPermission, requestedPermissionFlag);
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
    packageInfo.versionName = versionName;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      // setLongVersionCode will automatically set the version code.
      packageInfo.setLongVersionCode(longVersionCode);
    } else {
      packageInfo.versionCode = (int) longVersionCode;
    }

    if (applicationInfo == null) {
      applicationInfo = ApplicationInfoBuilder.newBuilder().setPackageName(packageName).build();
    }
    packageInfo.applicationInfo = applicationInfo;
    packageInfo.requestedPermissions = requestedPermissionsMap.keySet().toArray(new String[0]);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      Integer[] requestedPermissionsFlags =
          requestedPermissionsMap.values().toArray(new Integer[0]);
      // Stream APIs such as `mapToInt` are not supported below API 24.
      int[] requestedPermissionsFlagsIntArray = new int[requestedPermissionsFlags.length];
      for (int i = 0; i < requestedPermissionsFlags.length; i++) {
        requestedPermissionsFlagsIntArray[i] = requestedPermissionsFlags[i];
      }
      packageInfo.requestedPermissionsFlags = requestedPermissionsFlagsIntArray;
    }

    checkState(
        packageInfo.packageName.equals(packageInfo.applicationInfo.packageName),
        "Field 'packageName' must match field 'applicationInfo.packageName'");

    return packageInfo;
  }
}
