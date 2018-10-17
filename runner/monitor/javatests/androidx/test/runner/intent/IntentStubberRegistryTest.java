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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** {@link IntentStubberRegistry} tests. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class IntentStubberRegistryTest {

  private IntentStubber mIntentStubber;

  @Before
  public void setUp() throws Exception {
    mIntentStubber =
        new IntentStubber() {
          @Override
          public ActivityResult getActivityResultForIntent(Intent intent) {
            return null;
          }
        };
  }

  @After
  public void tearDown() throws Exception {
    IntentStubberRegistry.reset();
    assertThat(IntentStubberRegistry.isLoaded()).isFalse();
  }

  @Test
  @UiThreadTest
  public void testIntentStubberLoading() {
    IntentStubberRegistry.load(mIntentStubber);
    assertThat(IntentStubberRegistry.isLoaded()).isTrue();

    assertThat(IntentStubberRegistry.getInstance()).isNotNull();
  }

  @Test
  public void testLoadCanOnlyBeCalledOnce() {
    try {
      IntentStubberRegistry.load(mIntentStubber);
      IntentStubberRegistry.load(mIntentStubber);
      fail("IllegalStateException expected. An Intent Stubber can only be stubbed once!");

    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testLoadPassingNullThrows() {
    try {
      IntentStubberRegistry.load(null);
      fail("NullPointerException expected. Intent Stubber cannot be null!");
    } catch (NullPointerException expected) {
    }
  }

  @Test
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

  @Test
  public void testNoInstanceLoadedThrows() {
    try {
      IntentStubberRegistry.getInstance();
      fail("IllegalStateException expected. No instance available, load must be called" + "first");
    } catch (IllegalStateException expected) {
    }
  }
}
