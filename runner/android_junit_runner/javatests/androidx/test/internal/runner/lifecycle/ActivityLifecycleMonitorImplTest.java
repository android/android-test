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

package androidx.test.internal.runner.lifecycle;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** ActivityLifecycleMonitorImpl tests. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ActivityLifecycleMonitorImplTest {

  private Activity activity;
  private ActivityLifecycleMonitorImpl monitor;

  @Before
  public void setUp() {
    getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              public void run() {
                activity = new Activity();
                monitor = new ActivityLifecycleMonitorImpl(true);
              }
            });
  }

  @Test
  public void testAddRemoveListener() {
    ActivityLifecycleCallback callback = mock(ActivityLifecycleCallback.class);

    // multiple adds should only register once.
    monitor.addLifecycleCallback(callback);
    monitor.addLifecycleCallback(callback);
    monitor.addLifecycleCallback(callback);

    monitor.signalLifecycleChange(Stage.CREATED, activity);
    monitor.signalLifecycleChange(Stage.STARTED, activity);

    // multiple removes should no-op.
    monitor.removeLifecycleCallback(callback);
    monitor.removeLifecycleCallback(callback);

    monitor.signalLifecycleChange(Stage.DESTROYED, activity);

    verify(callback).onActivityLifecycleChanged(activity, Stage.CREATED);
    verify(callback).onActivityLifecycleChanged(activity, Stage.STARTED);
    verify(callback, never()).onActivityLifecycleChanged(activity, Stage.DESTROYED);
  }

  @Test
  public void testCallbackConsistancy() {
    ConsistancyCheckingCallback callback = new ConsistancyCheckingCallback();
    monitor.addLifecycleCallback(callback);

    for (Stage stage : Stage.values()) {
      monitor.signalLifecycleChange(stage, activity);
      if (null != callback.error) {
        throw callback.error;
      }
    }
  }

  @Test
  @UiThreadTest
  public void testDirectQueries() {
    Activity mock1 = new Activity();
    Activity mock2 = new Activity();
    Activity mock3 = new Activity();

    monitor.signalLifecycleChange(Stage.CREATED, mock1);
    monitor.signalLifecycleChange(Stage.CREATED, mock2);
    monitor.signalLifecycleChange(Stage.CREATED, mock3);

    assertThat(monitor.getLifecycleStageOf(mock1), is(Stage.CREATED));
    assertThat(monitor.getLifecycleStageOf(mock2), is(Stage.CREATED));
    assertThat(monitor.getLifecycleStageOf(mock3), is(Stage.CREATED));

    List<Activity> expectedActivities = new ArrayList<Activity>();
    expectedActivities.add(mock1);
    expectedActivities.add(mock2);
    expectedActivities.add(mock3);

    assertTrue(expectedActivities.containsAll(monitor.getActivitiesInStage(Stage.CREATED)));

    monitor.signalLifecycleChange(Stage.DESTROYED, mock1);
    monitor.signalLifecycleChange(Stage.PAUSED, mock2);
    monitor.signalLifecycleChange(Stage.PAUSED, mock3);
    assertThat(monitor.getLifecycleStageOf(mock1), is(Stage.DESTROYED));
    assertThat(monitor.getLifecycleStageOf(mock2), is(Stage.PAUSED));
    assertThat(monitor.getLifecycleStageOf(mock3), is(Stage.PAUSED));

    assertThat(monitor.getActivitiesInStage(Stage.CREATED).isEmpty(), is(true));
    assertThat(mock1, isIn(monitor.getActivitiesInStage(Stage.DESTROYED)));
    assertThat(mock2, isIn(monitor.getActivitiesInStage(Stage.PAUSED)));
    assertThat(mock3, isIn(monitor.getActivitiesInStage(Stage.PAUSED)));
  }

  private class ConsistancyCheckingCallback implements ActivityLifecycleCallback {
    private RuntimeException error = null;

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
      try {
        assertThat(activity, isIn(monitor.getActivitiesInStage(stage)));
        assertThat(monitor.getLifecycleStageOf(activity), is(stage));
      } catch (RuntimeException re) {
        error = re;
      }
    }
  }
}
