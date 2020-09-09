/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.ext.junit.rules;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ActivityScenario;

import org.junit.rules.ExternalResource;

import static androidx.test.internal.util.Checks.checkNotNull;

/**
 * ActivityScenarioRule launches a given activity before the test starts and closes after the test.
 *
 * <p>You can access the {@link androidx.test.core.app.ActivityScenario} instance via {@link
 * #getScenario()}. You may finish your activity manually in your test, it will not cause any
 * problems and this rule does nothing after the test in such cases.
 *
 * <p>This rule is an upgraded version of the now deprecated {@link
 * androidx.test.rule.ActivityTestRule}.
 *
 * <p>Example:
 *
 * <pre>
 *   &#64;Rule
 *   public ActivityScenarioRule<MyActivity> rule = new ActivityScenarioRule<>(MyActivity.class);
 *
 *   &#64;Test
 *   public void myTest() {
 *     ActivityScenario<MyActivity> scenario = rule.getScenario();
 *     // Your test code goes here.
 *   }
 * </pre>
 */
public final class ActivityScenarioRule<A extends Activity> extends ExternalResource {

  /**
   * Same as {@link java.util.function.Supplier} which requires API level 24.
   *
   * @hide
   */
  interface Supplier<T> {
    T get();
  }

  @NonNull private final Supplier<ActivityScenario<A>> scenarioSupplier;
  @Nullable private ActivityScenario<A> scenario;
  private final boolean launchActivity;

  /**
   * Constructs ActivityScenarioRule for a given activity class.
   *
   * @param activityClass an activity class to launch
   */
  public ActivityScenarioRule(Class<A> activityClass) {
    scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(activityClass));
    this.launchActivity = true;
  }

  /**
   * @see #ActivityScenarioRule(Class)
   * @param activityOptions an activity options bundle to be passed along with the intent to start
   *                        activity.
   */
  public ActivityScenarioRule(Class<A> activityClass, @Nullable Bundle activityOptions) {
    scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(activityClass), activityOptions);
    this.launchActivity = true;
  }

  /**
   * @see #ActivityScenarioRule(Class)
   * @param activityOptions an activity options bundle to be passed along with the intent to start
   *                        activity.
   * @param launchActivity true if the Activity should be launched automatically once per test. If
   *                       set to false the launch of the activity under test will be deferred until
   *                      {@link ActivityScenarioRule#getScenario()} is called.
   */
  public ActivityScenarioRule(Class<A> activityClass, @Nullable Bundle activityOptions, boolean launchActivity) {
    scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(activityClass), activityOptions);
    this.launchActivity = launchActivity;
  }

  /**
   * Constructs ActivityScenarioRule with a given intent.
   *
   * @param startActivityIntent an intent to start an activity
   */
  public ActivityScenarioRule(Intent startActivityIntent) {
    scenarioSupplier = () -> ActivityScenario.launch(checkNotNull(startActivityIntent));
    this.launchActivity = true;
  }

  /**
   * @see #ActivityScenarioRule(Intent)
   * @param activityOptions an activity options bundle to be passed along with the intent to start
   *                        activity.
   */
  public ActivityScenarioRule(Intent startActivityIntent, @Nullable Bundle activityOptions) {
    scenarioSupplier =
        () -> ActivityScenario.launch(checkNotNull(startActivityIntent), activityOptions);
    this.launchActivity = true;
  }

  /**
   * @see #ActivityScenarioRule(Intent)
   * @param activityOptions an activity options bundle to be passed along with the intent to start
   *                        activity.
   * @param launchActivity true if the Activity should be launched automatically once per test. If
   *                       set to false the launch of the activity under test will be deferred until
   *                      {@link ActivityScenarioRule#getScenario()} is called.
   */
  public ActivityScenarioRule(Intent startActivityIntent, @Nullable Bundle activityOptions, boolean launchActivity) {
    scenarioSupplier =
            () -> ActivityScenario.launch(checkNotNull(startActivityIntent), activityOptions);
    this.launchActivity = launchActivity;
  }

  @Override
  protected void before() {
    if (!launchActivity) return;
    scenario = scenarioSupplier.get();
  }

  @Override
  protected void after() {
    if (scenario == null) return;
    scenario.close();
  }

  /**
   * Returns {@link ActivityScenario} of the given activity class.
   *
   * @throws NullPointerException if you call this method while test is not running
   * @return a non-null {@link ActivityScenario} instance
   */
  public ActivityScenario<A> getScenario() {
    if (!launchActivity && scenario == null) scenario = scenarioSupplier.get();
    return checkNotNull(scenario);
  }
}
