/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.runner.lifecycle;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An exposed registry instance to make it easy for callers to find the lifecycle monitor for their
 * application.
 */
public final class ActivityLifecycleMonitorRegistry {

  private static final AtomicReference<ActivityLifecycleMonitor> lifecycleMonitor =
      new AtomicReference<ActivityLifecycleMonitor>(null);

  // singleton - disallow creation
  private ActivityLifecycleMonitorRegistry() {}

  /**
   * Returns the ActivityLifecycleMonitor.
   *
   * <p>This monitor is not guaranteed to be present under all instrumentations.
   *
   * @return ActivityLifecycleMonitor the monitor for this application.
   * @throws IllegalStateException if no monitor has been registered.
   */
  public static ActivityLifecycleMonitor getInstance() {
    ActivityLifecycleMonitor instance = lifecycleMonitor.get();
    if (null == instance) {
      throw new IllegalStateException(
          "No lifecycle monitor registered! Are you running "
              + "under an Instrumentation which registers lifecycle monitors?");
    }
    return instance;
  }

  /**
   * Stores a lifecycle monitor in the registry.
   *
   * <p>This is a global registry - so be aware of the impact of calling this method!
   *
   * @param monitor the monitor for this application. Null deregisters any existing monitor.
   */
  public static void registerInstance(ActivityLifecycleMonitor monitor) {
    lifecycleMonitor.set(monitor);
  }
}
