/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.idling.net;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import androidx.test.espresso.IdlingResource.ResourceCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class UriIdlingResourceTest {

  private static final String RESOURCE_NAME = "test_resource";
  private UriIdlingResource resource;
  private static final String GOOD_URL = "good_url";
  private static final String BAD_URL_1 = "bad_url_1";
  private static final String BAD_URL_2 = "bad_url_2";
  private static final Pattern BAD_PATTERN_1 = Pattern.compile(".*1.*");
  private static final Pattern BAD_PATTERN_2 = Pattern.compile(".*2.*");
  private static final int NETWORK_IDLE_TIME_MS = 100;

  @Mock private ResourceCallback mockCallback;

  @Mock private UriIdlingResource.HandlerIntf handler;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    resource = new UriIdlingResource(RESOURCE_NAME, NETWORK_IDLE_TIME_MS, true, handler);
  }

  @Test
  public void testResourceName() {
    assertThat(resource.getName(), equalTo(RESOURCE_NAME));
  }

  @Test
  public void testIdleTransition() throws Exception {
    registerIdleCallback();
    assertThat(callIsIdle(), is(true));
    verify(mockCallback, never()).onTransitionToIdle();
    resource.beginLoad(GOOD_URL);
    assertThat(callIsIdle(), is(false));
    resource.forceIdleTransition();
    assertThat(callIsIdle(), is(true));
    verify(mockCallback).onTransitionToIdle();
  }

  @Test
  public void testIsIdle() throws Exception {
    assertHandlerCounts(0, 0);
    assertThat(callIsIdle(), is(true));
    resource.beginLoad(GOOD_URL);
    assertHandlerCounts(0, 1);
    assertThat(callIsIdle(), is(false));
    resource.endLoad(GOOD_URL);
    assertHandlerCounts(1, 1);
    // Implicit transition to idle after postDelayed called
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidStateDetected() throws Exception {
    resource.beginLoad(GOOD_URL);
    resource.endLoad(GOOD_URL);
    resource.endLoad(GOOD_URL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTimeout() throws Exception {
    resource = new UriIdlingResource(RESOURCE_NAME, -1, true, handler);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroTimeout() throws Exception {
    resource = new UriIdlingResource(RESOURCE_NAME, 0, true, handler);
  }

  @Test
  public void testUrlBlockList() throws Exception {
    resource.ignoreUri(BAD_PATTERN_1);
    resource.ignoreUri(BAD_PATTERN_2);
    assertThat(callIsIdle(), is(true));
    resource.beginLoad(BAD_URL_1);
    resource.beginLoad(BAD_URL_2);
    assertThat(callIsIdle(), is(true));
    resource.beginLoad(GOOD_URL);
    assertThat(callIsIdle(), is(false));
    resource.endLoad(BAD_URL_1);
    resource.endLoad(BAD_URL_2);
    assertHandlerCounts(0, 1);
    resource.endLoad(GOOD_URL);
    assertHandlerCounts(1, 1);
    // Implicit transition to idle after postDelayed called
  }

  // Test helper methods

  private void assertHandlerCounts(int posted, int removed) {
    verify(handler, times(posted)).postDelayed(any(Runnable.class), anyLong());
    verify(handler, times(removed)).removeCallbacks(any(Runnable.class));
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
    InstrumentationRegistry.getInstrumentation().runOnMainSync(registerTask);
    try {
      registerTask.get();
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }

  private boolean callIsIdle() throws Exception {
    FutureTask<Boolean> isIdleTask = new FutureTask<Boolean>(new IsIdleCallable());
    InstrumentationRegistry.getInstrumentation().runOnMainSync(isIdleTask);
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
