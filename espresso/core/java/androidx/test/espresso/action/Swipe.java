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

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.MotionEvent;
import androidx.test.espresso.UiController;
import java.util.ArrayList;
import java.util.List;

/** Executes different swipe types to given positions. */
public enum Swipe implements Swiper {

  /** Swipes quickly between the co-ordinates. */
  FAST {
    @Override
    public Swiper.Status sendSwipe(
        UiController uiController,
        float[] startCoordinates,
        float[] endCoordinates,
        float[] precision) {
      return sendLinearSwipe(
          uiController, startCoordinates, endCoordinates, precision, SWIPE_FAST_DURATION_MS);
    }
  },

  /** Swipes deliberately slowly between the co-ordinates, to aid in visual debugging. */
  SLOW {
    @Override
    public Swiper.Status sendSwipe(
        UiController uiController,
        float[] startCoordinates,
        float[] endCoordinates,
        float[] precision) {
      return sendLinearSwipe(
          uiController, startCoordinates, endCoordinates, precision, SWIPE_SLOW_DURATION_MS);
    }
  };

  private static final String TAG = Swipe.class.getSimpleName();

  /** The number of motion events to send for each swipe. */
  private static final int SWIPE_EVENT_COUNT = 10;

  /** Length of time a "fast" swipe should last for, in milliseconds. */
  private static final int SWIPE_FAST_DURATION_MS = 150;

  /** Length of time a "slow" swipe should last for, in milliseconds. */
  private static final int SWIPE_SLOW_DURATION_MS = 1500;

  private static float[][] interpolate(float[] start, float[] end, int steps) {
    checkElementIndex(1, start.length);
    checkElementIndex(1, end.length);

    float[][] res = new float[steps][2];

    for (int i = 1; i < steps + 1; i++) {
      res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f);
      res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f);
    }

    return res;
  }

  private static Swiper.Status sendLinearSwipe(
      UiController uiController,
      float[] startCoordinates,
      float[] endCoordinates,
      float[] precision,
      int duration) {
    checkNotNull(uiController);
    checkNotNull(startCoordinates);
    checkNotNull(endCoordinates);
    checkNotNull(precision);

    float[][] steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT);

    List<MotionEvent> events = new ArrayList<>();
    MotionEvent downEvent = MotionEvents.obtainDownEvent(startCoordinates, precision);
    events.add(downEvent);

    try {
      final long intervalMS = duration / steps.length;
      long eventTime = downEvent.getDownTime();
      for (float[] step : steps) {
        eventTime += intervalMS;
        events.add(MotionEvents.obtainMovement(downEvent.getDownTime(), eventTime, step));
      }
      eventTime += intervalMS;
      events.add(
          MotionEvent.obtain(
              downEvent.getDownTime(),
              eventTime,
              MotionEvent.ACTION_UP,
              endCoordinates[0],
              endCoordinates[1],
              0));
      uiController.injectMotionEventSequence(events);
    } catch (Exception e) {
      return Swiper.Status.FAILURE;
    } finally {
      for (MotionEvent event : events) {
        event.recycle();
      }
    }
    return Swiper.Status.SUCCESS;
  }
}
