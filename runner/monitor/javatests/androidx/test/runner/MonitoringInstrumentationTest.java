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
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.AppComponentFactoryRegistry;
import androidx.test.runner.intercepting.InterceptingActivityFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class MonitoringInstrumentationTest {

  private MonitoringInstrumentation instrumentation;

  @Before
  public void setUp() throws Exception {
    instrumentation = (MonitoringInstrumentation) getInstrumentation();
  }

  @Test
  @SdkSuppress(minSdkVersion = 28)
  public void shouldCreateNewAppComponentsUsingAppComponentFactorySet() {
    final AppComponentFactory factory = new TestAppComponentFactory();
    AppComponentFactoryRegistry.setAppComponentFactory(factory);

    final AtomicReference<Activity> activity =
        retrieveActivityOnMainThread(SomeOtherActivity.class);
    final AtomicReference<Application> application =
        retrieveApplicationOnMainThread(SomeOtherApplication.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), instanceOf(SomeOtherActivity.class));
    assertThat(application.get(), instanceOf(SomeOtherApplication.class));
  }

  @Test
  @SdkSuppress(minSdkVersion = 28)
  public void shouldUseDefaultMechanismForCreatingAppComponentsIfAppComponentFactoryNotSet() {
    AppComponentFactoryRegistry.setAppComponentFactory(null);

    final AtomicReference<Activity> activity = retrieveActivityOnMainThread(TestActivity.class);
    final AtomicReference<Application> application =
        retrieveApplicationOnMainThread(TestApplication.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), instanceOf(TestActivity.class));
    assertThat(application.get(), instanceOf(TestApplication.class));
  }

  @Test
  public void shouldUseDefaultMechanismForCreatingActivityIfInterceptingActivityFactoryNotSet()
      throws Exception {
    final AtomicReference<Activity> activity = retrieveActivityOnMainThread(TestActivity.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), instanceOf(TestActivity.class));
  }

  @Test
  public void shouldCreateNewActivityUsingInterceptingActivityFactoryIfItCanCreate()
      throws Exception {
    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, true));

    final AtomicReference<Activity> activity = retrieveActivityOnMainThread(TestActivity.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), sameInstance(myTestActivity));
  }

  @Test
  public void shouldNotCreateNewActivityUsingInterceptingActivityFactoryIfItCannotCreate()
      throws Exception {
    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, false));

    final AtomicReference<Activity> activity = retrieveActivityOnMainThread(TestActivity.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), not(sameInstance(myTestActivity)));
  }

  @Test
  public void shouldNotCreateNewActivityUsingInterceptingActivityFactoryIfReset() throws Exception {
    final TestActivity myTestActivity = mock(TestActivity.class);
    instrumentation.interceptActivityUsing(interceptingActivityFactory(myTestActivity, true));
    instrumentation.useDefaultInterceptingActivityFactory();

    final AtomicReference<Activity> activity = retrieveActivityOnMainThread(TestActivity.class);
    instrumentation.waitForIdleSync();

    assertThat(activity.get(), not(sameInstance(myTestActivity)));
  }

  @Test
  public void runOnMainSyncShouldRethrowAssertionException() {
    final String expectedErrorMessage =
        "This AssertionError should be re-thrown by runOnMainSync() method.";
    Throwable actual = null;
    try {
      instrumentation.runOnMainSync(() -> fail(expectedErrorMessage));
    } catch (Throwable t) {
      actual = t;
    }
    assertThat(
        /* reason= */ "AssertionError thrown in the runnable should be re-thrown in the"
            + " instrumentation thread.",
        /* actual= */ actual,
        /* matcher= */ is(instanceOf(AssertionError.class)));
    assertThat(expectedErrorMessage, equalTo(actual.getMessage()));
  }

  private AtomicReference<Activity> retrieveActivityOnMainThread(
      final Class<? extends Activity> classRef) {
    final AtomicReference<Activity> atomicReference = new AtomicReference<>();
    instrumentation.runOnMainSync(
        () -> {
          try {
            final ClassLoader cl = classRef.getClassLoader();
            final String className = classRef.getName();
            final Activity activity = instrumentation.newActivity(cl, className, new Intent());
            atomicReference.set(activity);
          } catch (Exception ex) {
            fail(ex.getMessage());
          }
        });
    return atomicReference;
  }

  private AtomicReference<Application> retrieveApplicationOnMainThread(
      final Class<? extends Application> applicationClass) {
    final AtomicReference<Application> atomicReference = new AtomicReference<>();
    instrumentation.runOnMainSync(
        () -> {
          try {
            final ClassLoader cl = applicationClass.getClassLoader();
            final String className = applicationClass.getName();
            final Context context = instrumentation.getTargetContext();
            final Application application = instrumentation.newApplication(cl, className, context);
            atomicReference.set(application);
          } catch (Exception ex) {
            fail(ex.getMessage());
          }
        });
    return atomicReference;
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

  public static class TestApplication extends Application {}

  public static class TestActivity extends Activity {}

  public static class SomeOtherActivity extends Activity {}

  public static class SomeOtherApplication extends Application {}

  public static class TestAppComponentFactory extends AppComponentFactory {

    @NonNull
    @Override
    public Activity instantiateActivity(
        @NonNull ClassLoader cl, @NonNull String className, @Nullable Intent intent)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
      if (className.equals(SomeOtherActivity.class.getName())) {
        return new SomeOtherActivity();
      } else {
        return super.instantiateActivity(cl, className, intent);
      }
    }

    @NonNull
    @Override
    public Application instantiateApplication(@NonNull ClassLoader cl, @NonNull String className)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
      if (className.equals(SomeOtherApplication.class.getName())) {
        return new SomeOtherApplication();
      } else {
        return super.instantiateApplication(cl, className);
      }
    }
  }
}
