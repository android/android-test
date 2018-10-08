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

  private final Activity mMockActivity = mock(Activity.class);

  private final ActivityLifecycleMonitorImpl mMonitor = new ActivityLifecycleMonitorImpl(true);

  public void testAddRemoveListener() {
    ActivityLifecycleCallback callback = mock(ActivityLifecycleCallback.class);

    // multiple adds should only register once.
    mMonitor.addLifecycleCallback(callback);
    mMonitor.addLifecycleCallback(callback);
    mMonitor.addLifecycleCallback(callback);

    mMonitor.signalLifecycleChange(Stage.CREATED, mMockActivity);
    mMonitor.signalLifecycleChange(Stage.STARTED, mMockActivity);

    // multiple removes should no-op.
    mMonitor.removeLifecycleCallback(callback);
    mMonitor.removeLifecycleCallback(callback);

    mMonitor.signalLifecycleChange(Stage.DESTROYED, mMockActivity);

    verify(callback).onActivityLifecycleChanged(mMockActivity, Stage.CREATED);
    verify(callback).onActivityLifecycleChanged(mMockActivity, Stage.STARTED);
    verify(callback, never()).onActivityLifecycleChanged(mMockActivity, Stage.DESTROYED);
  }

  public void testCallbackConsistancy() {
    ConsistancyCheckingCallback callback = new ConsistancyCheckingCallback();
    mMonitor.addLifecycleCallback(callback);

    for (Stage stage : Stage.values()) {
      mMonitor.signalLifecycleChange(stage, mMockActivity);
      if (null != callback.mError) {
        throw callback.mError;
      }
    }
  }

  public void testDirectQueries() {
    Activity mock1 = mock(Activity.class);
    Activity mock2 = mock(Activity.class);
    Activity mock3 = mock(Activity.class);

    mMonitor.signalLifecycleChange(Stage.CREATED, mock1);
    mMonitor.signalLifecycleChange(Stage.CREATED, mock2);
    mMonitor.signalLifecycleChange(Stage.CREATED, mock3);

    assertThat(mMonitor.getLifecycleStageOf(mock1), is(Stage.CREATED));
    assertThat(mMonitor.getLifecycleStageOf(mock2), is(Stage.CREATED));
    assertThat(mMonitor.getLifecycleStageOf(mock3), is(Stage.CREATED));

    List<Activity> expectedActivities = new ArrayList<Activity>();
    expectedActivities.add(mock1);
    expectedActivities.add(mock2);
    expectedActivities.add(mock3);

    assertTrue(expectedActivities.containsAll(mMonitor.getActivitiesInStage(Stage.CREATED)));

    mMonitor.signalLifecycleChange(Stage.DESTROYED, mock1);
    mMonitor.signalLifecycleChange(Stage.PAUSED, mock2);
    mMonitor.signalLifecycleChange(Stage.PAUSED, mock3);
    assertThat(mMonitor.getLifecycleStageOf(mock1), is(Stage.DESTROYED));
    assertThat(mMonitor.getLifecycleStageOf(mock2), is(Stage.PAUSED));
    assertThat(mMonitor.getLifecycleStageOf(mock3), is(Stage.PAUSED));

    assertThat(mMonitor.getActivitiesInStage(Stage.CREATED).isEmpty(), is(true));
    assertThat(mock1, isIn(mMonitor.getActivitiesInStage(Stage.DESTROYED)));
    assertThat(mock2, isIn(mMonitor.getActivitiesInStage(Stage.PAUSED)));
    assertThat(mock3, isIn(mMonitor.getActivitiesInStage(Stage.PAUSED)));
  }

  private class ConsistancyCheckingCallback implements ActivityLifecycleCallback {
    private RuntimeException mError = null;

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
      try {
        assertThat(activity, isIn(mMonitor.getActivitiesInStage(stage)));
        assertThat(mMonitor.getLifecycleStageOf(activity), is(stage));
      } catch (RuntimeException re) {
        mError = re;
      }
    }
  }
}
