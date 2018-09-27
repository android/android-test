/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.intent;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import androidx.test.runner.intent.IntentStubber;
import org.hamcrest.Matcher;

/**
 * A sneaky singleton object used to respond to intents with fake responses. This interface is not
 * meant for public consumption. Test authors should use {@link Intents} instead.
 */
public interface ResettingStubber extends IntentStubber {

  /**
   * Sets the result that will be returned to the intent sender (if the sender expects the result),
   * next time an intent matched by the given matcher is launched.
   */
  public void setActivityResultForIntent(Matcher<Intent> matcher, ActivityResult result);

  /**
   * Sets a result function that will be called by the intent sender (if the sender expects the
   * result), next time an intent matched by the given matcher is launched.
   */
  public void setActivityResultFunctionForIntent(
      Matcher<Intent> matcher, ActivityResultFunction result);

  /**
   * Marks this spy as initialized. Once initialized, ResettingStubber begins recording intents and
   * provides intent stubbing.
   */
  public void initialize();

  /** @return {@code true} if this spy is initialized */
  public boolean isInitialized();

  /**
   * Clears state (initialization, expected responses).
   *
   * <p>Must be called on main thread.
   */
  public void reset();
}
