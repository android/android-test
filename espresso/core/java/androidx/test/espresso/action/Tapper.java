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

package androidx.test.espresso.action;

import androidx.test.espresso.UiController;

/** Interface to implement different click types. */
public interface Tapper {

  /** The result of the tap. */
  enum Status {
    /** The Tap action completed successfully. */
    SUCCESS,
    /**
     * The action seemed to have completed - but may have been misinterpreted by the application.
     * (For Example a TAP became a LONG PRESS by measuring its time between the down and up events).
     */
    WARNING,
    /** Injecting the event was a complete failure. */
    FAILURE
  }

  /**
   * Sends a MotionEvent to the given UiController.
   *
   * @param uiController a UiController to use to send MotionEvents to the screen.
   * @param coordinates a float[] with x and y values of center of the tap.
   * @param precision a float[] with x and y values of precision of the tap.
   * @param inputDevice the input device of the tap, ie. InputDevice.SOURCE_MOUSE.
   * @param buttonState the button the tap is received from, ie. BUTTON_PRIMARY, BUTTON_SECONDARY.
   * @return The status of the tap.
   */
  Status sendTap(
      UiController uiController,
      float[] coordinates,
      float[] precision,
      int inputDevice,
      int buttonState);
  /**
   * @deprecated Use @{link #sendTap(UiController, float[], float[], int, int)} instead This will
   *     call the @{link #sendTap(UiController, float[], float[], int, int)} with {@link
   *     android.view.InputDevice#SOURCE_UNKNOWN}, {@link android.view.MotionEvent#BUTTON_PRIMARY}
   *     by default.
   */
  @Deprecated
  Status sendTap(UiController uiController, float[] coordinates, float[] precision);
}
