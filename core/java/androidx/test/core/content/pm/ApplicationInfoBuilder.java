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

import android.content.pm.ApplicationInfo;
import androidx.annotation.Nullable;

/** Builder for {@link ApplicationInfo}. */
public final class ApplicationInfoBuilder {
  @Nullable private String name;
  @Nullable private String packageName;

  private ApplicationInfoBuilder() {}

  /**
   * Start building a new {@link ApplicationInfo}.
   *
   * @return a new instance of {@link ApplicationInfoBuilder}.
   */
  public static ApplicationInfoBuilder newBuilder() {
    return new ApplicationInfoBuilder();
  }

  /**
   * Sets the packageName.
   *
   * @see ApplicationInfo#packageName
   */
  public ApplicationInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /**
   * Sets the name.
   *
   * <p>Default is {@code null}.
   *
   * @see ApplicationInfo#name
   */
  public ApplicationInfoBuilder setName(@Nullable String name) {
    this.name = name;
    return this;
  }

  /** Returns a {@link ApplicationInfo} with the provided data. */
  public ApplicationInfo build() {
    checkNotNull(packageName, "Mandatory field 'packageName' missing.");

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.name = name;
    applicationInfo.packageName = packageName;

    return applicationInfo;
  }
}
