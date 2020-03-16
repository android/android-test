/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.espresso.idling.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** Unit tests for {@link IdlingThreadPoolExecutor}. */
@RunWith(AndroidJUnit4.class)
public class IdlingThreadPoolExecutorTest {

  private IdlingThreadPoolExecutor executor;
  @Mock private IdlingResource.ResourceCallback mockCallback;

  @Before
  public void setUp() {
    initMocks(this);
    this.executor =
        new IdlingThreadPoolExecutor(
            "resource" /* resourceName */,
            2 /* corePoolSize */,
            25 /* maxPoolSize */,
            5 /* keepAliveTime */,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            Executors.defaultThreadFactory());
    this.executor.registerIdleTransitionCallback(mockCallback);
  }

  @Test
  public void testGetName() {
    assertThat(this.executor.getName()).isEqualTo("resource");
  }

  @Test
  public void testIsIdleNow_initial() {
    assertThat(this.executor.isIdleNow()).isTrue();
  }

  /** Test idle transition for a single runnable. */
  @Test
  public void testIsIdleNow_simpleExecution() {
    LatchRunnable r = new LatchRunnable();
    this.executor.execute(r);
    // idling queue should be marked busy as there is a task scheduled for immediate execution
    assertThat(this.executor.isIdleNow()).isFalse();
    r.waitForExecution();
    assertThat(this.executor.isIdleNow()).isFalse();
    r.release();
    verify(mockCallback, timeout(100)).onTransitionToIdle();
    assertThat(this.executor.isIdleNow()).isTrue();
  }

  /** Test idle transition when there is runnables > thread pool size. */
  @Test
  public void testIsIdleNow_multiExecution() {
    LatchRunnable r1 = new LatchRunnable();
    LatchRunnable r2 = new LatchRunnable();
    LatchRunnable r3 = new LatchRunnable();
    this.executor.execute(r1);
    this.executor.execute(r2);
    this.executor.execute(r3);
    r1.waitForExecution();
    r2.waitForExecution();
    assertThat(this.executor.isIdleNow()).isFalse();
    r1.release();
    r3.waitForExecution();
    r2.release();
    assertThat(this.executor.isIdleNow()).isFalse();
    r3.release();
    verify(mockCallback, timeout(100)).onTransitionToIdle();
    assertThat(this.executor.isIdleNow()).isTrue();
  }

  /** Test idle transition when runnable fails. */
  @Test
  public void testIsIdleNow_executionError() {
    ErrorRunnable r = new ErrorRunnable();
    this.executor.execute(r);
    r.waitForExecution();
    verify(mockCallback, timeout(100)).onTransitionToIdle();
    assertThat(this.executor.isIdleNow()).isTrue();
  }

  /** Test that pool can be terminated while there is outstanding tasks. */
  @Test
  public void testTerminatedWhileActiveTasks() {
    this.executor.execute(new LatchRunnable());
    this.executor.terminated();
  }

  private static class LatchRunnable implements Runnable {

    private CountDownLatch finishedLatch = new CountDownLatch(1);
    private CountDownLatch executingLatch = new CountDownLatch(1);

    @Override
    public void run() {
      executingLatch.countDown();
      awaitLatch(finishedLatch);
    }

    private void awaitLatch(CountDownLatch latch) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("interrupted");
      }
    }

    public void waitForExecution() {
      awaitLatch(executingLatch);
    }

    public void release() {
      finishedLatch.countDown();
    }
  }

  private static class ErrorRunnable implements Runnable {

    private CountDownLatch executingLatch = new CountDownLatch(1);

    @Override
    public void run() {
      executingLatch.countDown();
      throw new RuntimeException();
    }

    private void awaitLatch(CountDownLatch latch) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("interrupted");
      }
    }

    public void waitForExecution() {
      awaitLatch(executingLatch);
    }
  }
}
