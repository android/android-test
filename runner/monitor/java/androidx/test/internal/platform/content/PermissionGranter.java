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

package androidx.test.internal.platform.content;

import androidx.annotation.NonNull;

/**
 * Requests a runtime permission.
 *
 * <p>Note: This class should not be used directly, but through {@link
 * androidx.test.rule.GrantPermissionRule}.
 */
public interface PermissionGranter {

  /**
   * Adds a permission to the list of permissions which will be requested when {@link
   * #requestPermissions()} is called.
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   *
   * @param permissions a list of Android runtime permissions.
   */
  void addPermissions(@NonNull String... permissions);

  /**
   * Request all permissions previously added using {@link #addPermissions(String...)}
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   */
  void requestPermissions();
}
