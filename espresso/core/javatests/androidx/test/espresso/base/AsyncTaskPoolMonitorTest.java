/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.Suppress;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link AsyncTaskPoolMonitor} */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AsyncTaskPoolMonitorTest {

  private final ThreadPoolExecutor testThreadPool =
      new ThreadPoolExecutor(4, 4, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  private AsyncTaskPoolMonitor monitor = new AsyncTaskPoolMonitor(testThreadPool);

  @After
  public void tearDown() throws Exception {
    testThreadPool.shutdownNow();
  }

  @Test
  public void isIdle_onEmptyPool() throws Exception {
    assertTrue(monitor.isIdleNow());
    final AtomicBoolean isIdle = new AtomicBoolean(false);
    // since we're already idle, this should be ran immedately on our thread.
    monitor.notifyWhenIdle(
        new Runnable() {
          @Override
          public void run() {
            isIdle.set(true);
          }
        });
    assertTrue(isIdle.get());
  }

  @Test
  public void isIdle_withRunningTask() throws Exception {
    final CountDownLatch runLatch = new CountDownLatch(1);
    @SuppressWarnings({"unused", "nullness"}) // go/futurereturn-lsc
    Future<?> possiblyIgnoredError =
        testThreadPool.submit(
            new Runnable() {
              @Override
              public void run() {
                runLatch.countDown();
                try {
                  Thread.sleep(50000);
                } catch (InterruptedException ie) {
                  throw new RuntimeException(ie);
                }
              }
            });
    assertTrue(runLatch.await(1, TimeUnit.SECONDS));
    assertFalse(monitor.isIdleNow());

    final AtomicBoolean isIdle = new AtomicBoolean(false);
    monitor.notifyWhenIdle(
        new Runnable() {
          @Override
          public void run() {
            isIdle.set(true);
          }
        });
    // runnable shouldn't be run ever..
    assertFalse(isIdle.get());
  }

  // TODO(b/68003948): flaky
  @Suppress
  @Test
  public void idleNotificationAndRestart() throws Exception {

    FutureTask<Thread> workerThreadFetchTask =
        new FutureTask<Thread>(
            new Callable<Thread>() {
              @Override
              public Thread call() {
                return Thread.currentThread();
              }
            });
    @SuppressWarnings({"unused", "nullness"}) // go/futurereturn-lsc
    Future<?> possiblyIgnoredError = testThreadPool.submit(workerThreadFetchTask);

    Thread workerThread = workerThreadFetchTask.get();

    final CountDownLatch runLatch = new CountDownLatch(1);
    final CountDownLatch exitLatch = new CountDownLatch(1);

    @SuppressWarnings({"unused", "nullness"}) // go/futurereturn-lsc
    Future<?> possiblyIgnoredError1 =
        testThreadPool.submit(
            new Runnable() {
              @Override
              public void run() {
                runLatch.countDown();
                try {
                  exitLatch.await();
                } catch (InterruptedException ie) {
                  throw new RuntimeException(ie);
                }
              }
            });

    assertTrue(runLatch.await(1, TimeUnit.SECONDS));
    final CountDownLatch notificationLatch = new CountDownLatch(1);
    monitor.notifyWhenIdle(
        new Runnable() {
          @Override
          public void run() {
            notificationLatch.countDown();
          }
        });
    // give some time for the idle detection threads to spin up.
    Thread.sleep(2000);
    // interrupt one of them
    workerThread.interrupt();
    Thread.sleep(1000);
    // unblock the dummy work item.
    exitLatch.countDown();
    assertTrue(notificationLatch.await(1, TimeUnit.SECONDS));
    assertTrue(monitor.isIdleNow());
  }

  // TODO(b/68003948): flaky
  @Suppress
  @Test
  public void idleNotification_extraWork() throws Exception {
    final CountDownLatch firstRunLatch = new CountDownLatch(1);
    final CountDownLatch firstExitLatch = new CountDownLatch(1);

    @SuppressWarnings({"unused", "nullness"}) // go/futurereturn-lsc
    Future<?> possiblyIgnoredError =
        testThreadPool.submit(
            new Runnable() {
              @Override
              public void run() {
                firstRunLatch.countDown();
                try {
                  firstExitLatch.await();
                } catch (InterruptedException ie) {
                  throw new RuntimeException(ie);
                }
              }
            });

    assertTrue(firstRunLatch.await(1, TimeUnit.SECONDS));

    final CountDownLatch notificationLatch = new CountDownLatch(1);
    monitor.notifyWhenIdle(
        new Runnable() {
          @Override
          public void run() {
            notificationLatch.countDown();
          }
        });

    final CountDownLatch secondRunLatch = new CountDownLatch(1);
    final CountDownLatch secondExitLatch = new CountDownLatch(1);
    @SuppressWarnings({"unused", "nullness"}) // go/futurereturn-lsc
    Future<?> possiblyIgnoredError1 =
        testThreadPool.submit(
            new Runnable() {
              @Override
              public void run() {
                secondRunLatch.countDown();
                try {
                  secondExitLatch.await();
                } catch (InterruptedException ie) {
                  throw new RuntimeException(ie);
                }
              }
            });

    assertFalse(notificationLatch.await(10, TimeUnit.MILLISECONDS));
    firstExitLatch.countDown();
    assertFalse(notificationLatch.await(500, TimeUnit.MILLISECONDS));
    secondExitLatch.countDown();
    assertTrue(notificationLatch.await(1, TimeUnit.SECONDS));
    assertTrue(monitor.isIdleNow());
  }
}
