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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Activity;
import android.support.annotation.Nullable;
import androidx.test.annotation.Beta;
import androidx.test.core.app.ActivityScenario;
import org.junit.rules.ExternalResource;

/**
 * ActivityScenarioRule launches a given activity before the test starts and closes after the test.
 *
 * <p>You can access to scenario interface via {@link #getScenario()} method. You may finish your
 * activity manually in your test, it will not cause any problems and this rule does nothing after
 * the test in such cases.
 *
 * <p>This rule is an upgraded version of {@link androidx.test.rule.ActivityTestRule}. The previous
 * version will be deprecated and eventually be removed from the library in the future.
 *
 * <pre>{@code
 * Example:
 *  }{@literal @Rule}{@code
 *   ActivityScenarioRule<MyActivity> rule = new ActivityScenarioRule<>(MyActivity.class);
 *
 *  }{@literal @Test}{@code
 *   public void myTest() {
 *     ActivityScenario<MyActivity> scenario = rule.getScenario();
 *     // Your test code goes here.
 *   }
 * }</pre>
 */
@Beta
public final class ActivityScenarioRule<A extends Activity> extends ExternalResource {

  private final Class<A> activityClass;
  @Nullable private ActivityScenario<A> scenario;

  /**
   * Constructs ActivityScenarioRule for a given activity class.
   *
   * @param activityClass an activity class to launch
   */
  public ActivityScenarioRule(Class<A> activityClass) {
    this.activityClass = activityClass;
  }

  @Override
  protected void before() throws Throwable {
    scenario = ActivityScenario.launch(activityClass);
  }

  @Override
  protected void after() {
    scenario.close();
  }

  /**
   * Returns {@link ActivityScenario} of the given activity class.
   *
   * @throws NullPointerException if you call this method while test is not running
   * @return a non-null {@link ActivityScenario} instance
   */
  public ActivityScenario<A> getScenario() {
    return checkNotNull(scenario);
  }
}
