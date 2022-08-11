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
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;

import android.app.Activity;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Tests for {@link EventInjector}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EventInjectorTest {

  @Rule public ExpectedException expectedException = none();

  private static final String TAG = EventInjectorTest.class.getSimpleName();
  private EventInjector injector;
  final AtomicBoolean injectEventWorked = new AtomicBoolean(false);
  final AtomicBoolean injectEventThrewSecurityException = new AtomicBoolean(false);
  final CountDownLatch latch = new CountDownLatch(1);

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
            latch.countDown();
          });

      assertWithMessage("Timed out!").that(latch.await(10, SECONDS)).isTrue();
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
            latch.countDown();
          });

      assertWithMessage("Timed out!").that(latch.await(10, SECONDS)).isTrue();
      assertWithMessage("SecurityException exception was thrown.")
          .that(injectEventThrewSecurityException.get())
          .isFalse();

      KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
      KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
      KeyEvent event = KeyEvent.changeTimeRepeat(events[0], 1, 0);

      // Stale event does not fail for API < 13.
      assertThat(injector.injectKeyEvent(event)).isEqualTo(Build.VERSION.SDK_INT < 13);
    }
  }

  @Test
  public void injectKeyEvent_securityException() throws InjectEventSecurityException {
    KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
    KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
    // On API <= 32: injectKeyEvent throws an InjectEventSecurityException.
    // On API >= 33: injectKeyEvent works for instrumentation and injects the event.
    if (Build.VERSION.SDK_INT <= 32) {
      expectedException.expect(InjectEventSecurityException.class);
    }
    injector.injectKeyEvent(events[0]);
  }

  @Test
  public void injectMotionEvent_securityException() throws Exception {
    getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                MotionEvent down =
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN,
                        0,
                        0,
                        0);
                try {
                  injector.injectMotionEvent(down);
                  if (Build.VERSION.SDK_INT <= 32) {
                    fail("InjectEventSecurityException not thrown");
                  }
                } catch (InjectEventSecurityException expected) {
                  injectEventThrewSecurityException.set(true);
                }
                latch.countDown();
              }
            });

    latch.await(10, SECONDS);
    // On API <= 32: injectMotionEvent throws an InjectEventSecurityException.
    // On API >= 33: injectMotionEvent works for instrumentation and injects the event.
    assertThat(injectEventThrewSecurityException.get()).isEqualTo(Build.VERSION.SDK_INT <= 32);
  }

  @Test
  public void injectMotionEvent_upEventFailure() throws InterruptedException {
    final CountDownLatch activityStarted = new CountDownLatch(1);
    ActivityLifecycleCallback callback =
        new ActivityLifecycleCallback() {
          @Override
          public void onActivityLifecycleChanged(Activity activity, Stage stage) {
            if (Stage.RESUMED == stage && activity instanceof SendActivity) {
              activityStarted.countDown();
            }
          }
        };
    ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback);
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      assertThat(activityStarted.await(20, SECONDS)).isTrue();
      final int[] xy = CoordinatesUtil.getCoordinatesInMiddleOfSendButton(activityScenario);

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  MotionEvent up =
                      MotionEvent.obtain(
                          SystemClock.uptimeMillis(),
                          SystemClock.uptimeMillis(),
                          MotionEvent.ACTION_UP,
                          xy[0],
                          xy[1],
                          0);

                  try {
                    injectEventWorked.set(injector.injectMotionEvent(up));
                  } catch (InjectEventSecurityException e) {
                    Log.e(TAG, "injectEvent threw a SecurityException");
                  }
                  up.recycle();
                  latch.countDown();
                }
              });

      latch.await(10, SECONDS);
      // On API <= 32: injectMotionEvent throws an InjectEventSecurityException.
      // On API >= 33: injectMotionEvent works for instrumentation and injects the event.
      assertThat(injectEventWorked.get()).isEqualTo(Build.VERSION.SDK_INT >= 33);
    } finally {
      ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback);
    }
  }
}
