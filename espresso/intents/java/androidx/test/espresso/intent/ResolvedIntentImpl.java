/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.intent;

import static androidx.test.espresso.intent.Checks.checkNotNull;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

/** Implementation of {@link ResolvedIntent}. */
class ResolvedIntentImpl extends Intent implements ResolvedIntent {
  private final List<ResolveInfo> possibleResolutions;

  ResolvedIntentImpl(Intent intent, List<ResolveInfo> possibleResolutions) {
    super(checkNotNull(intent));
    this.possibleResolutions = checkNotNull(possibleResolutions);
  }

  @Override
  public final boolean canBeHandledBy(String appPackage) {
    checkNotNull(appPackage);
    for (String pkg : getPossibleResolutionPackages()) {
      if (appPackage.equals(pkg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Intent getIntent() {
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "%s handling packages:[%s]", super.toString(), getPossibleResolutionPackages()));
    Bundle extrasBundle = getExtras();

    if (extrasBundle != null) {
      sb.append(String.format(", extras:[%s]", extrasBundle.toString()));
    }

    return sb.toString();
  }

  private List<String> getPossibleResolutionPackages() {
    List<String> packages = new ArrayList<String>();
    for (ResolveInfo info : possibleResolutions) {
      packages.add(info.activityInfo.packageName);
    }
    return packages;
  }
}
