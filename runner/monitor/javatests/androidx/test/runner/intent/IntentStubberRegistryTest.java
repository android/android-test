/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.runner.intent;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import androidx.test.filters.SmallTest;

/** {@link IntentStubberRegistry} tests. */
@SmallTest
public class IntentStubberRegistryTest extends InstrumentationTestCase {

  private IntentStubber mIntentStubber;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mIntentStubber =
        new IntentStubber() {
          @Override
          public ActivityResult getActivityResultForIntent(Intent intent) {
            return null;
          }
        };
  }

  @Override
  protected void tearDown() throws Exception {
    IntentStubberRegistry.reset();
    assertFalse(IntentStubberRegistry.isLoaded());
    super.tearDown();
  }

  @UiThreadTest
  public void testIntentStubberLoading() {
    IntentStubberRegistry.load(mIntentStubber);
    assertTrue(IntentStubberRegistry.isLoaded());

    assertNotNull(IntentStubberRegistry.getInstance());
  }

  public void testLoadCanOnlyBeCalledOnce() {
    try {
      IntentStubberRegistry.load(mIntentStubber);
      IntentStubberRegistry.load(mIntentStubber);
      fail("IllegalStateException expected. An Intent Stubber can only be stubbed once!");

    } catch (IllegalStateException expected) {
    }
  }

  public void testLoadPassingNullThrows() {
    try {
      IntentStubberRegistry.load(null);
      fail("NullPointerException expected. Intent Stubber cannot be null!");
    } catch (NullPointerException expected) {
    }
  }

  public void testGetInstanceCanOnlyBeCalledOnMainThread() {
    IntentStubberRegistry.load(mIntentStubber);
    try {
      IntentStubberRegistry.getInstance();
      fail(
          "IllegalStateException expected. getInstance() should only be allowed on main"
              + "thread!");
    } catch (IllegalStateException expected) {
    }
  }

  public void testNoInstanceLoadedThrows() {
    try {
      IntentStubberRegistry.getInstance();
      fail("IllegalStateException expected. No instance available, load must be called" + "first");
    } catch (IllegalStateException expected) {
    }
  }
}
