package androidx.test.gradletests.eventto;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.test.core.app.ActivityScenario;
import androidx.test.eventto.Eventto;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.gradletests.eventto.fixtures.SimpleActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class EventtoTest {

  @Rule
  public ActivityScenarioRule<SimpleActivity> activityScenarioRule =
      new ActivityScenarioRule<>(SimpleActivity.class);

//   @Test
//   public void eventto_find() throws Throwable {
//     Eventto.onView(withText("Text View")).check(matches(isDisplayed()));
//   }

  @Test
  public void eventto_asyncDelayed() throws Throwable {
    Eventto.onView(withText("Delayed update")).check(matches(isDisplayed()));
  }

//
//    activityScenarioRule.getScenario().onActivity(new ActivityScenario.ActivityAction<SimpleActivity>() {
//        @Override
//        public void perform(SimpleActivity activity) {
//            assertTrue(activity.buttonClicked);
//        }
//    });
//
//      AtomicBoolean elapsedTimeReached = new AtomicBoolean(false);
//      new Handler(Looper.getMainLooper()).postDelayed(() -> elapsedTimeReached.set(true), 100);
//    new QueueIdler().idleUntil(() -> elapsedTimeReached.get());





  //
  // @Test
  // public void espresso_click() throws Throwable {
  //   onView(withText("Launch delayed")).perform(click());
  //
  //   // expect another activity launch
  //   onView(withText("Delayed Activity")).check(matches(isDisplayed()));
  // }

  // @Test
  // public void eventto_findFails() throws Throwable {
  //   onViewWithText("Text View not there").assertThat(isDisplayed());
  // }

  // @Test
  // public void espressoFind() {
  //   onView(withText("Text View")).check(matches(isDisplayed()));
  // }

}
