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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.test.internal.runner.InstrumentationConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Ensures that no activities are running when a test method starts and that no activities are still
 * running when it ends.
 */
public class ActivityFinisherRunListener extends RunListener {
  private final Instrumentation instrumentation;
  private final NotifyingRunnable activityFinisher;
  private final Runnable waitForActivitiesToStopRunnable;
  private final Handler handler;

  public ActivityFinisherRunListener(
      Instrumentation instrumentation,
      Runnable finisher,
      Runnable waitForActivitiesToStopRunnable) {
    this.instrumentation = checkNotNull(instrumentation);
    this.activityFinisher = new NotifyingRunnable(checkNotNull(finisher));
    this.waitForActivitiesToStopRunnable = checkNotNull(waitForActivitiesToStopRunnable);
    this.handler = new Handler(Looper.getMainLooper());
  }

  @Override
  public void testStarted(Description description) throws Exception {
    runActivityFinisher();
    waitForActivitiesToStopRunnable.run();
  }

  private void runActivityFinisher() throws InterruptedException {
    handler.post(activityFinisher);
    // wait for the finisher to run, but don't wait forever since this will deadlock and timeout
    // the test if main thread is blocked
    if (!activityFinisher.await(2, TimeUnit.SECONDS)) {
      Log.w(
          "AFRunListener",
          "activity finisher did not run within 2 seconds. Is main thread blocked?");
      // remove the finisher to prevent potential test pollution where activities will be finished
      // mid-test
      handler.removeCallbacks(activityFinisher);
    }
  }

  @Override
  public void testFinished(Description description) throws Exception {
    InstrumentationConnection.getInstance().requestRemoteInstancesActivityCleanup();
    runActivityFinisher();
    waitForActivitiesToStopRunnable.run();
  }

  private static class NotifyingRunnable implements Runnable {

    private final Runnable wrappedRunnable;
    private final CountDownLatch latch = new CountDownLatch(1);

    NotifyingRunnable(Runnable wrappedRunnable) {
      this.wrappedRunnable = wrappedRunnable;
    }

    @Override
    public void run() {
      wrappedRunnable.run();
      latch.countDown();
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
      return latch.await(time, unit);
    }
  }
}
