/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.runner.integrationtests;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FailingTest {

  @Test
  public void testThreadFailure() {
    Assert.fail("throwing on test aka instrumentation thread");
  }

  @Test
  public void runOnMainSyncFailure() {
    // MonitoringInstrumentation.runOnMainSync has special handling to rethrow on test thread
    getInstrumentation()
        .runOnMainSync(() -> Assert.fail("throwing on main thread via runOnMainSync"));
  }

  @Test
  public void looperMainFailure() {
    new Handler(Looper.getMainLooper())
        .post(() -> Assert.fail("throwing on main thread via looper post"));
  }

  @Test
  public void otherThread() throws InterruptedException {
    Thread t = new Thread(() -> Assert.fail("throwing on background thread"));
    t.start();
    t.join();
  }
}
