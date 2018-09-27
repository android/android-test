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

package androidx.test.espresso.base;

import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.espresso.InjectEventSecurityException;

/**
 * Injects Events into the application under test. Implementors should expect to be called from the
 * UI thread and are responsible for ensuring the event gets delivered or indicating that it could
 * not be delivered.
 */
interface EventInjectionStrategy {
  /**
   * Injects the given {@link KeyEvent} into the android system.
   *
   * @param keyEvent The event to inject
   * @return {@code true} if the input was inject successfully, {@code false} otherwise.
   * @throws InjectEventSecurityException if the MotionEvent would be delivered to an area of the
   *     screen that is not owned by the application under test.
   */
  boolean injectKeyEvent(KeyEvent keyEvent) throws InjectEventSecurityException;

  /**
   * Injects the given {@link MotionEvent} into the android system.
   *
   * @param me The event to inject
   * @param sync Inject the event in synchronized mode if true
   * @return {@code true} if the input was inject successfully, {@code false} otherwise.
   * @throws InjectEventSecurityException if the MotionEvent would be delivered to an area of the
   *     screen that is not owned by the application under test.
   */
  boolean injectMotionEvent(MotionEvent me, boolean sync) throws InjectEventSecurityException;
}
