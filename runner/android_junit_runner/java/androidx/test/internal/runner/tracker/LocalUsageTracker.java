/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.internal.runner.tracker;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.content.Context;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.services.storage.TestStorage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A usage tracker that tracks the local usage data.
 *
 * <p>It dumps the usage data onto the device as output properties, which will then be recognized
 * and pulled by the testing infra.
 */
public class LocalUsageTracker implements UsageTracker {

  private static final String LOG_TAG = LocalUsageTracker.class.getSimpleName();

  private final Map<String, Serializable> usageTypeToVersion = new HashMap<>();
  private final TestStorage testStorage;

  public LocalUsageTracker(Context context) {
    this(new TestStorage(checkNotNull(context).getContentResolver()));
  }

  @VisibleForTesting
  LocalUsageTracker(TestStorage testStorage) {
    this.testStorage = checkNotNull(testStorage);
  }

  @Override
  public void trackUsage(String usageType, String version) {
    synchronized (usageTypeToVersion) {
      usageTypeToVersion.put("axt_internal." + usageType, version);
    }
  }

  @Override
  public void sendUsages() {
    try {
      testStorage.addOutputProperties(usageTypeToVersion);
    } catch (Exception e) {
      Log.d(
          LOG_TAG,
          "Exception occurred in adding usage data to the output properties. This may be caused by"
              + " the test storage service not installed on the device. Ignore.",
          e);
    }
  }
}
