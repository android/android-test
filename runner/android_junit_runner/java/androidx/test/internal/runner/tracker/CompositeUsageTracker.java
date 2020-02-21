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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A composite usage tracker that tracks usage data for all the added trackers. */
public class CompositeUsageTracker implements UsageTracker {

  private final List<UsageTracker> usageTrackers = new ArrayList<>();

  public CompositeUsageTracker(UsageTracker... trackers) {
    Collections.addAll(usageTrackers, checkNotNull(trackers));
  }

  /** Adds a new tracker. */
  public void addTracker(UsageTracker tracker) {
    usageTrackers.add(checkNotNull(tracker));
  }

  @Override
  public void trackUsage(String usage, String version) {
    for (UsageTracker usageTracker : usageTrackers) {
      usageTracker.trackUsage(usage, version);
    }
  }

  @Override
  public void sendUsages() {
    for (UsageTracker usageTracker : usageTrackers) {
      usageTracker.sendUsages();
    }
  }
}
