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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.action.MotionEvents;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link UiControllerImpl}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class UiControllerImplIntegrationTest {
  //
  // @Rule
  // public ActivityTestRule<SendActivity> rule =
  //     new ActivityTestRule<SendActivity>(SendActivity.class, true, false) {
  //       @Override
  //       public SendActivity getActivity() {
  //         SendActivity a = ActivityScenario.launch(SendActivity.class);
  //         while (!a.hasWindowFocus()) {
  //           getInstrumentation().waitForIdleSync();
  //         }
  //         return a;
  //       }
  //     };

  private final AtomicBoolean injectEventWorked = new AtomicBoolean(false);
  private final AtomicBoolean injectEventThrewSecurityException = new AtomicBoolean(false);
  private final CountDownLatch focusLatch = new CountDownLatch(1);
  private final CountDownLatch latch = new CountDownLatch(1);
  private UiController uiController;

  @Before
  public void setUp() throws Exception {
    EventInjector injector = null;
    if (Build.VERSION.SDK_INT > 15) {
      InputManagerEventInjectionStrategy strat = new InputManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    } else {
      WindowManagerEventInjectionStrategy strat = new WindowManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    }
    uiController =
        new UiControllerImpl(
            injector,
            new AsyncTaskPoolMonitor(
                    new ThreadPoolExecutorExtractor(Looper.getMainLooper())
                        .getAsyncTaskThreadPool())
                .asIdleNotifier(),
            new NoopRunnableIdleNotifier(),
            new Provider<IdleNotifier<IdleNotificationCallback>>() {
              @Override
              public IdleNotifier<IdleNotificationCallback> get() {
                return new IdlingResourceRegistry(Looper.getMainLooper()).asIdleNotifier();
              }
            },
            Looper.getMainLooper(),
            new IdlingResourceRegistry(Looper.getMainLooper()));
  }

  @Test
  public void injectKeyEvent() throws InterruptedException {
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            try {
              KeyCharacterMap keyCharacterMap = UiControllerImpl.getKeyCharacterMap();
              KeyEvent[] events = keyCharacterMap.getEvents("a".toCharArray());
              injectEventWorked.set(uiController.injectKeyEvent(events[0]));
              latch.countDown();
            } catch (InjectEventSecurityException e) {
              injectEventThrewSecurityException.set(true);
            }
          });

      assertFalse("injectEvent threw a SecurityException", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(10, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }

  @Test
  public void testInjectString() throws InterruptedException {
    final AtomicBoolean requestFocusSucceded = new AtomicBoolean(false);

    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            final View view = activity.findViewById(R.id.send_data_to_call_edit_text);
            Log.i("TEST", HumanReadables.describe(view));
            requestFocusSucceded.set(view.requestFocus() && view.hasWindowFocus());
            Log.i("TEST-post", HumanReadables.describe(view));
            focusLatch.countDown();
          });

      assertTrue("requestFocus timed out!", focusLatch.await(2, TimeUnit.SECONDS));
      assertTrue("requestFocus failed.", requestFocusSucceded.get());

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    injectEventWorked.set(uiController.injectString("Hello! \n&*$$$"));
                    latch.countDown();
                  } catch (InjectEventSecurityException e) {
                    injectEventThrewSecurityException.set(true);
                  }
                }
              });

      assertFalse(
          "SecurityException exception was thrown.", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(20, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }

  @Test
  public void injectLargeString() throws InterruptedException {
    final AtomicBoolean requestFocusSucceded = new AtomicBoolean(false);

    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            final View view = activity.findViewById(R.id.send_data_to_call_edit_text);
            Log.i("TEST", HumanReadables.describe(view));
            requestFocusSucceded.set(view.requestFocus());
            Log.i("TEST-post", HumanReadables.describe(view));

            focusLatch.countDown();
          });

      assertTrue("requestFocus timed out!", focusLatch.await(2, TimeUnit.SECONDS));
      assertTrue("requestFocus failed.", requestFocusSucceded.get());

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    injectEventWorked.set(
                        uiController.injectString("This is a string with 32 chars!!"));
                    latch.countDown();
                  } catch (InjectEventSecurityException e) {
                    injectEventThrewSecurityException.set(true);
                  }
                }
              });

      assertFalse(
          "SecurityException exception was thrown.", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(20, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }

  @Test
  public void testInjectEmptyString() throws InterruptedException {
    final AtomicBoolean requestFocusSucceded = new AtomicBoolean(false);
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            final View view = activity.findViewById(R.id.send_data_to_call_edit_text);
            requestFocusSucceded.set(view.requestFocus());
            focusLatch.countDown();
          });

      assertTrue("requestFocus timed out!", focusLatch.await(2, TimeUnit.SECONDS));
      assertTrue("requestFocus failed.", requestFocusSucceded.get());

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    injectEventWorked.set(uiController.injectString(""));
                    latch.countDown();
                  } catch (InjectEventSecurityException e) {
                    injectEventThrewSecurityException.set(true);
                  }
                }
              });

      assertFalse(
          "SecurityException exception was thrown.", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(20, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }

  @Test
  public void injectStringSecurityException() throws InterruptedException {
    getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                try {
                  injectEventWorked.set(uiController.injectString("Hello! \n&*$$$"));
                  latch.countDown();
                } catch (InjectEventSecurityException e) {
                  injectEventThrewSecurityException.set(true);
                }
              }
            });

    assertTrue("SecurityException exception was thrown.", injectEventThrewSecurityException.get());
    assertFalse("Did NOT time out!", latch.await(3, TimeUnit.SECONDS));
    assertFalse(injectEventWorked.get());
  }

  @Test
  public void injectMotionEvent() throws InterruptedException {
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      final int[] coords = CoordinatesUtil.getCoordinatesInMiddleOfSendButton(activityScenario);

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  long downTime = SystemClock.uptimeMillis();
                  try {
                    MotionEvent event =
                        MotionEvent.obtain(
                            downTime,
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN,
                            coords[0],
                            coords[1],
                            0);

                    injectEventWorked.set(uiController.injectMotionEvent(event));
                    event.recycle();
                    latch.countDown();
                  } catch (InjectEventSecurityException e) {
                    injectEventThrewSecurityException.set(true);
                  }
                }
              });

      assertFalse(
          "SecurityException exception was thrown.", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(10, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }

  @Test
  public void injectMotionEventSequence() throws InterruptedException {
    try (ActivityScenario<SendActivity> scenario = ActivityScenario.launch(SendActivity.class)) {
      getInstrumentation().waitForIdleSync();
      final float[][] steps = CoordinatesUtil.getCoordinatesToDrag();

      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  long downTime = SystemClock.uptimeMillis();
                  List<MotionEvent> events = new ArrayList<>();
                  try {
                    MotionEvent down =
                        MotionEvents.obtainDownEvent(steps[0], new float[] {16f, 16f});
                    events.add(down);
                    for (int i = 1; i < events.size() - 1; i++) {
                      events.add(
                          MotionEvents.obtainMovement(
                              downTime, SystemClock.uptimeMillis(), steps[i]));
                    }
                    events.add(MotionEvents.obtainUpEvent(down, steps[steps.length - 1]));

                    injectEventWorked.set(uiController.injectMotionEventSequence(events));
                    latch.countDown();
                  } catch (InjectEventSecurityException e) {
                    injectEventThrewSecurityException.set(true);
                  }
                }
              });

      assertFalse(
          "SecurityException exception was thrown.", injectEventThrewSecurityException.get());
      assertTrue("Timed out!", latch.await(10, TimeUnit.SECONDS));
      assertTrue(injectEventWorked.get());
    }
  }
}
