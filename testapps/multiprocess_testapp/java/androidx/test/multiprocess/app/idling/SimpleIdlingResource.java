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

package com.google.android.apps.common.testing.ui.multiprocess.testapp.idling;

import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingResource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A very simple implementation of {@link IdlingResource}.
 */
public class SimpleIdlingResource implements IdlingResource {

  @Nullable
  private volatile ResourceCallback callback;

  // Idleness is controlled with this boolean.
  private final AtomicBoolean isIdleNow = new AtomicBoolean(true);

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public boolean isIdleNow() {
    return isIdleNow.get();
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }

  /**
   * Sets the new idle state, if {@link #isIdleNow} is {@code true}, it pings the {@link
   * ResourceCallback} by calling {@link ResourceCallback#onTransitionToIdle}.
   *
   * @param isIdleNow false if there are pending operations, true if idle.
   */
  public void setIdleState(boolean isIdleNow) {
    this.isIdleNow.set(isIdleNow);
    if (isIdleNow && callback != null) {
      callback.onTransitionToIdle();
    }
  }
}
