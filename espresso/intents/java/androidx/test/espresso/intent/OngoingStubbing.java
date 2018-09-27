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

import static androidx.test.espresso.intent.Checks.checkNotNull;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import org.hamcrest.Matcher;

/** Supports method chaining after @Intents#intending method call. */
public final class OngoingStubbing {

  private final Matcher<Intent> matcher;
  private final ResettingStubber resettingStubber;
  private final Instrumentation instrumentation;

  OngoingStubbing(
      Matcher<Intent> matcher, ResettingStubber resettingStubber, Instrumentation instrumentation) {
    this.matcher = checkNotNull(matcher);
    this.resettingStubber = checkNotNull(resettingStubber);
    this.instrumentation = checkNotNull(instrumentation);
  }

  /** Sets a response for the intent being stubbed. */
  public void respondWith(final ActivityResult result) {
    respondWithFunction(intent -> checkNotNull(result));
  }

  /** Sets a response callable for the intent being stubbed. */
  public void respondWithFunction(final ActivityResultFunction result) {
    checkNotNull(result);
    instrumentation.waitForIdleSync();
    instrumentation.runOnMainSync(
        () -> resettingStubber.setActivityResultFunctionForIntent(matcher, result));
    instrumentation.waitForIdleSync();
  }
}
