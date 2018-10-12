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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import androidx.test.filters.SmallTest;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/** ActivityLifecycleMonitorImpl tests. */
@SmallTest
public class ActivityLifecycleMonitorImplTest extends TestCase {

  private final Activity mockActivity = mock(Activity.class);

  private final ActivityLifecycleMonitorImpl monitor = new ActivityLifecycleMonitorImpl(true);

  public void testAddRemoveListener() {
    ActivityLifecycleCallback callback = mock(ActivityLifecycleCallback.class);

    // multiple adds should only register once.
    monitor.addLifecycleCallback(callback);
    monitor.addLifecycleCallback(callback);
    monitor.addLifecycleCallback(callback);

    monitor.signalLifecycleChange(Stage.CREATED, mockActivity);
    monitor.signalLifecycleChange(Stage.STARTED, mockActivity);

    // multiple removes should no-op.
    monitor.removeLifecycleCallback(callback);
    monitor.removeLifecycleCallback(callback);

    monitor.signalLifecycleChange(Stage.DESTROYED, mockActivity);

    verify(callback).onActivityLifecycleChanged(mockActivity, Stage.CREATED);
    verify(callback).onActivityLifecycleChanged(mockActivity, Stage.STARTED);
    verify(callback, never()).onActivityLifecycleChanged(mockActivity, Stage.DESTROYED);
  }

  public void testCallbackConsistancy() {
    ConsistancyCheckingCallback callback = new ConsistancyCheckingCallback();
    monitor.addLifecycleCallback(callback);

    for (Stage stage : Stage.values()) {
      monitor.signalLifecycleChange(stage, mockActivity);
      if (null != callback.error) {
        throw callback.error;
      }
    }
  }

  public void testDirectQueries() {
    Activity mock1 = mock(Activity.class);
    Activity mock2 = mock(Activity.class);
    Activity mock3 = mock(Activity.class);

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
