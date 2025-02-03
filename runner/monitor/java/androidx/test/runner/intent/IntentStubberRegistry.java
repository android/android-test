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

package androidx.test.runner.intent;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Exposes an implementation of {@link IntentStubber}. */
public final class IntentStubberRegistry {

  private static final AtomicReference<IntentStubber> instance = new AtomicReference<>();

  private static final AtomicBoolean isLoaded = new AtomicBoolean();

  /**
   * Loads an {@link IntentStubber} into this registry. There can only be one active stubber at a
   * time.
   *
   * <p>Calling this method multiple times in the same instrumentation will result in an exception.
   *
   * <p>This method can be called from any thread.
   */
  public static synchronized void load(IntentStubber intentStubber) {
    checkNotNull(intentStubber, "IntentStubber cannot be null!");
    checkState(
        !isLoaded.getAndSet(true),
        "Intent stubber already registered! Multiple stubbers are not"
            + "allowedAre you running under an ");
    instance.set(intentStubber);
  }

  /** Returns if an {@link IntentStubber} has been loaded. */
  public static boolean isLoaded() {
    return isLoaded.get();
  }

  /** Clears the current instance of Intent Stubber. */
  public static synchronized void reset() {
    instance.set(null);
    isLoaded.set(false);
  }

  /**
   * Returns the activity result for the given intent..
   *
   * <p>This method can be called from any thread.
   *
   * @throws IllegalStateException if no Intent Stubber has been loaded.
   */
  public static ActivityResult getActivityResultForIntent(Intent intent) {
    return getInstance().getActivityResultForIntent(intent);
  }

  /**
   * Returns the loaded Intent Stubber instance.
   *
   * <p>This method can be called from any thread.
   *
   * @throws IllegalStateException if no Intent Stubber has been loaded.
   */
  private static IntentStubber getInstance() {
    checkNotNull(
        instance,
        "No intent monitor registered! Are you running under an "
            + "Instrumentation which registers intent monitors?");
    return instance.get();
  }

  private IntentStubberRegistry() {}
}
