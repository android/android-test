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

package androidx.test.espresso.base;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.internal.platform.reflect.ReflectionException;
import androidx.test.internal.platform.reflect.ReflectiveMethod;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.view.inspector.WindowInspectorCompat;
import androidx.test.platform.view.inspector.WindowInspectorCompat.ViewRetrievalException;
import java.util.List;

/**
 * An {@link EventInjectionStrategy} that uses the input manager to inject Events. This strategy
 * supports API level 23 and above.
 */
final class InputManagerEventInjectionStrategy implements EventInjectionStrategy {
  private static final String TAG = "EventInjectionStrategy";
  // The delay time to allow the soft keyboard to dismiss.
  private static final long KEYBOARD_DISMISSAL_DELAY_MILLIS = 1000L;

  // Used in reflection
  // TODO(b/404661556): use a public API method instead
  private final ReflectiveMethod<Boolean> injectInputEventMethod =
      new ReflectiveMethod<>(
          InputManager.class, "injectInputEvent", InputEvent.class, Integer.TYPE);

  // hardcoded copies of private InputManager fields.
  // historically these were obtained via reflection, but that seems
  // wasteful as these values have not changed since they were introduced
  // copy of private InputManager.INJECT_INPUT_EVENT_MODE_ASYNC.
  // This value has always been 0
  private static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;

  // Setting event mode to INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH to ensure
  // that we've dispatched the event and any side effects its had on the view hierarchy
  // have occurred.
  private static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

  InputManagerEventInjectionStrategy() {}

  @Override
  public boolean injectKeyEvent(KeyEvent keyEvent) throws InjectEventSecurityException {
    try {
      return injectInputEventMethod.invoke(
          getContext().getSystemService(InputManager.class),
          keyEvent,
          INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    } catch (ReflectionException e) {
      // annoyingly, ReflectiveMethod always rewraps the underlying exception
      Throwable cause = e.getCause().getCause();
      if (cause instanceof SecurityException) {
        throw new InjectEventSecurityException(cause);
      }
      throw new RuntimeException(cause);
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
  }

  @Override
  public boolean injectMotionEvent(MotionEvent motionEvent, boolean sync)
      throws InjectEventSecurityException {
    return innerInjectMotionEvent(motionEvent, true, sync);
  }

  private boolean innerInjectMotionEvent(MotionEvent motionEvent, boolean shouldRetry, boolean sync)
      throws InjectEventSecurityException {
    try {
      // Need to set the event source to touch screen, otherwise the input can be ignored even
      // though injecting it would be successful.
      // TODO: proper handling of events from a trackball (SOURCE_TRACKBALL) and joystick.
      if ((motionEvent.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0
          && !isFromTouchpadInGlassDevice(motionEvent)) {

        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
      }
      int eventMode =
          sync ? INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH : INJECT_INPUT_EVENT_MODE_ASYNC;
      return injectInputEventMethod.invoke(
          getContext().getSystemService(InputManager.class), motionEvent, eventMode);
    } catch (ReflectionException e) {
      Throwable cause = e.getCause().getCause();
      if (cause instanceof SecurityException) {
        if (shouldRetry) {
          Log.w(
              TAG,
              "Error performing a ViewAction! soft keyboard dismissal animation may have "
                  + "been in the way. Retrying once after: "
                  + KEYBOARD_DISMISSAL_DELAY_MILLIS
                  + " millis");
          SystemClock.sleep(KEYBOARD_DISMISSAL_DELAY_MILLIS);
          innerInjectMotionEvent(motionEvent, false, sync);
        } else {
          throw new InjectEventSecurityException(
              "Check if Espresso is clicking outside the app (system dialog, navigation bar if"
                  + " edge-to-edge is enabled, etc.).",
              cause);
        }
      } else {
        throw new RuntimeException(e.getCause());
      }
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
    return false;
  }

  // We'd like to inject non-pointer events sourced from touchpad in Glass.
  private static boolean isFromTouchpadInGlassDevice(MotionEvent motionEvent) {
    return (Build.DEVICE.contains("glass")
            || Build.DEVICE.contains("Glass")
            || Build.DEVICE.contains("wingman"))
        && ((motionEvent.getSource() & InputDevice.SOURCE_TOUCHPAD) != 0);
  }

  private static Context getContext() {
    try {
      return InstrumentationRegistry.getInstrumentation().getTargetContext();
    } catch (IllegalStateException e) {
      // Espresso is being used outside of instrumentation. Unusual, but prior art exists
      // Attempt to get context from global views
      try {
        List<View> views = WindowInspectorCompat.getGlobalWindowViews();
        if (views.isEmpty()) {
          throw new IllegalStateException(
              "Could not get Context. Not running under instrumentation and there is no UI"
                  + " present");
        }
        return views.get(0).getContext();
      } catch (ViewRetrievalException ve) {
        throw new IllegalStateException(ve);
      }
    }
  }
}
