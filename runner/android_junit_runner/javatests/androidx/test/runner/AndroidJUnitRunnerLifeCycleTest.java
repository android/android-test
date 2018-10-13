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

  private final ActivityLifecycleCallback callback = Mockito.mock(ActivityLifecycleCallback.class);

  private PublicLifecycleMethodActivity spiedActivity;
  private ActivityLifecycleMonitor monitor;

  public AndroidJUnitRunnerLifeCycleTest() {
    super(PublicLifecycleMethodActivity.class);
  }

  @Override
  public void setActivity(Activity activity) {
    if (null != activity) {
      spiedActivity = Mockito.spy((PublicLifecycleMethodActivity) activity);
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
    monitor = ActivityLifecycleMonitorRegistry.getInstance();
    monitor.addLifecycleCallback(callback);
  }

  @Override
  protected void tearDown() throws Exception {
    monitor.removeLifecycleCallback(callback);
    super.tearDown();
  }

  public void testInstrumentationArgumentsRegistryGetsPopulated() {
    assertNotNull(InstrumentationRegistry.getArguments());
  }

  @UiThreadTest
  public void testActivityPreOnCreateCalled() {
    startActivity(new Intent(), null, null);
    spiedActivity.setRunnableForOnCreate(
        new Runnable() {
          @Override
          public void run() {
            assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.PRE_ON_CREATE)));
            assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.PRE_ON_CREATE));
          }
        });
    getInstrumentation().callActivityOnCreate(spiedActivity, new Bundle());
  }

  // temporarily suppress - fails due to some sort of mockito issue
  @Suppress
  @UiThreadTest
  public void testOnStartStopCalled() {
    startActivity(new Intent(), null, null);

    // if we dont pair start/stop together the test runner will block until a timeout
    // occurs waiting for the activity to stop.
    getInstrumentation().callActivityOnStart(spiedActivity);
    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.STARTED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.STARTED));

    getInstrumentation().callActivityOnStop(spiedActivity);
    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.STOPPED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.STOPPED));

    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onStart();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.STARTED);
    order.verify(spiedActivity).onStop();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.STOPPED);
  }

  @UiThreadTest
  public void testOnCreateCalled() {
    startActivity(new Intent(), null, null);
    Bundle b = new Bundle();
    getInstrumentation().callActivityOnCreate(spiedActivity, b);

    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.CREATED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.CREATED));
    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onCreate(b);
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.CREATED);
  }

  @UiThreadTest
  public void testOnResumeCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnResume(spiedActivity);

    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.RESUMED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.RESUMED));
    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onResume();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.RESUMED);
  }

  @UiThreadTest
  public void testOnPauseCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnPause(spiedActivity);

    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.PAUSED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.PAUSED));
    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onPause();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.PAUSED);
  }

  @UiThreadTest
  public void testOnRestartCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnRestart(spiedActivity);

    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.RESTARTED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.RESTARTED));
    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onRestart();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.RESTARTED);
  }

  @UiThreadTest
  public void testOnDestroyCalled() {
    startActivity(new Intent(), null, null);
    getInstrumentation().callActivityOnDestroy(spiedActivity);

    assertThat(spiedActivity, isIn(monitor.getActivitiesInStage(Stage.DESTROYED)));
    assertThat(monitor.getLifecycleStageOf(spiedActivity), is(Stage.DESTROYED));
    InOrder order = inOrder(spiedActivity, callback);
    order.verify(spiedActivity).onDestroy();
    order.verify(callback).onActivityLifecycleChanged(spiedActivity, Stage.DESTROYED);
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
