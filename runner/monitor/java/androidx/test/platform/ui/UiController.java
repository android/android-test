/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.platform.ui;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Provides base-level UI operations (such as injection of {@link MotionEvent}s) that can be used to
 * build user actions such as clicks, scrolls, swipes, etc. This replaces parts of the android
 * Instrumentation class that provides similar functionality. However, it provides a more advanced
 * synchronization mechanism for test actions. The key differentiators are:
 *
 * <ul>
 *   <li>test actions are assumed to be called on the main thread
 *   <li>after a test action is initiated, execution blocks until all messages in the main message
 *       queue have been cleared.
 * </ul>
 */
public interface UiController {
  /**
   * Injects a motion event into the application.
   *
   * @param event the (non-null!) event to inject
   * @return true if the event was injected, false otherwise
   * @throws InjectEventSecurityException if the event couldn't be injected because it would
   *     interact with another application.
   */
  boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException;

  /**
   * Injects a key event into the application.
   *
   * @param event the (non-null!) event to inject
   * @return true if the event was injected, false otherwise
   * @throws InjectEventSecurityException if the event couldn't be injected because it would
   *     interact with another application.
   */
  boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException;

  /**
   * Types a string into the application using series of {@link KeyEvent}s. It is up to the
   * implementor to decide how to map the string to {@link KeyEvent} objects. If you need specific
   * control over the key events generated use {@link #injectKeyEvent(KeyEvent)}.
   *
   * @param str the (non-null!) string to type
   * @return true if the string was injected, false otherwise
   * @throws InjectEventSecurityException if the events couldn't be injected because it would
   *     interact with another application.
   */
  boolean injectString(String str) throws InjectEventSecurityException;

  /**
   * Loops the main thread until the application goes idle.
   *
   * <p>An empty task is immediately inserted into the task queue to ensure that if we're idle at
   * this moment we'll return instantly.
   */
  void loopMainThreadUntilIdle();

  /**
   * Loops the main thread for a specified period of time.
   *
   * <p>Control may not return immediately, instead it'll return after the provided delay has passed
   * and the queue is in an idle state again.
   *
   * @param millisDelay time to spend in looping the main thread
   */
  void loopMainThreadForAtLeast(long millisDelay);
}
