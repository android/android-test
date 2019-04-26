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
package androidx.test.internal.platform.os;

import android.view.View;

/**
 * An API for advancing the looper when its in a paused or controlled state.
 *
 * <p>Implementations should be loaded via {@link
 * androidx.test.internal.platform.ServiceLoaderWrapper}
 */
public interface ControlledLooper {

  /**
   * Executes all queued main looper tasks until its idle.
   *
   * <p>Intended to be used in unit test environments where main thread looper is in a paused state.
   */
  void drainMainThreadUntilIdle();

  /** Generate window focus event for given view. */
  void simulateWindowFocus(View decorView);

  public static final ControlledLooper NO_OP_CONTROLLED_LOOPER =
      new ControlledLooper() {
        @Override
        public void drainMainThreadUntilIdle() {}

        @Override
        public void simulateWindowFocus(View decorView) {}
      };
}
