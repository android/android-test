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

import static com.google.common.base.Preconditions.checkState;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.espresso.InjectEventSecurityException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An {@link EventInjectionStrategy} that uses the window manager to inject {@link MotionEvent}s.
 * This strategy supports API level 15 and below.
 */
final class WindowManagerEventInjectionStrategy implements EventInjectionStrategy {
  private static final String TAG = WindowManagerEventInjectionStrategy.class.getSimpleName();

  WindowManagerEventInjectionStrategy() {
    checkState(Build.VERSION.SDK_INT >= 7 && Build.VERSION.SDK_INT <= 15, "Unsupported API level.");
  }

  // Reflection members.
  private boolean initComplete;
  private Object wmInstance;
  private Method injectInputKeyEventMethod;
  private Method injectInputMotionEventMethod;

  void initialize() {
    if (initComplete) {
      return;
    }

    try {
      Log.d(TAG, "Trying to create injection strategy.");

      Class<?> serviceManagerClassObj = Class.forName("android.os.ServiceManager");
      Method windowServiceMethod =
          serviceManagerClassObj.getDeclaredMethod("getService", String.class);
      windowServiceMethod.setAccessible(true);

      Object windowServiceBinderObj = windowServiceMethod.invoke(serviceManagerClassObj, "window");

      Class<?> windowManagerStubObject = Class.forName("android.view.IWindowManager$Stub");
      Method asInterfaceMethod =
          windowManagerStubObject.getDeclaredMethod("asInterface", IBinder.class);
      asInterfaceMethod.setAccessible(true);

      wmInstance = asInterfaceMethod.invoke(windowManagerStubObject, windowServiceBinderObj);

      injectInputMotionEventMethod =
          wmInstance
              .getClass()
              .getDeclaredMethod("injectPointerEvent", MotionEvent.class, Boolean.TYPE);
      injectInputMotionEventMethod.setAccessible(true);

      injectInputKeyEventMethod =
          wmInstance.getClass().getDeclaredMethod("injectKeyEvent", KeyEvent.class, Boolean.TYPE);
      injectInputKeyEventMethod.setAccessible(true);

      initComplete = true;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw e;
    }
  }

  @Override
  public boolean injectKeyEvent(KeyEvent keyEvent) throws InjectEventSecurityException {
    try {
      // From javadoc of com.android.server.WindowManagerService.injectKeyEvent:
      // @param sync If true, wait for the event to be completed before returning to the caller.
      // @return true if event was dispatched, false if it was dropped for any reason
      //
      // Key events are delivered OFF the main thread, and we block until they are processed.
      return (Boolean) injectInputKeyEventMethod.invoke(wmInstance, keyEvent, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SecurityException) {
        throw new InjectEventSecurityException(cause);
      }
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
  }

  @Override
  public boolean injectMotionEvent(MotionEvent motionEvent, boolean sync)
      throws InjectEventSecurityException {
    try {
      // From javadoc of com.android.server.WindowManagerService.injectKeyEvent:
      // @param sync If true, wait for the event to be completed before returning to the caller.
      // @return true if event was dispatched, false if it was dropped for any reason
      //
      // We inject the pointer with sync=true to ensure the event is dispatched before control
      // is returned to our code.

      return (Boolean) injectInputMotionEventMethod.invoke(wmInstance, motionEvent, sync);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SecurityException) {
        throw new InjectEventSecurityException(cause);
      }
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new InjectEventSecurityException(e);
    }
  }
}
