/*
 * Copyright (C) 2014 The Android Open Source Project
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
package androidx.test.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Mockito.inOrder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.filters.Suppress;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.mockito.InOrder;
import org.mockito.Mockito;

/** Integration tests between lifecycle management methods in runner and the LifecycleMonitor. */
@MediumTest
public class AndroidJUnitRunnerLifeCycleTest
    extends ActivityUnitTestCase<AndroidJUnitRunnerLifeCycleTest.PublicLifecycleMethodActivity> {

  private final ActivityLifecycleCallback mCallback = Mockito.mock(ActivityLifecycleCallback.class);

  private PublicLifecycleMethodActivity mSpiedActivity;
  private ActivityLifecycleMonitor mMonitor;

  public AndroidJUnitRunnerLifeCycleTest() {
    super(PublicLifecycleMethodActivity.class);
  }

  @Override
  public void setActivity(Activity activity) {
    if (null != activity) {
      mSpiedActivity = Mockito.spy((PublicLifecycleMethodActivity) activity);
    }
  }

  @Override
  public PublicLifecycleMethodActivity getActivity() {
    // otherwise ActivityUnitTestCase will call onCreate which will have side
    // effects in lifecycle tracking which we are specifically testing for.
    return null;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mMonitor = ActivityLifecycleMonitorRegistry.getInstance();
    mMonitor.addLifecycleCallback(mCallback);
  }

  @Override
  protected void tearDown() throws Exception {
    mMonitor.removeLifecycleCallback(mCallback);
    super.tearDown();
  }

  public void testInstrumentationArgumentsRegistryGetsPopulated() {
    assertNotNull(InstrumentationRegistry.getArguments());
  }

  @UiThreadTest
  public void testActivityPreOnCreateCalled() {
    startActivity(new Intent(), null, null);
    mSpiedActivity.setRunnableForOnCreate(
        new Runnable() {
          @Override
          public void run() {
            assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.PRE_ON_CREATE)));
            assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.PRE_ON_CREATE));
          }
        });
    getInstrumentation().callActivityOnCreate(mSpiedActivity, new Bundle());
  }

  // temporarily suppress - fails due to some sort of mockito issue
  @Suppress
  @UiThreadTest
  public void testOnStartStopCalled() {
    startActivity(new Intent(), null, null);

    // if we dont pair start/stop together the test runner will block until a timeout
    // occurs waiting for the activity to stop.
    getInstrumentation().callActivityOnStart(mSpiedActivity);
    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.STARTED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.STARTED));

    getInstrumentation().callActivityOnStop(mSpiedActivity);
    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.STOPPED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.STOPPED));

    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onStart();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.STARTED);
    order.verify(mSpiedActivity).onStop();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.STOPPED);
  }

  @UiThreadTest
  public void testOnCreateCalled() {
    startActivity(new Intent(), null, null);
    Bundle b = new Bundle();
    getInstrumentation().callActivityOnCreate(mSpiedActivity, b);

    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.CREATED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.CREATED));
    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onCreate(b);
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.CREATED);
  }

  @UiThreadTest
  public void testOnResumeCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnResume(mSpiedActivity);

    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.RESUMED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.RESUMED));
    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onResume();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.RESUMED);
  }

  @UiThreadTest
  public void testOnPauseCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnPause(mSpiedActivity);

    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.PAUSED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.PAUSED));
    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onPause();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.PAUSED);
  }

  @UiThreadTest
  public void testOnRestartCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnRestart(mSpiedActivity);

    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.RESTARTED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.RESTARTED));
    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onRestart();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.RESTARTED);
  }

  @UiThreadTest
  public void testOnDestroyCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnDestroy(mSpiedActivity);

    assertThat(mSpiedActivity, isIn(mMonitor.getActivitiesInStage(Stage.DESTROYED)));
    assertThat(mMonitor.getLifecycleStageOf(mSpiedActivity), is(Stage.DESTROYED));
    InOrder order = inOrder(mSpiedActivity, mCallback);
    order.verify(mSpiedActivity).onDestroy();
    order.verify(mCallback).onActivityLifecycleChanged(mSpiedActivity, Stage.DESTROYED);
  }

  /** Makes lifecycle methods public so we can verify on them. */
  public static class PublicLifecycleMethodActivity extends Activity {
    private Runnable runnableForOnCreate;

    /**
     * Invokes the runnable in onCreate of this activity.
     *
     * @param runnable runnable to invoke in onCreate or {@code null} for no-op runnable.
     */
    public void setRunnableForOnCreate(Runnable runnable) {
      runnableForOnCreate = runnable;
    }

    @Override
    public void onStart() {
      super.onStart();
    }

    @Override
    public void onStop() {
      super.onStop();
    }

    @Override
    public void onCreate(Bundle b) {
      super.onCreate(b);
      if (runnableForOnCreate != null) {
        runnableForOnCreate.run();
      }
    }

    @Override
    public void onRestart() {
      super.onRestart();
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
    }

    @Override
    public void onPause() {
      super.onPause();
    }

    @Override
    public void onResume() {
      super.onResume();
    }
  }
}
