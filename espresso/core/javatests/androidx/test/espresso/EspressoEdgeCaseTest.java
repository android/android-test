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

package androidx.test.espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Collection of some nasty edge cases. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoEdgeCaseTest {
  @Rule
  public ActivityScenarioRule<SendActivity> rule = new ActivityScenarioRule<>(SendActivity.class);

  private static final Callable<Void> NO_OP =
      new Callable<Void>() {
        @Override
        public Void call() {
          return null;
        }
      };

  private Handler mainHandler;
  private final OneShotResource oneShotResource = new OneShotResource();

  @Before
  public void setUp() throws Exception {
    mainHandler = new Handler(Looper.getMainLooper());
  }

  @After
  public void tearDown() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
    IdlingPolicies.setIdlingResourceTimeout(26, TimeUnit.SECONDS);
    oneShotResource.setIdle(true);
  }

  @Test
  public void recoveryFromExceptionOnMainThreadLoopMainThreadUntilIdle() throws Exception {
    final RuntimeException poison = new RuntimeException("oops");
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {

                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(
                      new Runnable() {
                        @Override
                        public void run() {
                          throw poison;
                        }
                      });
                  controller.loopMainThreadUntilIdle();
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re == poison) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("Hello World111"));
    onView(withId(R.id.enter_data_edit_text)).check(matches(withText("Hello World111")));
  }

  @Test
  public void recoveryFromExceptionOnMainThreadLoopMainThreadForAtLeast() throws Exception {
    final RuntimeException poison = new RuntimeException("oops");
    final FutureTask<Void> syncTask = new FutureTask<Void>(NO_OP);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {
                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(
                      new Runnable() {
                        @Override
                        public void run() {
                          throw poison;
                        }
                      });
                  // block test execution until loopMainThreadForAtLeast call
                  // would be satisified
                  mainHandler.postDelayed(syncTask, 2500);
                  controller.loopMainThreadForAtLeast(2000);
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re == poison) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    syncTask.get();

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("baz bar"));
    onView(withId(R.id.enter_data_edit_text)).check(matches(withText("baz bar")));
  }

  @Test
  public void recoveryFromTimeOutExceptionMaster() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.SECONDS);
    final FutureTask<Void> syncTask = new FutureTask<Void>(NO_OP);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {
                @Override
                public void perform(UiController controller, View view) {
                  mainHandler.post(
                      new Runnable() {
                        @Override
                        public void run() {
                          SystemClock.sleep(TimeUnit.SECONDS.toMillis(8));
                        }
                      });
                  // block test execution until loopMainThreadForAtLeast call
                  // would be satisified
                  mainHandler.postDelayed(syncTask, 2500);
                  controller.loopMainThreadForAtLeast(1000);
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    syncTask.get();

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("one two three"));
    onView(withId(R.id.enter_data_edit_text)).check(matches(withText("one two three")));
  }

  @Test
  public void recoveryFromTimeOutExceptionDynamic() {
    IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.SECONDS);

    Espresso.registerIdlingResources(oneShotResource);
    oneShotResource.setIdle(false);

    try {
      onView(withId(R.id.enter_data_edit_text)).perform(click());
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    oneShotResource.setIdle(true);

    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("Doh"));
    onView(withId(R.id.enter_data_edit_text)).check(matches(withText("Doh")));
  }

  @Test
  public void recoveryFromAsyncTaskTimeout() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.SECONDS);
    try {
      onView(withId(R.id.enter_data_edit_text))
          .perform(
              new TestAction() {
                @Override
                public void perform(UiController controller, View view) {
                  new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                      SystemClock.sleep(TimeUnit.SECONDS.toMillis(8));
                      return null;
                    }
                  }.execute();
                  // block test execution until loopMainThreadForAtLeast call
                  // would be satisified
                  controller.loopMainThreadForAtLeast(1000);
                }
              });
      fail("should throw");
    } catch (RuntimeException re) {
      if (re instanceof EspressoException) {
        // expected
      } else {
        // something else.
        throw re;
      }
    }
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
    // life should continue normally.
    onView(withId(R.id.enter_data_edit_text)).perform(typeText("Har Har"));
    onView(withId(R.id.enter_data_edit_text)).check(matches(withText("Har Har")));
  }


  private abstract static class TestAction implements ViewAction {
    @Override
    public String getDescription() {
      return "A random test action.";
    }

    @Override
    public Matcher<View> getConstraints() {
      return isAssignableFrom(View.class);
    }
  }

  private static class OneShotResource implements IdlingResource {
    private static AtomicInteger counter = new AtomicInteger(0);

    private final int instance;
    private volatile IdlingResource.ResourceCallback callback;
    private volatile boolean isIdle = true;

    private OneShotResource() {
      instance = counter.incrementAndGet();
    }

    @Override
    public String getName() {
      return "TestOneShotResource_" + instance;
    }

    public void setIdle(boolean idle) {
      isIdle = idle;
      if (isIdle && callback != null) {
        callback.onTransitionToIdle();
      }
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(IdlingResource.ResourceCallback callback) {
      this.callback = callback;
    }
  }
}
