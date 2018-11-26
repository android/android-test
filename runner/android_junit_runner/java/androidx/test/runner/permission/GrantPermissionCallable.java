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

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

/** Grants a permission at runtime using a @{link ShellCommand} */
class GrantPermissionCallable extends RequestPermissionCallable {

  private static final String TAG = "GrantPermissionCallable";

  GrantPermissionCallable(
      @NonNull ShellCommand shellCommand, @NonNull Context context, String permission) {
    super(shellCommand, context, permission);
  }

  @Override
  public Result call() throws Exception {
    if (isPermissionGranted()) {
      Log.i(TAG, "Permission: " + getPermission() + " is already granted!");
      return Result.SUCCESS;
    }
    ShellCommand cmdForPermission = getShellCommand();
    try {
      cmdForPermission.execute();
    } finally {
      if (!isPermissionGranted()) {
        // Wait another 1000 ms before we fail
        Thread.sleep(1000);
        if (!isPermissionGranted()) {
          Log.e(TAG, "Permission: " + getPermission() + " cannot be granted!");
          return Result.FAILURE;
        }
      }
    }
    return Result.SUCCESS;
  }
}
