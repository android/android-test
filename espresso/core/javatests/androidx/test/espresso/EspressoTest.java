/*
 * Copyright (C) 2015 The Android Open Source Project
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.tracing.Tracing;
import androidx.test.ui.app.ActionBarTestActivity;
import androidx.test.ui.app.KeyboardTestActivity;
import androidx.test.ui.app.MainActivity;
import androidx.test.ui.app.R;
import androidx.test.ui.app.SendActivity;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests Espresso top level (i.e. ones not specific to a view) actions like pressBack and
 * closeSoftKeyboard.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoTest {
  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  private TestTracer tracer;

  @Before
  public void setUp() throws Exception {
    tracer = new TestTracer();
    Tracing.getInstance().registerTracer(tracer);
  }

  @After
  public void tearDown() throws Exception {
    Tracing.getInstance().unregisterTracer(tracer);
  }

  @Test
  public void openOverflowFromActionBar() {
    onData(allOf(instanceOf(Map.class), hasValue(ActionBarTestActivity.class.getSimpleName())))
        .perform(click());
    onView(withId(R.id.hide_contextual_action_bar)).perform(click());
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    onView(withText("World")).perform(click());
    onView(withId(R.id.text_action_bar_result)).check(matches(withText("World")));

    assertThat(
        tracer.getSpans(),
        contains(
            "beginSpan: Espresso-perform-AdapterDataLoaderAction",
            "+-endSpan: Espresso-perform-AdapterDataLoaderAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-openActionBarOverflowOrOptionsMenu",
            "beginSpan: Espresso-perform-KeyEventAction",
            "+-endSpan: Espresso-perform-KeyEventAction",
            "+-endSpan: Espresso-openActionBarOverflowOrOptionsMenu",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-check-MatchesViewAssertion",
            "+-endSpan: Espresso-check-MatchesViewAssertion"));
  }

  @Test
  public void openOverflowInActionMode() {
    onData(allOf(instanceOf(Map.class), hasValue(ActionBarTestActivity.class.getSimpleName())))
        .perform(click());
    openContextualActionModeOverflowMenu();
    onView(withText("Key")).perform(click());
    onView(withId(R.id.text_action_bar_result)).check(matches(withText("Key")));

    assertThat(
        tracer.getSpans(),
        contains(
            "beginSpan: Espresso-perform-AdapterDataLoaderAction",
            "+-endSpan: Espresso-perform-AdapterDataLoaderAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-openContextualActionModeOverflowMenu",
            "beginSpan: Espresso-perform-TransitionBridgingViewAction",
            "+-endSpan: Espresso-perform-TransitionBridgingViewAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-openContextualActionModeOverflowMenu",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-check-MatchesViewAssertion",
            "+-endSpan: Espresso-check-MatchesViewAssertion"));
  }

  @Test
  public void closeSoftKeyboard() {
    onData(allOf(instanceOf(Map.class), hasValue(SendActivity.class.getSimpleName())))
        .perform(click());

    onView(withId(R.id.enter_data_edit_text))
        .perform(
            new ViewAction() {
              @Override
              public Matcher<View> getConstraints() {
                return any(View.class);
              }

              @Override
              public void perform(UiController uiController, View view) {
                // This doesn't do anything if hardware keyboard is present - that is, soft keyboard
                // is _not_ present. Whether it's present or not can be verified under the following
                // device settings: Settings > Language & Input > Under Keyboard and input method
                InputMethodManager imm =
                    (InputMethodManager)
                        getInstrumentation()
                            .getTargetContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, 0);
                uiController.loopMainThreadUntilIdle();
              }

              @Override
              public String getDescription() {
                return "show soft input";
              }
            });

    onView(withId(R.id.enter_data_edit_text)).perform(ViewActions.closeSoftKeyboard());

    assertThat(
        tracer.getSpans(),
        contains(
            "beginSpan: Espresso-perform-AdapterDataLoaderAction",
            "+-endSpan: Espresso-perform-AdapterDataLoaderAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-perform-show soft input",
            "+-endSpan: Espresso-perform-show soft input",
            "beginSpan: Espresso-perform-CloseKeyboardAction",
            "+-endSpan: Espresso-perform-CloseKeyboardAction"));
  }

  /**
   * for this test to be useful, hardware keyboard must be disabled. Thus, soft keyboard must be
   * present.
   */
  @Test
  public void closeSoftKeyboardRetry() {
    onData(allOf(instanceOf(Map.class), hasValue(KeyboardTestActivity.class.getSimpleName())))
        .perform(click());
    // click on the edit text which bring the soft keyboard up
    onView(withId(R.id.editTextUserInput))
        .perform(typeText("Espresso"), ViewActions.closeSoftKeyboard());
    // the soft keyboard should be dismissed to expose the button
    onView(withId(R.id.changeTextBt)).perform(click());

    // repeat, to make sure the retry mechanism still works for a subsequent ViewAction.
    onView(withId(R.id.editTextUserInput))
        .perform(typeText(", just works!"), ViewActions.closeSoftKeyboard());
    onView(withId(R.id.changeTextBt)).perform(click());

    assertThat(
        tracer.getSpans(),
        contains(
            "beginSpan: Espresso-perform-AdapterDataLoaderAction",
            "+-endSpan: Espresso-perform-AdapterDataLoaderAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-perform-TypeTextAction",
            "+-endSpan: Espresso-perform-TypeTextAction",
            "beginSpan: Espresso-perform-CloseKeyboardAction",
            "+-endSpan: Espresso-perform-CloseKeyboardAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction",
            "beginSpan: Espresso-perform-TypeTextAction",
            "+-endSpan: Espresso-perform-TypeTextAction",
            "beginSpan: Espresso-perform-CloseKeyboardAction",
            "+-endSpan: Espresso-perform-CloseKeyboardAction",
            "beginSpan: Espresso-perform-GeneralClickAction",
            "+-endSpan: Espresso-perform-GeneralClickAction"));
  }

  @Test
  public void setFailureHandler() {
    final AtomicBoolean handled = new AtomicBoolean(false);
    Espresso.setFailureHandler(
        new FailureHandler() {
          @Override
          public void handle(Throwable error, Matcher<View> viewMatcher) {
            handled.set(true);
          }
        });
    onView(withText("does not exist")).perform(click());
    assertTrue(handled.get());
  }

  @Test
  public void registerResourceWithNullName() {
    DummyIdlingResource resource = new DummyIdlingResource(null);
    try {
      Espresso.registerIdlingResources(resource);
      // IRs are taken into account only on the next Espresso interaction, thus preform the
      // following dummy check (could be anything)
      onView(isRoot()).check(ViewAssertions.matches(isDisplayed()));
    } catch (RuntimeException expected) {
      // expected
    } finally {
      // cleanup
      Espresso.unregisterIdlingResources(resource);
    }
  }

  @Test
  public void getIdlingResources() {
    int originalCount = Espresso.getIdlingResources().size();

    IdlingResource resource = new DummyIdlingResource("testGetIdlingResources");

    Espresso.registerIdlingResources(resource);
    assertEquals(originalCount + 1, Espresso.getIdlingResources().size());

    Espresso.unregisterIdlingResources(resource);
    assertEquals(originalCount, Espresso.getIdlingResources().size());
  }

  @Test
  public void registerIdlingResources() {
    IdlingResource resource = new DummyIdlingResource("testRegisterIdlingResources");
    assertTrue(Espresso.registerIdlingResources(resource));
    assertFalse(Espresso.registerIdlingResources(resource));
  }

  @Test
  public void unregisterIdlingResources() {
    IdlingResource resource = new DummyIdlingResource("testUnregisterIdlingResources");
    Espresso.registerIdlingResources(resource);
    assertTrue(Espresso.unregisterIdlingResources(resource));
    assertFalse(Espresso.unregisterIdlingResources(resource));
  }

  @Test
  public void emptyArrayOfResources() {
    assertTrue(Espresso.registerIdlingResources());
    assertTrue(Espresso.unregisterIdlingResources());
  }

  @Test
  public void onView_thrownFromScenarioOnActivity() {
    rule.getScenario()
        .onActivity(
            activity -> {
              IllegalStateException thrown =
                  assertThrows(
                      IllegalStateException.class,
                      () ->
                          onView(withId(android.R.id.list))
                              .check(matches(withText("ActionBarTestActivity")))
                              .perform(click()));

              assertThat(
                  thrown.getMessage(),
                  containsString("Method cannot be called on the main application thread"));
            });

    assertThat(tracer.getSpans(), empty());
  }

  @Test
  public void onData_throwsFromScenarioOnActivity() {
    rule.getScenario()
        .onActivity(
            activity -> {
              IllegalStateException thrown =
                  assertThrows(
                      IllegalStateException.class,
                      () ->
                          onData(
                                  allOf(
                                      instanceOf(Map.class),
                                      hasValue(ActionBarTestActivity.class.getSimpleName())))
                              .perform(click()));

              assertThat(
                  thrown.getMessage(),
                  containsString("Method cannot be called on the main application thread"));
            });

    assertThat(tracer.getSpans(), empty());
  }

  @Test
  public void onIdle_throwsFromScenarioOnActivity() {
    rule.getScenario()
        .onActivity(
            activity -> {
              IllegalStateException thrown =
                  assertThrows(IllegalStateException.class, () -> onIdle());

              assertThat(
                  thrown.getMessage(),
                  containsString("Method cannot be called on the main application thread"));
            });

    assertThat(
        tracer.getSpans(), contains("beginSpan: Espresso-onIdle", "+-endSpan: Espresso-onIdle"));
  }

  private static class DummyIdlingResource implements IdlingResource {
    private String name;

    public DummyIdlingResource(String name) {
      this.name = name;
    }

    @Override
    public boolean isIdleNow() {
      return true;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      // ignore
    }
  }
}
