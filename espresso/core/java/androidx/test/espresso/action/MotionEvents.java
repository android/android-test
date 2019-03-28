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
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;

/** Facilitates sending of motion events to a {@link UiController}. */
public final class MotionEvents {

  private static final String TAG = MotionEvents.class.getSimpleName();

  @VisibleForTesting static final int MAX_CLICK_ATTEMPTS = 3;

  private MotionEvents() {
    // Shouldn't be instantiated
  }

  public static DownResultHolder sendDown(
      UiController uiController, float[] coordinates, float[] precision) {
    return sendDown(
        uiController,
        coordinates,
        precision,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY);
  }

  /** Obtains the {@code MotionEvent} of down. */
  public static MotionEvent obtainDownEvent(
      float[] coordinates, float[] precision, int inputDevice, int buttonState) {
    checkNotNull(coordinates);
    checkNotNull(precision);

    // Algorithm of sending click event adopted from android.test.TouchUtils.
    // When the click event was first initiated. Needs to be same for both down and up press
    // events.
    long downTime = SystemClock.uptimeMillis();
    // Down press.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return downPressGingerBread(downTime, coordinates, precision);
    } else {
      return downPressICS(downTime, coordinates, precision, inputDevice, buttonState);
    }
  }

  public static MotionEvent obtainDownEvent(float[] coordinates, float[] precision) {
    return obtainDownEvent(
        coordinates, precision, InputDevice.SOURCE_UNKNOWN, MotionEvent.BUTTON_PRIMARY);
  }

  public static DownResultHolder sendDown(
      UiController uiController,
      float[] coordinates,
      float[] precision,
      int inputDevice,
      int buttonState) {
    checkNotNull(uiController);
    checkNotNull(coordinates);
    checkNotNull(precision);

    for (int retry = 0; retry < MAX_CLICK_ATTEMPTS; retry++) {
      MotionEvent motionEvent = null;
      try {
        motionEvent = obtainDownEvent(coordinates, precision, inputDevice, buttonState);
        // The down event should be considered a tap if it is long enough to be detected
        // but short enough not to be a long-press. Assume that TapTimeout is set at least
        // twice the detection time for a tap (no need to sleep for the whole TapTimeout since
        // we aren't concerned about scrolling here).
        long downTime = motionEvent.getDownTime();
        long isTapAt = downTime + (ViewConfiguration.getTapTimeout() / 2);

        boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

        while (true) {
          long delayToBeTap = isTapAt - SystemClock.uptimeMillis();
          if (delayToBeTap <= 10) {
            break;
          }
          // Sleep only a fraction of the time, since there may be other events in the UI queue
          // that could cause us to start sleeping late, and then oversleep.
          uiController.loopMainThreadForAtLeast(delayToBeTap / 4);
        }

        boolean longPress = false;
        if (SystemClock.uptimeMillis() > (downTime + ViewConfiguration.getLongPressTimeout())) {
          longPress = true;
          Log.w(TAG, "Overslept and turned a tap into a long press");
        }

        if (!injectEventSucceeded) {
          motionEvent.recycle();
          motionEvent = null;
          continue;
        }

        return new DownResultHolder(motionEvent, longPress);
      } catch (InjectEventSecurityException e) {
        throw new PerformException.Builder()
            .withActionDescription("Send down motion event")
            .withViewDescription("unknown") // likely to be replaced by FailureHandler
            .withCause(e)
            .build();
      }
    }
    throw new PerformException.Builder()
        .withActionDescription(
            String.format(Locale.ROOT, "click (after %s attempts)", MAX_CLICK_ATTEMPTS))
        .withViewDescription("unknown") // likely to be replaced by FailureHandler
        .build();
  }

  public static boolean sendUp(UiController uiController, MotionEvent downEvent) {
    return sendUp(uiController, downEvent, new float[] {downEvent.getX(), downEvent.getY()});
  }

  public static MotionEvent obtainUpEvent(MotionEvent downEvent, float[] coordinates) {
    checkNotNull(downEvent);
    checkNotNull(coordinates);
    // Up press.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return upPressGingerBread(downEvent, coordinates);
    } else {
      return upPressICS(downEvent, coordinates);
    }
  }

  public static boolean sendUp(
      UiController uiController, MotionEvent downEvent, float[] coordinates) {
    checkNotNull(uiController);
    checkNotNull(downEvent);
    checkNotNull(coordinates);

    MotionEvent motionEvent = null;
    try {
      motionEvent = obtainUpEvent(downEvent, coordinates);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.e(
            TAG,
            String.format(
                Locale.ROOT,
                "Injection of up event failed (corresponding down event: %s)",
                downEvent));
        return false;
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
          .withActionDescription(
              String.format(
                  Locale.ROOT, "inject up event (corresponding down event: %s)", downEvent))
          .withViewDescription("unknown") // likely to be replaced by FailureHandler
          .withCause(e)
          .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }
    return true;
  }

  public static void sendCancel(UiController uiController, MotionEvent downEvent) {
    checkNotNull(uiController);
    checkNotNull(downEvent);

    MotionEvent motionEvent = null;
    try {
      // Up press.
      motionEvent =
          MotionEvent.obtain(
              downEvent.getDownTime(),
              SystemClock.uptimeMillis(),
              MotionEvent.ACTION_CANCEL,
              downEvent.getX(),
              downEvent.getY(),
              0);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.e(
            TAG,
            String.format(
                Locale.ROOT,
                "Injection of cancel event failed (corresponding down event: %s)",
                downEvent));
        return;
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
          .withActionDescription(
              String.format(
                  Locale.ROOT, "inject cancel event (corresponding down event: %s)", downEvent))
          .withViewDescription("unknown") // likely to be replaced by FailureHandler
          .withCause(e)
          .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }
  }

  public static MotionEvent obtainMovement(long downTime, float[] coordinates) {
    return MotionEvent.obtain(
        downTime,
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_MOVE,
        coordinates[0],
        coordinates[1],
        0);
  }

  public static MotionEvent obtainMovement(long downTime, long eventTime, float[] coordinates) {
    return MotionEvent.obtain(
        downTime, eventTime, MotionEvent.ACTION_MOVE, coordinates[0], coordinates[1], 0);
  }

  public static boolean sendMovement(
      UiController uiController, MotionEvent downEvent, float[] coordinates) {
    checkNotNull(uiController);
    checkNotNull(downEvent);
    checkNotNull(coordinates);

    MotionEvent motionEvent = null;
    try {
      motionEvent = obtainMovement(downEvent.getDownTime(), coordinates);
      boolean injectEventSucceeded = uiController.injectMotionEvent(motionEvent);

      if (!injectEventSucceeded) {
        Log.e(
            TAG,
            String.format(
                Locale.ROOT,
                "Injection of motion event failed (corresponding down event: %s)",
                downEvent));
        return false;
      }
    } catch (InjectEventSecurityException e) {
      throw new PerformException.Builder()
          .withActionDescription(
              String.format(
                  Locale.ROOT, "inject motion event (corresponding down event: %s)", downEvent))
          .withViewDescription("unknown") // likely to be replaced by FailureHandler
          .withCause(e)
          .build();
    } finally {
      if (null != motionEvent) {
        motionEvent.recycle();
        motionEvent = null;
      }
    }

    return true;
  }

  private static MotionEvent downPressGingerBread(
      long downTime, float[] coordinates, float[] precision) {
    return MotionEvent.obtain(
        downTime,
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_DOWN,
        coordinates[0],
        coordinates[1],
        0, // pressure
        1, // size
        0, // metaState
        precision[0], // xPrecision
        precision[1], // yPrecision
        0, // deviceId
        0); // edgeFlags
  }

  private static MotionEvent downPressICS(
      long downTime, float[] coordinates, float[] precision, int inputDevice, int buttonState) {
    MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};
    MotionEvent.PointerProperties[] pointerProperties = getPointerProperties(inputDevice);
    pointerCoords[0].clear();
    pointerCoords[0].x = coordinates[0];
    pointerCoords[0].y = coordinates[1];
    pointerCoords[0].pressure = 0;
    pointerCoords[0].size = 1;

    return MotionEvent.obtain(
        downTime,
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_DOWN,
        1, // pointerCount
        pointerProperties,
        pointerCoords,
        0, // metaState
        buttonState,
        precision[0],
        precision[1],
        0, // deviceId
        0, // edgeFlags
        inputDevice,
        0); // flags
  }

  private static MotionEvent upPressGingerBread(MotionEvent downEvent, float[] coordinates) {
    return MotionEvent.obtain(
        downEvent.getDownTime(),
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_UP,
        coordinates[0],
        coordinates[1],
        0);
  }

  private static MotionEvent upPressICS(MotionEvent downEvent, float[] coordinates) {
    MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};
    MotionEvent.PointerProperties[] pointerProperties = getPointerProperties(downEvent.getSource());
    pointerCoords[0].clear();
    pointerCoords[0].x = coordinates[0];
    pointerCoords[0].y = coordinates[1];
    pointerCoords[0].pressure = 0;
    pointerCoords[0].size = 1;

    return MotionEvent.obtain(
        downEvent.getDownTime(),
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_UP,
        1, // pointerCount
        pointerProperties,
        pointerCoords,
        0, // metaState
        downEvent.getButtonState(),
        downEvent.getXPrecision(),
        downEvent.getYPrecision(),
        0, // deviceId
        0, // edgeFlags
        downEvent.getSource(),
        0); // flags
  }

  private static MotionEvent.PointerProperties[] getPointerProperties(int inputDevice) {
    MotionEvent.PointerProperties[] pointerProperties = {new MotionEvent.PointerProperties()};
    pointerProperties[0].clear();
    pointerProperties[0].id = 0;
    switch (inputDevice) {
      case InputDevice.SOURCE_MOUSE:
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;
        break;
      case InputDevice.SOURCE_STYLUS:
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_STYLUS;
        break;
      case InputDevice.SOURCE_TOUCHSCREEN:
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
        break;
      default:
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_UNKNOWN;
        break;
    }
    return pointerProperties;
  }

  /** Holds the result of a down motion. */
  public static class DownResultHolder {
    public final MotionEvent down;
    public final boolean longPress;

    DownResultHolder(MotionEvent down, boolean longPress) {
      this.down = down;
      this.longPress = longPress;
    }
  }
}
