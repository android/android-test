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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.test.espresso.UiController;
import androidx.test.espresso.action.MotionEvents.DownResultHolder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Executes different click types to given position. */
public enum Tap implements Tapper {
  SINGLE {
    @Override
    public Tapper.Status sendTap(
        UiController uiController, float[] coordinates, float[] precision) {
      return sendTap(uiController, coordinates, precision, 0, 0);
    }

    @Override
    public Tapper.Status sendTap(
        UiController uiController,
        float[] coordinates,
        float[] precision,
        int inputDevice,
        int buttonState) {
      Tapper.Status stat =
          sendSingleTap(uiController, coordinates, precision, inputDevice, buttonState);
      if (Tapper.Status.SUCCESS == stat) {
        // Wait until the touch event was processed by the main thread.
        long singlePressTimeout = (long) (ViewConfiguration.getTapTimeout() * 1.5f);
        uiController.loopMainThreadForAtLeast(singlePressTimeout);
      }
      return stat;
    }
  },
  LONG {
    @Override
    public Tapper.Status sendTap(
        UiController uiController, float[] coordinates, float[] precision) {
      return sendTap(uiController, coordinates, precision, 0, 0);
    }

    @Override
    public Tapper.Status sendTap(
        UiController uiController,
        float[] coordinates,
        float[] precision,
        int inputDevice,
        int buttonState) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);

      MotionEvent downEvent =
          MotionEvents.sendDown(uiController, coordinates, precision, inputDevice, buttonState)
              .down;
      try {
        // Duration before a press turns into a long press.
        // Factor 1.5 is needed, otherwise a long press is not safely detected.
        // See android.test.TouchUtils longClickView
        long longPressTimeout = (long) (ViewConfiguration.getLongPressTimeout() * 1.5f);
        uiController.loopMainThreadForAtLeast(longPressTimeout);

        if (!MotionEvents.sendUp(uiController, downEvent)) {
          MotionEvents.sendCancel(uiController, downEvent);
          return Tapper.Status.FAILURE;
        }
      } finally {
        downEvent.recycle();
        downEvent = null;
      }
      return Tapper.Status.SUCCESS;
    }
  },
  DOUBLE {
    @Override
    public Tapper.Status sendTap(
        UiController uiController, float[] coordinates, float[] precision) {
      return sendTap(uiController, coordinates, precision, 0, 0);
    }

    @Override
    public Tapper.Status sendTap(
        UiController uiController,
        float[] coordinates,
        float[] precision,
        int inputDevice,
        int buttonState) {
      checkNotNull(uiController);
      checkNotNull(coordinates);
      checkNotNull(precision);
      Tapper.Status stat =
          sendSingleTap(uiController, coordinates, precision, inputDevice, buttonState);
      if (stat == Tapper.Status.FAILURE) {
        return Tapper.Status.FAILURE;
      }

      if (0 < DOUBLE_TAP_MIN_TIMEOUT) {
        uiController.loopMainThreadForAtLeast(DOUBLE_TAP_MIN_TIMEOUT);
      }

      Tapper.Status secondStat =
          sendSingleTap(uiController, coordinates, precision, inputDevice, buttonState);

      if (secondStat == Tapper.Status.FAILURE) {
        return Tapper.Status.FAILURE;
      }

      if (secondStat == Tapper.Status.WARNING || stat == Tapper.Status.WARNING) {
        return Tapper.Status.WARNING;
      } else {
        return Tapper.Status.SUCCESS;
      }
    }
  };

  private static final String TAG = Tap.class.getSimpleName();
  private static final int DOUBLE_TAP_MIN_TIMEOUT;

  static {
    int timeVal = 0;
    if (Build.VERSION.SDK_INT > 18) {
      try {
        Method getDoubleTapMinTimeMethod =
            ViewConfiguration.class.getDeclaredMethod("getDoubleTapMinTime");
        timeVal = (Integer) getDoubleTapMinTimeMethod.invoke(null);
      } catch (NoSuchMethodException nsme) {
        Log.w(TAG, "Expected to find getDoubleTapMinTime", nsme);
      } catch (InvocationTargetException ite) {
        Log.w(TAG, "Unable to query double tap min time!", ite);
      } catch (IllegalAccessException iae) {
        Log.w(TAG, "Unable to query double tap min time!", iae);
      }
    }
    DOUBLE_TAP_MIN_TIMEOUT = timeVal;
  }

  private static Tapper.Status sendSingleTap(
      UiController uiController,
      float[] coordinates,
      float[] precision,
      int inputDevice,
      int buttonState) {
    checkNotNull(uiController);
    checkNotNull(coordinates);
    checkNotNull(precision);
    DownResultHolder res =
        MotionEvents.sendDown(uiController, coordinates, precision, inputDevice, buttonState);
    try {
      if (!MotionEvents.sendUp(uiController, res.down)) {
        Log.d(TAG, "Injection of up event as part of the click failed. Send cancel event.");
        MotionEvents.sendCancel(uiController, res.down);
        return Tapper.Status.FAILURE;
      }
    } finally {
      res.down.recycle();
    }
    return res.longPress ? Tapper.Status.WARNING : Tapper.Status.SUCCESS;
  }
}
