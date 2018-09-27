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

package androidx.test.espresso.base;

/**
 * An internal interface UiControllerImpl uses to communicate with sources of idleness.
 *
 * @param <CB> the callback type which receives notification of idleness.
 */
interface IdleNotifier<CB> {

  /** Indicates the resources backed by this notifier are idle now. */
  boolean isIdleNow();

  /** De-registers any currently registered callback. */
  void cancelCallback();

  /**
   * Registers a callback to be invoked when the resources backed by the notifier go idle.
   *
   * @param callback an object that the notifier will use to inform us about the idle state of the
   *     resources.
   */
  void registerNotificationCallback(CB callback);
}
