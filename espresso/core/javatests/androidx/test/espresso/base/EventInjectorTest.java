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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link EventInjector}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EventInjectorTest {
  private EventInjector injector;

  @Before
  public void setUp() throws Exception {
    if (Build.VERSION.SDK_INT > 15) {
      InputManagerEventInjectionStrategy strat = new InputManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    } else {
      WindowManagerEventInjectionStrategy strat = new WindowManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    }
  }

  @Test
  public void injectKeyEventUpWithNoDown() throws Exception {
    try (ActivityScenario<SendActivity> scenario = ActivityScenario.launch(SendActivity.class)) {

      scenario.onActivity(
          sendActivity -> {
            View view = sendActivity.findViewById(R.id.send_data_edit_text);
            assertThat(view.requestFocus()).isTrue();
          });

      KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
      KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
      assertThat(injector.injectKeyEvent(events[1])).isTrue();
    }
  }

  @Test
  public void injectStaleKeyEvent() throws Exception {
    try (ActivityScenario<SendActivity> scenario = ActivityScenario.launch(SendActivity.class)) {

      scenario.onActivity(
          sendActivity -> {
            View view = sendActivity.findViewById(R.id.send_data_edit_text);
            assertThat(view.requestFocus()).isTrue();
          });

      KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
      KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
      KeyEvent event = KeyEvent.changeTimeRepeat(events[0], 1, 0);

      assertThat(injector.injectKeyEvent(event)).isFalse();
    }
  }

  @Test
  public void injectKeyEvent_securityException() {
    final AtomicBoolean injectEventWorked = new AtomicBoolean(false);
    final AtomicBoolean injectEventThrewSecurityException = new AtomicBoolean(false);
    KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
    KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());

    try {
      injectEventWorked.set(injector.injectKeyEvent(events[0]));
    } catch (InjectEventSecurityException expected) {
      injectEventThrewSecurityException.set(true);
    }

    // On API <= 32: injectKeyEvent throws an InjectEventSecurityException.
    // On API >= 33: injectKeyEvent works for instrumentation and injects the event.
    if (Build.VERSION.SDK_INT <= 32) {
      assertThat(injectEventThrewSecurityException.get()).isTrue();
    } else {
      assertThat(injectEventWorked.get()).isTrue();
      assertThat(injectEventThrewSecurityException.get()).isFalse();
    }
  }

  @Test
  public void injectMotionEvent_securityException() throws Exception {
    final AtomicBoolean injectEventWorked = new AtomicBoolean(false);
    final AtomicBoolean injectEventThrewSecurityException = new AtomicBoolean(false);
    getInstrumentation()
        .runOnMainSync(
            () -> {
              MotionEvent down =
                  MotionEvent.obtain(
                      SystemClock.uptimeMillis(),
                      SystemClock.uptimeMillis(),
                      MotionEvent.ACTION_DOWN,
                      0,
                      0,
                      0);
              try {
                injectEventWorked.set(injector.injectMotionEvent(down));
              } catch (InjectEventSecurityException expected) {
                injectEventThrewSecurityException.set(true);
              }
            });

    // On API <= 32: injectMotionEvent throws an InjectEventSecurityException.
    // On API >= 33: injectMotionEvent works for instrumentation and injects the event.
    if (Build.VERSION.SDK_INT <= 32) {
      assertThat(injectEventThrewSecurityException.get()).isTrue();
    } else {
      assertThat(injectEventWorked.get()).isTrue();
      assertThat(injectEventThrewSecurityException.get()).isFalse();
    }
  }
}
