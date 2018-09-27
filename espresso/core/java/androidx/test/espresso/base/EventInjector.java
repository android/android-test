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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Build;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.espresso.InjectEventSecurityException;

/**
 * Responsible for selecting the proper strategy for injecting MotionEvents to the application under
 * test.
 */
final class EventInjector {
  private static final String TAG = EventInjector.class.getSimpleName();
  private final EventInjectionStrategy injectionStrategy;

  EventInjector(EventInjectionStrategy injectionStrategy) {
    this.injectionStrategy = checkNotNull(injectionStrategy);
  }

  boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    long downTime = event.getDownTime();
    long eventTime = event.getEventTime();
    int action = event.getAction();
    int code = event.getKeyCode();
    int repeatCount = event.getRepeatCount();
    int metaState = event.getMetaState();
    int deviceId = event.getDeviceId();
    int scancode = event.getScanCode();
    int flags = event.getFlags();

    if (eventTime == 0) {
      eventTime = SystemClock.uptimeMillis();
    }

    if (downTime == 0) {
      downTime = eventTime;
    }

    // API < 9 does not have constructor with source (nor has source field).
    KeyEvent newEvent;
    if (Build.VERSION.SDK_INT < 9) {
      newEvent =
          new KeyEvent(
              downTime,
              eventTime,
              action,
              code,
              repeatCount,
              metaState,
              deviceId,
              scancode,
              flags | KeyEvent.FLAG_FROM_SYSTEM);
    } else {
      int source = event.getSource();
      newEvent =
          new KeyEvent(
              downTime,
              eventTime,
              action,
              code,
              repeatCount,
              metaState,
              deviceId,
              scancode,
              flags | KeyEvent.FLAG_FROM_SYSTEM,
              source);
    }

    return injectionStrategy.injectKeyEvent(newEvent);
  }

  boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    return injectionStrategy.injectMotionEvent(event, true);
  }

  boolean injectMotionEventAsync(MotionEvent event) throws InjectEventSecurityException {
    return injectionStrategy.injectMotionEvent(event, false);
  }
}
