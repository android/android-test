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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Build;
import android.os.Looper;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.action.MotionEvents;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.tracing.Tracing;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link UiControllerImpl}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class UiControllerImplIntegrationTest {

  private UiController uiController;

  @Before
  public void setUp() throws Exception {
    EventInjector injector = new EventInjector(new InputManagerEventInjectionStrategy());

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
                return new IdlingResourceRegistry(Looper.getMainLooper(), Tracing.getInstance())
                    .asIdleNotifier();
              }
            },
            Looper.getMainLooper(),
            new IdlingResourceRegistry(Looper.getMainLooper(), Tracing.getInstance()));
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
              assertThat(uiController.injectKeyEvent(events[0])).isTrue();
            } catch (InjectEventSecurityException e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  @Test
  public void testInjectString() throws InterruptedException, IOException {
    // use TypeTextAction as proxy to test injectString, since it contains the necessary
    // focus handling to make injectString reliable
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      onView(withId(R.id.send_data_to_call_edit_text)).perform(typeText("Hello! \n&*$$$"));

      activityScenario.onActivity(
          activity -> {
            EditText editText = activity.findViewById(R.id.send_data_to_call_edit_text);
            assertThat(editText.getText().toString()).isEqualTo("Hello! \n&*$$$");
          });
    }
  }

  @Test
  public void injectLargeString() throws InterruptedException {
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      onView(withId(R.id.send_data_to_call_edit_text))
          .perform(typeText("This is a string with 32 chars!!"));

      activityScenario.onActivity(
          activity -> {
            EditText editText = activity.findViewById(R.id.send_data_to_call_edit_text);
            assertThat(editText.getText().toString()).isEqualTo("This is a string with 32 chars!!");
          });
    }
  }

  @Test
  public void testInjectEmptyString() throws InterruptedException {
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {
      onView(withId(R.id.send_data_to_call_edit_text)).perform(typeText(""));

      activityScenario.onActivity(
          activity -> {
            EditText editText = activity.findViewById(R.id.send_data_to_call_edit_text);
            assertThat(editText.getText().toString()).isEmpty();
          });
    }
  }

  @Test
  public void injectStringSecurityException() throws InterruptedException {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              try {
                uiController.injectString("Hello! \n&*$$$");
              } catch (InjectEventSecurityException e) {
                // expected on API <= 32
                return;
              }
              // On API <= 32: injectString throws an InjectEventSecurityException.
              // On API >= 33: injectString works for instrumentation and injects the event.
              if (Build.VERSION.SDK_INT <= 32) {
                fail("InjectEventSecurityException not thrown");
              }
            });
  }

  @Test
  public void injectMotionEvent() throws InterruptedException, IOException {
    try (ActivityScenario<SendActivity> activityScenario =
        ActivityScenario.launch(SendActivity.class)) {

      final int[] intCoords = CoordinatesUtil.getCoordinatesInMiddleOfSendButton(activityScenario);
      final float[] coords = new float[] {intCoords[0], intCoords[1]};
      final float[] precision = new float[] {1f, 1f};
      getInstrumentation()
          .runOnMainSync(
              () -> {
                uiController.loopMainThreadForAtLeast(100);

                final MotionEvent downEvent = MotionEvents.obtainDownEvent(coords, precision);
                final MotionEvent upEvent = MotionEvents.obtainUpEvent(downEvent, coords);
                try {
                  assertThat(uiController.injectMotionEvent(downEvent)).isTrue();
                  assertThat(uiController.injectMotionEvent(upEvent)).isTrue();
                } catch (InjectEventSecurityException e) {
                  throw new RuntimeException(e);
                } finally {
                  downEvent.recycle();
                  upEvent.recycle();
                }
              });
    }
  }

  @Test
  @Ignore // flaky, uiController.injectMotionEventSequence can intermittently return false
  public void injectMotionEventSequence() throws InterruptedException {
    try (ActivityScenario<SendActivity> scenario = ActivityScenario.launch(SendActivity.class)) {

      final float[][] steps = CoordinatesUtil.getCoordinatesToDrag();

      getInstrumentation()
          .runOnMainSync(
              () -> {
                uiController.loopMainThreadForAtLeast(100);
                List<MotionEvent> events = new ArrayList<>();
                try {
                  MotionEvent down = MotionEvents.obtainDownEvent(steps[0], new float[] {16f, 16f});
                  events.add(down);
                  for (int i = 1; i < events.size() - 1; i++) {
                    events.add(MotionEvents.obtainMovement(down, steps[i]));
                  }
                  events.add(MotionEvents.obtainUpEvent(down, steps[steps.length - 1]));

                  assertThat(uiController.injectMotionEventSequence(events)).isTrue();
                } catch (InjectEventSecurityException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }
}
