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

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import androidx.test.runner.permission.RequestPermissionCallable.Result;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Base class for runtime permission {@link Callable}s.
 *
 * <p>Note: This class is visible only for testing. Please do not use it directly.
 */
@VisibleForTesting
public abstract class RequestPermissionCallable implements Callable<Result> {

  private final ShellCommand shellCommand;
  private final Context targetContext;
  private final String targetPackage;
  private final String permission;

  /** Result of a permission request. */
  public enum Result {
    SUCCESS,
    FAILURE
  }

  public RequestPermissionCallable(
      @NonNull ShellCommand shellCommand, @NonNull Context targetContext, String permission) {
    this.shellCommand = checkNotNull(shellCommand, "shellCommand cannot be null!");
    this.targetContext = checkNotNull(targetContext, "targetContext cannot be null!");
    String targetPackage = this.targetContext.getPackageName();
    checkState(!TextUtils.isEmpty(targetPackage), "targetPackage cannot be empty or null!");
    this.targetPackage = targetPackage;
    this.permission = permission;
  }

  protected String getPermission() {
    return permission;
  }

  protected boolean isPermissionGranted() {
    return PackageManager.PERMISSION_GRANTED
        == targetContext.checkCallingOrSelfPermission(permission);
  }

  protected ShellCommand getShellCommand() {
    return shellCommand;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestPermissionCallable that = (RequestPermissionCallable) o;
    return Objects.equals(targetPackage, that.targetPackage)
        && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetPackage, permission);
  }
}
