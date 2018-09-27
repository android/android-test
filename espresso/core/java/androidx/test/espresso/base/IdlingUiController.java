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

import androidx.test.espresso.UiController;

/**
 * Similar to {@link UiController} but with one additional method that enables getting an instance
 * of {@link IdlingResourceRegistry}. For instance, the registry can then be used by {@link
 * androidx.test.espresso.ViewAction} to register and unregister idling resources before and
 * after performing the actual action.
 *
 * <p>This interface used for Espresso internals and shouldn't be called by external users.
 */
public interface IdlingUiController extends UiController {

  /** Returns an instance of {@link IdlingResourceRegistry}. */
  IdlingResourceRegistry getIdlingResourceRegistry();
}
