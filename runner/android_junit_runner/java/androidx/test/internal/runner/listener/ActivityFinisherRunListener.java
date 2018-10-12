/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.internal.runner.listener;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Instrumentation;
import androidx.test.internal.runner.InstrumentationConnection;
import androidx.test.runner.MonitoringInstrumentation;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Ensures that no activities are running when a test method starts and that no activities are still
 * running when it ends.
 */
public class ActivityFinisherRunListener extends RunListener {
  private final Instrumentation instrumentation;
  private final MonitoringInstrumentation.ActivityFinisher activityFinisher;
  private final Runnable waitForActivitiesToFinishRunnable;

  public ActivityFinisherRunListener(
      Instrumentation instrumentation,
      MonitoringInstrumentation.ActivityFinisher finisher,
      Runnable waitForActivitiesToFinishRunnable) {
    this.instrumentation = checkNotNull(instrumentation);
    activityFinisher = checkNotNull(finisher);
    this.waitForActivitiesToFinishRunnable = checkNotNull(waitForActivitiesToFinishRunnable);
  }

  @Override
  public void testStarted(Description description) throws Exception {
    instrumentation.runOnMainSync(activityFinisher);
    waitForActivitiesToFinishRunnable.run();
  }

  @Override
  public void testFinished(Description description) throws Exception {
    InstrumentationConnection.getInstance().requestRemoteInstancesActivityCleanup();
    instrumentation.runOnMainSync(activityFinisher);
    waitForActivitiesToFinishRunnable.run();
  }
}
