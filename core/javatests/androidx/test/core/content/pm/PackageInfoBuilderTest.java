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

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.content.pm.PackageInfo;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link PackageInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
public final class PackageInfoBuilderTest {

  private static final String TEST_PACKAGE_NAME = "test.package.name";
  private static final String SECOND_TEST_PACKAGE_NAME = "second.test.package.name";
  private static final String TEST_REQUESTED_PERMISSION = "android.permission.test";
  private static final long TEST_VERSION_CODE = 123;
  private static final String TEST_VERSION_NAME = "v1";

  @Test
  public void buildAllFields() {
    PackageInfo packageInfo =
        PackageInfoBuilder.newBuilder()
            .setPackageName(TEST_PACKAGE_NAME)
            .setApplicationInfo(
                ApplicationInfoBuilder.newBuilder().setPackageName(TEST_PACKAGE_NAME).build())
            .addRequestedPermission(
                TEST_REQUESTED_PERMISSION, PackageInfo.REQUESTED_PERMISSION_GRANTED)
            .setVersionCode(TEST_VERSION_CODE)
            .setVersionName(TEST_VERSION_NAME)
            .build();

    assertThat(packageInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(packageInfo.applicationInfo).isNotNull();
    assertThat(packageInfo.requestedPermissions).hasLength(1);
    assertThat(packageInfo.requestedPermissions[0]).isEqualTo(TEST_REQUESTED_PERMISSION);
    assertThat(packageInfo.versionCode).isEqualTo(TEST_VERSION_CODE);
    assertThat(packageInfo.versionName).isEqualTo(TEST_VERSION_NAME);
  }

  @Test
  public void build_withLongVersionCode() {
    // API added in API 28.
    assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);
    PackageInfo packageInfo =
        PackageInfoBuilder.newBuilder()
            .setPackageName(TEST_PACKAGE_NAME)
            .setApplicationInfo(
                ApplicationInfoBuilder.newBuilder().setPackageName(TEST_PACKAGE_NAME).build())
            .setVersionCode(TEST_VERSION_CODE)
            .build();

    assertThat(packageInfo.getLongVersionCode()).isEqualTo(TEST_VERSION_CODE);
    assertThat(packageInfo.versionCode).isEqualTo(TEST_VERSION_CODE);
  }

  @Test
  public void build_withRequestedPermissionsFlags() {
    // API added in API 16 or newer.
    assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
    int permissionFlag = PackageInfo.REQUESTED_PERMISSION_GRANTED;

    PackageInfo packageInfo =
        PackageInfoBuilder.newBuilder()
            .setPackageName(TEST_PACKAGE_NAME)
            .setApplicationInfo(
                ApplicationInfoBuilder.newBuilder().setPackageName(TEST_PACKAGE_NAME).build())
            .addRequestedPermission(TEST_REQUESTED_PERMISSION, permissionFlag)
            .build();

    assertThat(packageInfo.requestedPermissions).hasLength(1);
    assertThat(packageInfo.requestedPermissions[0]).isEqualTo(TEST_REQUESTED_PERMISSION);
    assertThat(packageInfo.requestedPermissionsFlags).hasLength(1);
    assertThat(packageInfo.requestedPermissionsFlags[0]).isEqualTo(permissionFlag);
  }

  @Test
  public void defaultApplicationInfoIsValid() {
    PackageInfoBuilder builder = PackageInfoBuilder.newBuilder().setPackageName(TEST_PACKAGE_NAME);

    PackageInfo packageInfo = builder.build();

    assertThat(packageInfo.applicationInfo).isNotNull();
    assertThat(packageInfo.applicationInfo.packageName).isEqualTo(packageInfo.packageName);
  }

  @Test
  public void build_throwsException_whenPackageNameMissing() throws Exception {
    try {
      PackageInfoBuilder.newBuilder().build();
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Mandatory field 'packageName' missing.");
    }
  }

  @Test
  public void build_throwsException_whenPackageNameMismatched() {
    PackageInfoBuilder builder =
        PackageInfoBuilder.newBuilder()
            .setPackageName(TEST_PACKAGE_NAME)
            .setApplicationInfo(
                ApplicationInfoBuilder.newBuilder()
                    .setPackageName(SECOND_TEST_PACKAGE_NAME)
                    .build());

    try {
      builder.build();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("Field 'packageName' must match field 'applicationInfo.packageName'");
    }
  }
}
