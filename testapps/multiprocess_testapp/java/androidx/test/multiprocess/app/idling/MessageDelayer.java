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

import android.os.Handler;
import android.support.annotation.Nullable;
import androidx.test.espresso.IdlingResource;

/**
 * Takes a String and passes it back after a while via a callback.
 * <p>
 * This executes a long-running operation on a different thread that results in problems with
 * Espresso if an {@link IdlingResource} is not implemented and registered.
 */
public class MessageDelayer {

  private static final int DELAY_MILLIS = 3000;

  /**
   * Callback interface for clients to use.
   */
  public interface DelayerCallback {
    void onDone(String text);
  }

  /**
   * Takes a String and returns it after {@link #DELAY_MILLIS} via a {@link DelayerCallback}.
   *
   * @param message the string that will be returned via the callback
   * @param callback used to notify the caller asynchronously
   */
  public static void processMessage(
      final String message,
      final DelayerCallback callback,
      @Nullable final SimpleIdlingResource idlingResource) {
    // The IdlingResource is null in production.
    if (idlingResource != null) {
      idlingResource.setIdleState(false);
    }

    // Delay the execution, return message via callback.
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (callback != null) {
          callback.onDone(message);
          if (idlingResource != null) {
            idlingResource.setIdleState(true);
          }
        }
      }
    }, DELAY_MILLIS);
  }
}
