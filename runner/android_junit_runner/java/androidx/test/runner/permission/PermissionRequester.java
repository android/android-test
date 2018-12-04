/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.runner.permission;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import androidx.test.annotation.Beta;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.permission.UiAutomationShellCommand.PmCommand;
import java.util.HashSet;

/**
 * Requests a runtime permission on devices running Android M (API 23) and above.
 *
 * <p>This class is usually used to grant runtime permissions to avoid the permission dialog from
 * showing up and blocking the App's Ui. This is especially helpful for Ui-Testing to avoid loosing
 * control over your application under test.
 *
 * <p>The requested permissions will be granted for all test methods in the test class. Use {@link
 * #addPermissions(String...)} to add a permission to the permission list. To request all
 * permissions use the {@link #requestPermissions()} method.
 *
 * <p>Note: Usually this class would not be used directly, but through {@link
 * androidx.test.rule.GrantPermissionRule}.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
@TargetApi(value = 23)
public class PermissionRequester implements PermissionGranter {

  private static final String TAG = "PermissionRequester";

  private int androidRuntimeVersion = Build.VERSION.SDK_INT;

  @NonNull private final Context targetContext;

  @VisibleForTesting
  final HashSet<RequestPermissionCallable> requestedPermissions = new HashSet<>();

  public PermissionRequester() {
    this(InstrumentationRegistry.getInstrumentation().getTargetContext());
  }

  @VisibleForTesting
  PermissionRequester(@NonNull Context targetContext) {
    this.targetContext = checkNotNull(targetContext, "targetContext cannot be null!");
  }

  /**
   * Adds a permission to the list of permissions which will be requested when {@link
   * #requestPermissions()} is called.
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   *
   * @param permissions a list of Android runtime permissions.
   */
  public void addPermissions(@NonNull String... permissions) {
    checkNotNull(permissions, "permissions cannot be null!");
    if (deviceSupportsRuntimePermissions()) {
      for (String permission : permissions) {
        assertFalse("Permission String is empty or null!", TextUtils.isEmpty(permission));
        GrantPermissionCallable requestPermissionCallable =
            new GrantPermissionCallable(
                new UiAutomationShellCommand(
                    targetContext.getPackageName(), permission, PmCommand.GRANT_PERMISSION),
                targetContext,
                permission);
        checkState(requestedPermissions.add(requestPermissionCallable));
      }
    }
  }

  /**
   * Request all permissions previously added using {@link #addPermissions(String...)}
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   */
  public void requestPermissions() {
    if (deviceSupportsRuntimePermissions()) {
      for (RequestPermissionCallable requestPermissionCallable : requestedPermissions) {
        try {
          if (RequestPermissionCallable.Result.FAILURE == requestPermissionCallable.call()) {
            fail("Failed to grant permissions, see logcat for details");
            return;
          }
        } catch (Exception exception) {
          Log.e(TAG, "An Exception was thrown while granting permission", exception);
          fail("Failed to grant permissions, see logcat for details");
          return;
        }
      }
    }
  }

  @VisibleForTesting
  protected void setAndroidRuntimeVersion(int sdkInt) {
    androidRuntimeVersion = sdkInt;
  }

  private boolean deviceSupportsRuntimePermissions() {
    boolean supportsRuntimePermissions = getAndroidRuntimeVersion() >= 23;
    if (!supportsRuntimePermissions) {
      // TODO: replace with Assume.assumeTrue() once we bumped gradle plugin version to 2.0
      Log.w(
          TAG,
          "Permissions can only be granted on devices running Android M (API 23) or"
              + "higher. This rule is ignored.");
    }
    return supportsRuntimePermissions;
  }

  private int getAndroidRuntimeVersion() {
    return androidRuntimeVersion;
  }
}
