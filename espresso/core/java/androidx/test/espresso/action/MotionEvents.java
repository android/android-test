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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
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

    long downTime = SystemClock.uptimeMillis();
    return obtain(
        downTime,
        downTime,
        MotionEvent.ACTION_DOWN,
        coordinates,
        precision[0],
        precision[1],
        inputDevice,
        buttonState);
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
      MotionEvent motionEvent;
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

  /**
   * Create a new finger up motion event associated with the specified down motion event, at the
   * current time.
   *
   * @param downEvent the finger down motion event assoicated with this event.
   * @param coordinates The coordinates of the event
   */
  public static MotionEvent obtainUpEvent(MotionEvent downEvent, float[] coordinates) {
    return obtainUpEvent(downEvent, SystemClock.uptimeMillis(), coordinates);
  }

  /**
   * Create a new finger up motion event associated with the specified down motion event.
   *
   * @param downEvent the finger down motion event assoicated with this event.
   * @param eventTime The the time (in ms) when this specific event was generated.
   * @param coordinates The coordinates of the event
   */
  public static MotionEvent obtainUpEvent(
      MotionEvent downEvent, long eventTime, float[] coordinates) {
    checkNotNull(downEvent);
    checkNotNull(coordinates);
    return obtain(downEvent, eventTime, MotionEvent.ACTION_UP, coordinates);
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
      final float[] coordinates = new float[] {downEvent.getX(), downEvent.getY()};
      motionEvent =
          obtain(downEvent, SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, coordinates);
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
      }
    }
  }

  /**
   * Create a new move motion event associated with the specified down motion event, at the current
   * time.
   *
   * @param downEvent the finger down motion event assoicated with this event.
   * @param coordinates The coordinates of the event
   */
  @NonNull
  public static MotionEvent obtainMovement(
      @NonNull MotionEvent downEvent, @NonNull float[] coordinates) {
    return obtainMovement(downEvent, SystemClock.uptimeMillis(), coordinates);
  }

  /**
   * Create a new move motion event associated with the specified down motion event.
   *
   * @param downEvent the finger down motion event assoicated with this event.
   * @param eventTime The the time (in ms) when this specific event was generated.
   * @param coordinates The coordinates of the event
   */
  @NonNull
  public static MotionEvent obtainMovement(
      @NonNull MotionEvent downEvent, long eventTime, @NonNull float[] coordinates) {
    checkNotNull(downEvent);
    checkNotNull(coordinates);
    return obtain(downEvent, eventTime, MotionEvent.ACTION_MOVE, coordinates);
  }

  /**
   * @deprecated Use {@link #obtainMovement(MotionEvent, float[])} instead.
   */
  @Deprecated
  public static MotionEvent obtainMovement(long downTime, float[] coordinates) {
    return MotionEvent.obtain(
        downTime,
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_MOVE,
        coordinates[0],
        coordinates[1],
        0);
  }

  /**
   * @deprecated Use {@link #obtainMovement(MotionEvent, long, float[])} instead.
   */
  @Deprecated
  public static MotionEvent obtainMovement(long downTime, long eventTime, float[] coordinates) {
    return MotionEvent.obtain(
        downTime, eventTime, MotionEvent.ACTION_MOVE, coordinates[1], coordinates[1], 0);
  }

  public static boolean sendMovement(
      UiController uiController, MotionEvent downEvent, float[] coordinates) {
    checkNotNull(uiController);
    checkNotNull(downEvent);
    checkNotNull(coordinates);

    MotionEvent motionEvent = null;
    try {
      motionEvent = obtainMovement(downEvent, coordinates);
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
      }
    }

    return true;
  }

  private static MotionEvent obtain(
      MotionEvent downEvent, long eventTime, int action, float[] coordinates) {
    return obtain(
        downEvent.getDownTime(),
        eventTime,
        action,
        coordinates,
        downEvent.getXPrecision(),
        downEvent.getYPrecision(),
        downEvent.getSource(),
        downEvent.getToolType(0),
        downEvent.getButtonState());
  }

  private static MotionEvent obtain(
      long downTime,
      long eventTime,
      int action,
      float[] coordinates,
      float xPrecision,
      float yPrecision,
      int source,
      int buttonState) {
    return obtain(
        downTime,
        eventTime,
        action,
        coordinates,
        xPrecision,
        yPrecision,
        source,
        mapInputDeviceToToolType(source),
        buttonState);
  }

  private static MotionEvent obtain(
      long downTime,
      long eventTime,
      int action,
      float[] coordinates,
      float xPrecision,
      float yPrecision,
      int source,
      int toolType,
      int buttonState) {
    final MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};
    final MotionEvent.PointerProperties[] pointerProperties = getPointerProperties(toolType);
    pointerCoords[0].clear();
    pointerCoords[0].x = coordinates[0];
    pointerCoords[0].y = coordinates[1];
    pointerCoords[0].pressure = 0;
    pointerCoords[0].size = 1;

    return MotionEvent.obtain(
        downTime,
        eventTime,
        action,
        1, // pointerCount
        pointerProperties,
        pointerCoords,
        0, // metaState
        buttonState,
        xPrecision,
        yPrecision,
        0, // deviceId
        0, // edgeFlags
        source,
        0); // flags
  }

  private static MotionEvent.PointerProperties[] getPointerProperties(int toolType) {
    MotionEvent.PointerProperties[] pointerProperties = {new MotionEvent.PointerProperties()};
    pointerProperties[0].clear();
    pointerProperties[0].id = 0;
    pointerProperties[0].toolType = toolType;
    return pointerProperties;
  }

  private static int mapInputDeviceToToolType(int inputDevice) {
    int toolType;
    switch (inputDevice) {
      case InputDevice.SOURCE_MOUSE:
        toolType = MotionEvent.TOOL_TYPE_MOUSE;
        break;
      case InputDevice.SOURCE_STYLUS:
        toolType = MotionEvent.TOOL_TYPE_STYLUS;
        break;
      case InputDevice.SOURCE_TOUCHSCREEN:
        toolType = MotionEvent.TOOL_TYPE_FINGER;
        break;
      default:
        toolType = MotionEvent.TOOL_TYPE_UNKNOWN;
        break;
    }
    return toolType;
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
