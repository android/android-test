/*
 * Copyright (C) 2016 The Android Open Source Project
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

import androidx.test.espresso.UiController;

/**
 * Similar to {@link UiController} but with one additional method that enables interrupting Espresso
 * tasks.
 *
 * <p>This interface used for Espresso internals and shouldn't be called by external users. It's
 * much safer to let all Espresso tasks to be successfully executed on the main thread.
 */
public interface InterruptableUiController extends UiController {
  /**
   * Interrupts all Espresso tasks scheduled to be executed on the main thread.
   *
   * <p>Note: This method is used for Espresso internals and shouldn't be called by external users.
   * It's much safer to let all Espresso tasks to be successfully executed on the main thread.
   */
  void interruptEspressoTasks();
}
