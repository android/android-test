/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.intent.rule;

import android.app.Activity;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

/**
 * This rule makes it easy to use Espresso-Intents APIs in functional UI tests. This class is an
 * extension of {@link ActivityTestRule}, which initializes Espresso-Intents before each test
 * annotated with <a href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code>
 * </a> and releases Espresso-Intents after each test run. The Activity will be terminated after
 * each test and this rule can be used in the same way as {@link ActivityTestRule}.
 *
 * <p>Espresso-Intents APIs can be used in two ways:
 *
 * <ul>
 *   <li>Intent Verification, using the {@link Intents#intended(Matcher)} API
 *   <li>Intent Stubbing, using the {@link Intents#intending(Matcher)} API
 * </ul>
 *
 * @param <T> The activity to test
 * @deprecated Use {@link androidx.test.espresso.intent.Intents.init()}, in conjunction with {@link
 *     androidx.test.core.app.ActivityScenario} or {@link
 *     androidx.test.ext.junit.rules.ActivityScenarioRule} instead.
 */
@Deprecated
public class IntentsTestRule<T extends Activity> extends ActivityTestRule<T> {

  private boolean isInitialized;

  public IntentsTestRule(Class<T> activityClass) {
    super(activityClass);
  }

  public IntentsTestRule(Class<T> activityClass, boolean initialTouchMode) {
    super(activityClass, initialTouchMode);
  }

  public IntentsTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
    super(activityClass, initialTouchMode, launchActivity);
  }

  @Override
  protected void afterActivityLaunched() {
    Intents.init();
    isInitialized = true;
    super.afterActivityLaunched();
  }

  @Override
  protected void afterActivityFinished() {
    super.afterActivityFinished();
    if (isInitialized) {
      // Otherwise will throw a NPE if Intents.init() wasn't called.
      Intents.release();
      isInitialized = false;
    }
  }
}
