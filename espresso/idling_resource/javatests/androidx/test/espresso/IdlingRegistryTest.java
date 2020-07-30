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

package androidx.test.espresso;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
@SuppressWarnings("StaticAccessedFromInstance")
public class IdlingRegistryTest {
  private IdlingRegistry idlingRegistry;

  @Before
  public void setup() {
    // If the current thread is not associated with a looper yet, use the instrumentation thread.
    if (Looper.myLooper() == null) {
      Looper.prepare();
    }
    idlingRegistry = new IdlingRegistry();
  }

  @Test
  public void verifyRegisterAndUnregister() {
    IdlingResource r1 = getDummyIdlingResource("r1");
    IdlingResource r2 = getDummyIdlingResource("r2");
    assertTrue(idlingRegistry.register(r1));
    assertTrue(idlingRegistry.register(r2));
    assertEquals(2, idlingRegistry.getResources().size());
    assertTrue(idlingRegistry.unregister(r1));
    assertTrue(idlingRegistry.unregister(r2));
    assertEquals(0, idlingRegistry.getResources().size());
  }

  @Test
  public void verifyRegisterDupsFails() {
    IdlingResource r1 = getDummyIdlingResource("r1");
    assertTrue(idlingRegistry.register(r1));
    assertFalse(idlingRegistry.register(r1));
    assertEquals(1, idlingRegistry.getResources().size());
  }

  @Test
  public void verifyRegisterLoopers() {
    idlingRegistry.registerLooperAsIdlingResource(Looper.myLooper());
    assertEquals(1, idlingRegistry.getLoopers().size());
  }

  @Test
  public void verifyRegisterDupLoopersFails() {
    idlingRegistry.registerLooperAsIdlingResource(Looper.myLooper());
    idlingRegistry.registerLooperAsIdlingResource(Looper.myLooper());
    assertEquals(1, idlingRegistry.getLoopers().size());
  }

  @Test
  public void verifyRegisterAndUnregisterLooper() {
    idlingRegistry.registerLooperAsIdlingResource(Looper.myLooper());
    idlingRegistry.unregisterLooperAsIdlingResource(Looper.myLooper());
    assertEquals(0, idlingRegistry.getLoopers().size());
  }

  @Test
  public void verifyUnregisterNotRegisteredLooper() {
    assertFalse(idlingRegistry.unregisterLooperAsIdlingResource(Looper.myLooper()));
    assertEquals(0, idlingRegistry.getLoopers().size());
  }

  @Test
  public void verifyAttemptingToRegisterMainLooperThrows() {
    try {
      idlingRegistry.registerLooperAsIdlingResource(Looper.getMainLooper());
      fail("Expected to throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private IdlingResource getDummyIdlingResource(String name) {
    return new IdlingResourceFixture(name, true);
  }

  private static class IdlingResourceFixture implements IdlingResource {
    private final String name;
    private final boolean isIdle;

    IdlingResourceFixture(String name, boolean isIdle) {
      this.name = name;
      this.isIdle = isIdle;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      /* no-op */
    }
  }
}
