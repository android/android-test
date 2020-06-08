/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.runner;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.test.filters.MediumTest;
import androidx.test.runner.intercepting.InterceptingActivityFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class MonitoringInstrumentationTest {

  private MonitoringInstrumentation instrumentation;

  @Before
  public void setUp() throws Exception {
    instrumentation = (MonitoringInstrumentation) getInstrumentation();
  }

  @Test
  public void shouldUseDefaultMechanismForCreatingActivityIfInterceptingActivityFactoryNotSet()
      throws Exception {
    final Class<TestActivity> testActivityClass = TestActivity.class;
    final AtomicReference<Activity> activity = new AtomicReference<>();
    retrieveActivityOnMainThread(testActivityClass, activity);

    instrumentation.waitForIdleSync();
    assertThat(activity.get(), instanceOf(TestActivity.class));
  }

  @Test
  public void shouldCreateNewActivityUsingInterceptingActivityFactoryIfItCanCreate()
      throws Exception {
    final Class<TestActivity> testActivityClass = TestActivity.class;

    final AtomicReference<Activity> testActivityReference = new AtomicReference<>();
    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, true));

    retrieveActivityOnMainThread(testActivityClass, testActivityReference);

    instrumentation.waitForIdleSync();
    assertThat(testActivityReference.get(), sameInstance((Activity) myTestActivity));
  }

  @Test
  public void shouldNotCreateNewActivityUsingInterceptingActivityFactoryIfItCannotCreate()
      throws Exception {
    final Class<TestActivity> testActivityClass = TestActivity.class;

    final AtomicReference<Activity> testActivityReference = new AtomicReference<>();
    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, false));

    retrieveActivityOnMainThread(testActivityClass, testActivityReference);

    instrumentation.waitForIdleSync();
    assertThat(testActivityReference.get(), not(sameInstance((Activity) myTestActivity)));
  }

  @Test
  public void shouldNotCreateNewActivityUsingInterceptingActivityFactoryIfReset() throws Exception {
    final Class<TestActivity> testActivityClass = TestActivity.class;
    final AtomicReference<Activity> activity = new AtomicReference<>();

    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, true));
    instrumentation.useDefaultInterceptingActivityFactory();
    retrieveActivityOnMainThread(testActivityClass, activity);

    instrumentation.waitForIdleSync();
    assertThat(activity.get(), not(sameInstance((Activity) myTestActivity)));
  }

  @Test
  public void runOnMainSyncShouldRethrowAssertionException() {
    final String expectedErrorMessage =
        "This AssertionError should be re-thrown by runOnMainSync() method.";
    try {
      instrumentation.runOnMainSync(() -> fail(expectedErrorMessage));
      fail(
          "AssertionError thrown in the runnable should be re-thrown in the instrumentation"
              + " thread.");
    } catch (Throwable t) {
      assertThat(t, is(instanceOf(AssertionError.class)));
      assertEquals(expectedErrorMessage, t.getMessage());
    }
  }

  private void retrieveActivityOnMainThread(
      final Class<TestActivity> activityClass,
      final AtomicReference<Activity> activityAtomicReference) {
    instrumentation.runOnMainSync(
        new Runnable() {
          @Override
          public void run() {
            try {
              activityAtomicReference.set(
                  instrumentation.newActivity(
                      activityClass.getClassLoader(), activityClass.getName(), new Intent()));
            } catch (Exception ex) {
              fail(ex.getMessage());
            }
          }
        });
  }

  @NonNull
  private static <T extends Activity> InterceptingActivityFactory interceptingActivityFactory(
      final T activity, final boolean shouldCreate) {
    return new InterceptingActivityFactory() {
      @Override
      public boolean shouldIntercept(ClassLoader classLoader, String className, Intent intent) {
        return shouldCreate;
      }

      @Override
      public Activity create(ClassLoader classLoader, String className, Intent intent) {
        return activity;
      }
    };
  }

  public static class TestActivity extends Activity {}

  public static class SomeOtherActivity extends Activity {}
}
