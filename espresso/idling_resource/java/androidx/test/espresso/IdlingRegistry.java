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

package androidx.test.espresso;

import static java.util.Collections.synchronizedSet;

import android.os.Looper;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles registering and unregistering of {@link IdlingResource}s with Espresso from within your
 * application code.
 *
 * <p>These resources are required by Espresso to provide synchronisation against your application
 * code. All registered resources with this registry will be automatically synchronized against for
 * each Espresso interaction.
 *
 * <p>This registry along with {@link IdlingResource} interface are bundled together in a small
 * light weight module so that it can be pulled in as a dependency of the App under test with close
 * to no overhead.
 */
public final class IdlingRegistry {

  private static final IdlingRegistry instance = new IdlingRegistry();
  private final Set<IdlingResource> resources = synchronizedSet(new HashSet<IdlingResource>());
  private final Set<Looper> loopers = synchronizedSet(new HashSet<Looper>());

  // VisibleForTesting
  IdlingRegistry() {}

  /**
   * Returns a singleton instance of this {@link IdlingRegistry} that should be globally used for
   * registering and unregistering {@link IdlingResource}s
   */
  public static IdlingRegistry getInstance() {
    return instance;
  }

  /**
   * Registers one or more {@link IdlingResource}s. When registering more than one resource, ensure
   * that each has a unique name returned from {@link IdlingResource#getName()}
   *
   * @return {@code true} if at least one resource was successfully added to the registry
   */
  public boolean register(IdlingResource... idlingResources) {
    if (null == idlingResources) {
      throw new NullPointerException("idlingResources cannot be null!");
    }
    return resources.addAll(Arrays.asList(idlingResources));
  }

  /**
   * Unregisters one or more {@link IdlingResource}s.
   *
   * @return {@code true} if at least one resource was successfully removed from the registry
   */
  public boolean unregister(IdlingResource... idlingResources) {
    if (null == idlingResources) {
      throw new NullPointerException("idlingResources cannot be null!");
    }
    return resources.removeAll(Arrays.asList(idlingResources));
  }

  /**
   * Registers a {@link Looper} for idle checking with the framework. This is intended for use with
   * non-UI thread {@link Looper}s only.
   *
   * @throws IllegalArgumentException if looper is the main looper.
   * @throws NullPointerException if looper is null.
   */
  public void registerLooperAsIdlingResource(Looper looper) {
    if (null == looper) {
      throw new NullPointerException("looper cannot be null!");
    }
    if (Looper.getMainLooper() == looper) {
      throw new IllegalArgumentException("Not intended for use with main looper!");
    }

    loopers.add(looper);
  }

  /**
   * Unregisters a {@link Looper}.
   *
   * <p>Attempting to unregister a looper that is not registered is a no-op.
   *
   * @return {@code true} if the looper was successfully removed from the registry
   */
  public boolean unregisterLooperAsIdlingResource(Looper looper) {
    if (null == looper) {
      throw new NullPointerException("looper cannot be null!");
    }
    return loopers.remove(looper);
  }

  /** Returns a set of all currently registered {@link IdlingResource}s. */
  public Collection<IdlingResource> getResources() {
    return new HashSet<>(resources);
  }

  /** @return a set of all currently registered {@link Looper}s. */
  public Collection<Looper> getLoopers() {
    return new HashSet<>(loopers);
  }
}
