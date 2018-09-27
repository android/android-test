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

import android.view.MotionEvent;
import androidx.test.espresso.UiController;

/** Interface to implement different swipe types. */
public interface Swiper {

  /** The result of the swipe. */
  public enum Status {
    /** The swipe action completed successfully. */
    SUCCESS,
    /** Injecting the event was a complete failure. */
    FAILURE
  }

  /**
   * Swipes from {@code startCoordinates} to {@code endCoordinates} using the given {@code
   * uiController} to send {@link MotionEvent}s.
   *
   * @param uiController a UiController to use to send MotionEvents to the screen.
   * @param startCoordinates a float[] with x and y co-ordinates of the start of the swipe.
   * @param endCoordinates a float[] with x and y co-ordinates of the end of the swipe.
   * @param precision a float[] with x and y values of precision of the tap.
   * @return The status of the swipe.
   */
  public Status sendSwipe(
      UiController uiController,
      float[] startCoordinates,
      float[] endCoordinates,
      float[] precision);
}
