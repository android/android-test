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

package androidx.test.espresso.idling;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import androidx.test.espresso.IdlingResource.ResourceCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** Unit tests for {@link CountingIdlingResource}. */
@RunWith(AndroidJUnit4.class)
public class CountingIdlingResourceTest {

  private static final String RESOURCE_NAME = "test_resource";
  private CountingIdlingResource resource;

  @Mock private ResourceCallback mockCallback;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    resource = new CountingIdlingResource(RESOURCE_NAME, true);
  }

  @Test
  public void testResourceName() {
    assertEquals(RESOURCE_NAME, resource.getName());
  }

  @Test
  public void testInvalidStateDetected() throws Exception {
    resource.increment();
    resource.decrement();
    try {
      resource.decrement();
      fail("Should throw illegal state exception!");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testIsIdle() throws Exception {
    assertTrue(callIsIdle());
    resource.increment();
    assertFalse(callIsIdle());
    resource.decrement();
    assertTrue(callIsIdle());
  }

  @Test
  public void testIdleNotification() throws Exception {
    registerIdleCallback();
    assertTrue(callIsIdle());
    verify(mockCallback, never()).onTransitionToIdle();

    resource.increment();
    verify(mockCallback, never()).onTransitionToIdle();
    assertFalse(callIsIdle());

    resource.decrement();
    verify(mockCallback).onTransitionToIdle();
    assertTrue(callIsIdle());
  }

  private void registerIdleCallback() throws Exception {
    FutureTask<Void> registerTask =
        new FutureTask<Void>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                resource.registerIdleTransitionCallback(mockCallback);
                return null;
              }
            });
    getInstrumentation().runOnMainSync(registerTask);
    try {
      registerTask.get();
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }

  private boolean callIsIdle() throws Exception {
    FutureTask<Boolean> isIdleTask = new FutureTask<Boolean>(new IsIdleCallable());
    getInstrumentation().runOnMainSync(isIdleTask);
    try {
      return isIdleTask.get();
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }

  private class IsIdleCallable implements Callable<Boolean> {
    @Override
    public Boolean call() throws Exception {
      return resource.isIdleNow();
    }
  }
}
