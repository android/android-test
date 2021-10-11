/*
 * Copyright (C) 2017 The Android Open Source Project
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

/**
 * A registry to hold the global {@link UsageTracker}.
 *
 * <p>Instrumentation will configure this registry at startup.
 *
 * @deprecated obsolete
 */
@Deprecated
public final class UsageTrackerRegistry {

  /**
   * Contains versions for AXT libraries
   *
   * @deprecated obsolete
   */
  @Deprecated
  public interface AxtVersions {
    // Espresso version includes: Espresso, Espresso-Web, Intents, Espresso-MPE
    String ESPRESSO_VERSION = "na";
    // Runner version includes: Runner
    String RUNNER_VERSION = "na";
    // Test services version.
    String SERVICES_VERSION = "na";
  }

  // By default we use a NoOp class.
  private static volatile UsageTracker instance = new UsageTracker.NoOpUsageTracker();

  public static void registerInstance(UsageTracker tracker) {
    instance = checkNotNull(tracker);
  }

  public static UsageTracker getInstance() {
    return instance;
  }

  private UsageTrackerRegistry() {}
}
