/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso;

/**
 * Represents a resource of an application under test which can cause asynchronous background work
 * to happen during test execution (e.g. an intent service that processes a button click). By
 * default, {@link Espresso} synchronizes all view operations with the UI thread as well as
 * AsyncTasks; however, it has no way of doing so with "hand-made" resources. In such cases, test
 * authors can register the custom resource via {@link androidx.test.espresso.IdlingRegistry}
 * and {@link Espresso} will wait for the resource to become idle prior to executing a view
 * operation.
 *
 * <p><b>Important Note:</b> it is assumed that the resource stays idle most of the time.
 *
 * <p><b>Note:</b> before writing your implementation consider using {@link
 * androidx.test.espresso.idling.CountingIdlingResource} instead.
 */
public interface IdlingResource {

  /** Returns the name of the resources (used for logging and idempotency of registration). */
  public String getName();

  /**
   * Returns {@code true} if resource is currently idle. Espresso will <b>always</b> call this
   * method from the main thread, therefore it should be non-blocking and return immediately.
   */
  public boolean isIdleNow();

  /**
   * Registers the given {@link ResourceCallback} with the resource. Espresso will call this method:
   *
   * <ul>
   *   <li>with its implementation of {@link ResourceCallback} so it can be notified asynchronously
   *       that your resource is idle
   *   <li>from the main thread, but you are free to execute the callback's onTransitionToIdle from
   *       any thread
   *   <li>once (when it is initially given a reference to your IdlingResource)
   * </ul>
   *
   * <p>You only need to call this upon transition from busy to idle - if the resource is already
   * idle when the method is called invoking the call back is optional and has no significant
   * impact.
   */
  public void registerIdleTransitionCallback(ResourceCallback callback);

  /** Registered by an {@link IdlingResource} to notify Espresso of a transition to idle. */
  public interface ResourceCallback {
    /** Called when the resource goes from busy to idle. */
    public void onTransitionToIdle();
  }
}
