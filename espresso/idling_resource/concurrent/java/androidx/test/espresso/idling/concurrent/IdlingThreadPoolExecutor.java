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

import android.util.Log;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ThreadPoolExecutor} that can be registered as an {@link IdlingResource} with Espresso.
 *
 * <p>Overrides parent methods to monitor threads starting, and finishing execution. Uses {@link
 * CountingIdlingResource} to track number of active tasks.
 *
 * <p><b>This API is currently in beta.</b>
 */
public class IdlingThreadPoolExecutor extends ThreadPoolExecutor implements IdlingResource {

  private static final String LOG_TAG = "IdlingThreadPoolExec";

  private CountingIdlingResource countingIdlingResource;

  /**
   * Creates a new {@code IdlingThreadPoolExecutor} with the given initial parameters and default
   * rejected execution handler.
   *
   * @param resourceName the name of the executor (used for logging and idempotency of
   *     registration).
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     allowCoreThreadTimeOut is set.
   * @param maximumPoolSize the maximum number of threads to allow in the pool.
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the keepAliveTime argument.
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the Runnable tasks submitted by the execute method.
   * @param threadFactory the factory to use when the executor creates a new thread.
   */
  public IdlingThreadPoolExecutor(
      String resourceName,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    countingIdlingResource = new CountingIdlingResource(resourceName);
    Log.i(LOG_TAG, "Register idling resource for thread pool " + resourceName);
    IdlingRegistry.getInstance().register(this);
  }

  @Override
  public String getName() {
    return countingIdlingResource.getName();
  }

  @Override
  public boolean isIdleNow() {
    return countingIdlingResource.isIdleNow();
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    countingIdlingResource.registerIdleTransitionCallback(callback);
  }

  @Override
  public synchronized void execute(Runnable command) {
    countingIdlingResource.increment();
    super.execute(command);
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    countingIdlingResource.decrement();
  }

  @Override
  protected void terminated() {
    super.terminated();
    Log.i(LOG_TAG, "Thread pool terminated, unregistering " + countingIdlingResource.getName());
    IdlingRegistry.getInstance().unregister(this);
  }
}
