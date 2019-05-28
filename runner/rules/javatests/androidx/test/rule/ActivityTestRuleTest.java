/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.rule;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.runner.JUnitCore.runClasses;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.MonitoringInstrumentation;
import androidx.test.runner.intercepting.SingleActivityFactory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityTestRuleTest {

  private static final String STUB_ANDROID_PACKAGE_NAME = "com.android.testing.rocks";

  private static final ActivityFixture mockActivity = mock(ActivityFixture.class);

  public static class ActivityFixture extends Activity {}

  public static class ActivityLifecycleTest {

    private static StringBuilder log = new StringBuilder();

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(ActivityFixture.class) {
          @Override
          public ActivityFixture launchActivity(Intent startIntent) {
            log.append("launchActivity ");
            activity = makeWeakReference(mockActivity);
            return activity.get();
          }

          @Override
          public void finishActivity() {
            log.append("finishActivity");
            super.finishActivity();
          }

          @Override
          void callFinishOnMainSync() {
            // nothing
          }
        };

    @Before
    public void before() {
      log.append("before ");
    }

    @Test
    public void fails() {
      log.append("test ");
      fail("This is a dummy test");
    }

    @After
    public void after() {
      log.append("after ");
    }
  }

  @Test
  public void activityLifecycleTest() {
    Result result = runClasses(ActivityLifecycleTest.class);
    assertThat(result.getFailureCount(), equalTo(1));
    assertThat(result.getFailures().get(0).getMessage(), equalTo("This is a dummy test"));
    assertThat(
        ActivityLifecycleTest.log.toString(),
        equalTo("launchActivity before test after finishActivity"));
  }

  public static class FailureOfActivityLaunchTest {

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<>(ActivityFixture.class);

    @Test
    public void emptyTest() {}
  }

  @Test
  public void failureOfActivityLaunch() {
    Result result = runClasses(FailureOfActivityLaunchTest.class);
    assertThat(result.getFailureCount(), equalTo(1));
    assertThat(
        result.getFailures().get(0).getException().getClass(),
        CoreMatchers.<Class<?>>equalTo(RuntimeException.class));
    assertThat(result.getFailures().get(0).getMessage(), equalTo("Could not launch activity"));
  }

  public static class SuccessfulLaunchTest {

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(ActivityFixture.class) {
          @Override
          public ActivityFixture launchActivity(Intent startIntent) {
            activity = makeWeakReference(mockActivity);
            return activity.get();
          }

          @Override
          public void finishActivity() {
            activity = null;
          }
        };

    @Test
    public void verifyTheLaunchedActivityIsReturned() {
      ActivityFixture activity = activityRule.getActivity();
      assertThat(activity, notNullValue());
    }
  }

  @Test
  public void successfulLaunch() {
    Result result = runClasses(SuccessfulLaunchTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class SuccessfulLaunchWithDefaultsTest {

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(ActivityFixture.class, false, false) {};

    @Test
    public void verifyLaunchedActivityIsReturned() {
      Instrumentation mockInstrumentation = mock(Instrumentation.class);
      activityRule.setInstrumentation(mockInstrumentation);

      ActivityFixture activity = activityRule.launchActivity(null);

      assertThat(activity, notNullValue());
      assertThat(
          activityRule.getActivityIntent().getComponent().getPackageName(),
          equalTo(getApplicationContext().getPackageName()));
      assertThat(
          activityRule.getActivityIntent().getFlags(), equalTo(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
  }

  @Test
  public void successfulLaunchWithDefaults() {
    Result result = runClasses(SuccessfulLaunchTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class MockedInstrumentationTest {

    @Mock Instrumentation instrumentation;

    @Mock Context targetContext;

    boolean beforeActivityLaunched;
    boolean afterActivityLaunched;
    boolean afterActivityFinished;

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(ActivityFixture.class, false, false) {

          @Override
          protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            beforeActivityLaunched = true;
          }

          @Override
          protected void afterActivityLaunched() {
            afterActivityLaunched = true;
            super.afterActivityLaunched();
          }

          @Override
          protected void afterActivityFinished() {
            afterActivityFinished = true;
            super.afterActivityFinished();
          }

          @Override
          void callFinishOnMainSync() {
            // nothing
          }
        };

    @Before
    public void injectMockInstrumentation() {
      MockitoAnnotations.initMocks(this);
      beforeActivityLaunched = false;
      afterActivityLaunched = false;
      afterActivityFinished = false;
      activityRule.setInstrumentation(instrumentation);
      withStubbedInstrumentation();
    }

    /** Stubs Instrumentation to return a mock Activity and mock Context and package name. */
    protected void withStubbedInstrumentation() {
      when(instrumentation.startActivitySync(any(Intent.class))).thenReturn(mockActivity);
      when(instrumentation.getTargetContext()).thenReturn(targetContext);
      when(targetContext.getPackageName()).thenReturn(STUB_ANDROID_PACKAGE_NAME);
    }
  }

  public static class LazyLaunchActivityLifecycleTest extends MockedInstrumentationTest {

    @Test
    public void launchActivityCallsLifecycleMethods() {
      assertThat(activityRule.getActivity(), nullValue());
      assertThat(beforeActivityLaunched, equalTo(false));

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), notNullValue());
      assertThat(beforeActivityLaunched, equalTo(true));
      assertThat(afterActivityLaunched, equalTo(true));
    }
  }

  @Test
  public void launchActivityLifecycle() {
    Result result = runClasses(LazyLaunchActivityLifecycleTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class LazyLaunchWithRelaunchActivityLifecycleTest
      extends MockedInstrumentationTest {

    @Test
    public void launchActivityCallsLifecycleMethods() {
      assertThat(activityRule.getActivity(), nullValue());
      assertThat(beforeActivityLaunched, equalTo(false));
      assertThat(afterActivityLaunched, equalTo(false));
      assertThat(afterActivityFinished, equalTo(false));

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), notNullValue());
      assertThat(afterActivityLaunched, equalTo(true));

      activityRule.finishActivity();

      assertThat(activityRule.getActivity(), nullValue());
      assertThat(afterActivityFinished, equalTo(true));

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), notNullValue());
      assertThat(afterActivityLaunched, equalTo(true));
    }
  }

  @Test
  public void lazyLaunchWithRelaunchActivityLifecycle() {
    Result result = runClasses(LazyLaunchWithRelaunchActivityLifecycleTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  /**
   * When an Activity fails to launch we need to make sure an error is logged to logcat and that the
   * after* lifecycle methods are not called.
   */
  public static class FailedActivityLaunchLifecycle extends MockedInstrumentationTest {

    @Override
    protected void withStubbedInstrumentation() {
      when(instrumentation.startActivitySync(any(Intent.class)))
          .thenReturn(null /* return null here to simulate a failed Activity launch */);
      when(instrumentation.getTargetContext()).thenReturn(targetContext);
      when(targetContext.getPackageName()).thenReturn(STUB_ANDROID_PACKAGE_NAME);
    }

    @Test
    public void failedActivityLaunch_DoesNotCallAfterLifecycleMethods() {
      assertThat(activityRule.getActivity(), nullValue());
      assertThat(beforeActivityLaunched, equalTo(false));

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), nullValue());
      assertThat(beforeActivityLaunched, equalTo(true));
      assertThat(afterActivityLaunched, equalTo(false));
      assertThat(afterActivityFinished, equalTo(false));

      verify(instrumentation).sendStatus(eq(0), any(Bundle.class));
    }
  }

  @Test
  public void failedActivityLaunchLifecycle() {
    Result result = runClasses(FailedActivityLaunchLifecycle.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class ActivityDoesNotLaunchedWhenLazyLaunchFlagSetTest
      extends MockedInstrumentationTest {

    @Test
    public void lazilyLaunchedActivity_ExercisesLifecycleCorrectly() {
      assertThat(activityRule.getActivity(), nullValue());
      assertThat(beforeActivityLaunched, equalTo(false));

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), notNullValue());
      assertThat(beforeActivityLaunched, equalTo(true));
      assertThat(afterActivityLaunched, equalTo(true));
      assertThat(afterActivityFinished, equalTo(false));
    }
  }

  @Test
  public void activity_DoesNotLaunch_WhenLazyLaunchFlagSet() {
    Result result = runClasses(ActivityDoesNotLaunchedWhenLazyLaunchFlagSetTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class CustomIntentPerTest extends MockedInstrumentationTest {

    @Test
    public void customIntent_Set_IsUsedToStartActivity() {
      assertThat(activityRule.getActivity(), nullValue());

      final Intent actionPick = new Intent(Intent.ACTION_PICK);
      activityRule.launchActivity(actionPick);

      assertThat(activityRule.getActivity(), notNullValue());
      verify(instrumentation).startActivitySync(actionPick);
    }

    @Test
    public void customIntent_NotSet_UsesDefaultIntentToStartActivity() {
      assertThat(activityRule.getActivity(), nullValue());

      activityRule.launchActivity(null);

      assertThat(activityRule.getActivity(), notNullValue());
    }

    @Test
    public void customIntent_SetToNull_DoesNotThrow() {
      assertThat(activityRule.getActivity(), nullValue());
      assertThat(activityRule.launchActivity(null), notNullValue());
    }

    @Test
    public void customIntent_OverridesDefaultValues() {
      String customActivityClass = "SomeCustomActivity";
      String customPackageName = "some.custom.package";
      assertThat(activityRule.getActivity(), nullValue());
      final Intent actionPick =
          new Intent(Intent.ACTION_PICK)
              .setClassName(customPackageName, customActivityClass)
              .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

      assertThat(activityRule.launchActivity(actionPick), notNullValue());
      verify(instrumentation).startActivitySync(eq(actionPick));
    }

    @Test
    public void customIntent_PartialOverride_AddsMissingComponentName() {
      assertThat(activityRule.getActivity(), nullValue());
      final Intent actionPick =
          new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

      ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
      assertThat(activityRule.launchActivity(actionPick), notNullValue());
      verify(instrumentation).startActivitySync(intentArgumentCaptor.capture());
      assertThat(
          intentArgumentCaptor.getValue().getComponent(),
          is(equalTo(new ComponentName(getApplicationContext(), ActivityFixture.class.getName()))));
    }

    @Test
    public void customIntent_PartialOverride_AddsMissingLaunchFlag() {
      String customActivityClass = "SomeCustomActivity";
      String customPackageName = "some.custom.package";
      assertThat(activityRule.getActivity(), nullValue());
      final Intent actionPick =
          new Intent(Intent.ACTION_PICK).setClassName(customPackageName, customActivityClass);

      ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
      assertThat(activityRule.launchActivity(actionPick), notNullValue());
      verify(instrumentation).startActivitySync(intentArgumentCaptor.capture());
      assertThat(
          intentArgumentCaptor.getValue().getFlags(), is(equalTo(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }
  }

  @Test
  public void customIntentPerTest() {
    Result result = runClasses(CustomIntentPerTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class CustomPackagePerTest {

    private static final String CUSTOM_PACKAGE_NAME = "some.custom.package";

    private static final int CUSTOM_FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Mock Instrumentation instrumentation;

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(
            ActivityFixture.class,
            CUSTOM_PACKAGE_NAME,
            Intent.FLAG_ACTIVITY_CLEAR_TOP,
            false,
            false) {};

    @Test
    public void customPackageSet_IsUsedToStartActivity() {
      final Intent customIntent = new Intent(Intent.ACTION_PICK);
      activityRule.launchActivity(customIntent);

      assertThat(customIntent.getComponent().getPackageName(), equalTo(CUSTOM_PACKAGE_NAME));
      assertThat(customIntent.getFlags(), equalTo(CUSTOM_FLAGS));

      verify(instrumentation).startActivitySync(customIntent);
    }
  }

  @Test
  public void customPackageAndFlagsSet() {
    Result result = runClasses(CustomIntentPerTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  public static class CustomGetActivityIntentOverridesDefaults {

    private static final String CUSTOM_CLASS_NAME = "SomeCustomActivity";
    private static final String CUSTOM_PACKAGE_NAME = "some.custom.package";
    private static final int CUSTOM_FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Mock Instrumentation instrumentation;

    @Rule
    public ActivityTestRule<ActivityFixture> activityRule =
        new ActivityTestRule<ActivityFixture>(ActivityFixture.class, false, false) {
          @Override
          protected Intent getActivityIntent() {
            final Intent customIntent =
                new Intent(Intent.ACTION_PICK)
                    .setClassName(CUSTOM_PACKAGE_NAME, CUSTOM_CLASS_NAME)
                    .addFlags(CUSTOM_FLAGS);
            return customIntent;
          }
        };

    @Test
    public void customIntent_PartialOverride_AddsMissingComponentName() {
      assertThat(activityRule.getActivity(), nullValue());

      ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
      assertThat(activityRule.launchActivity(null), notNullValue());
      verify(instrumentation).startActivitySync(intentArgumentCaptor.capture());
      assertThat(
          intentArgumentCaptor.getValue().getComponent(),
          is(equalTo(new ComponentName(getApplicationContext(), ActivityFixture.class.getName()))));
      assertThat(
          intentArgumentCaptor.getValue().getFlags(), is(equalTo(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }
  }

  @Test
  public void customGetActivityIntentOverridesDefaults() {
    Result result = runClasses(CustomIntentPerTest.class);
    assertThat(result.getFailureCount(), equalTo(0));
  }

  @SdkSuppress(minSdkVersion = 17)
  @Test
  public void shouldAskInstrumentationToInterceptActivityUsingGivenFactoryAndResetItAfterTest()
      throws Throwable {
    SingleActivityFactory<ActivityFixture> singleActivityFactory =
        new SingleActivityFactory<ActivityFixture>(ActivityFixture.class) {
          @Override
          public ActivityFixture create(Intent intent) {
            return mockActivity;
          }
        };
    ActivityTestRule<ActivityFixture> activityTestRule =
        new ActivityTestRule<>(singleActivityFactory, true, false);
    MonitoringInstrumentation instrumentation = mock(MonitoringInstrumentation.class);
    when(instrumentation.getTargetContext()).thenReturn(getApplicationContext());
    activityTestRule.setInstrumentation(instrumentation);
    Statement baseStatement = mock(Statement.class);
    activityTestRule.apply(baseStatement, mock(Description.class)).evaluate();

    InOrder inOrder = Mockito.inOrder(instrumentation, baseStatement);
    inOrder.verify(instrumentation).interceptActivityUsing(singleActivityFactory);
    inOrder.verify(baseStatement).evaluate();
    inOrder.verify(instrumentation).useDefaultInterceptingActivityFactory();
  }

  public static class SuccessfulGetActivityResultTest {

    @Rule public ActivityTestRule<Activity> rule = new ActivityTestRule<>(Activity.class);

    @Test
    @UiThreadTest
    public void shouldReturnActivityResult() {
      // We need to use a real Activity (no mock) here in order to capture the result.
      // The Activity we use is android.app.Activity which exists on all API levels.
      Activity activity = rule.getActivity();
      activity.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_VIEW));

      activity.finish(); // must be called on the UI Thread

      ActivityResult activityResult = rule.getActivityResult();
      assertNotNull(activityResult);
      assertEquals(Activity.RESULT_OK, activityResult.getResultCode());
      assertEquals(Intent.ACTION_VIEW, activityResult.getResultData().getAction());
    }

    @Test
    public void shouldReturnActivityResult_withActivityFinish() {
      // We need to use a real Activity (no mock) here in order to capture the result.
      // The Activity we use is android.app.Activity which exists on all API levels.
      Activity activity = rule.getActivity();
      activity.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_VIEW));

      rule.finishActivity();

      ActivityResult activityResult = rule.getActivityResult();
      assertNotNull(activityResult);
      assertEquals(Activity.RESULT_OK, activityResult.getResultCode());
      assertEquals(Intent.ACTION_VIEW, activityResult.getResultData().getAction());
    }
  }

  @Test
  public void successfulGetActivityResult() {
    Result result = runClasses(SuccessfulGetActivityResultTest.class);
    assertEquals(0, result.getFailureCount());
  }

  public static class FailureGetActivityResultTest extends MockedInstrumentationTest {

    @Rule public ActivityTestRule<Activity> rule = new ActivityTestRule<>(Activity.class);

    @Test
    public void shouldFailWhenNotFinishing() {
      Activity activity = rule.getActivity();
      activity.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_VIEW));
      rule.getActivityResult();
    }
  }

  @Test
  public void failureGetActivityResult() {
    Result result = runClasses(FailureGetActivityResultTest.class);
    assertEquals(1, result.getFailureCount());
    assertEquals(
        result.getFailures().get(0).getTrace(),
        IllegalStateException.class,
        result.getFailures().get(0).getException().getClass());
  }

  @Test
  public void verifyLaunchFlagIsSetForNullIntent() {
    ActivityTestRule<Activity> rule = new ActivityTestRule<>(Activity.class, false, false);
    rule.launchActivity(null);
    assertEquals(
        Intent.FLAG_ACTIVITY_NEW_TASK,
        rule.getActivity().getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK);
  }
}
