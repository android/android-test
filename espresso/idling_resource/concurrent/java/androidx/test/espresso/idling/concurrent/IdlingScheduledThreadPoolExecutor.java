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
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ScheduledThreadPoolExecutor} that can be registered as an {@link IdlingResource} with
 * Espresso.
 *
 * <p>Overrides parent methods to monitor threads starting, and finishing execution. Uses {@link
 * CountingIdlingResource} to track number of active tasks.
 *
 * <p><b>This API is currently in beta.</b>
 */
public class IdlingScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor
    implements IdlingResource {

  private static final String LOG_TAG = "IdlingThreadPoolExec";

  private CountingIdlingResource countingIdlingResource;

  /**
   * Creates a new {@code IdlingScheduledThreadPoolExecutor} with the given initial parameters.
   *
   * @param resourceName the name of the executor (used for logging and idempotency of
   *     registration).
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     allowCoreThreadTimeOut is set.
   * @param threadFactory the factory to use when the executor creates a new thread.
   */
  public IdlingScheduledThreadPoolExecutor(
      String resourceName, int corePoolSize, ThreadFactory threadFactory) {
    this(resourceName, corePoolSize, threadFactory, false);
  }

  /**
   * Creates a new {@code IdlingScheduledThreadPoolExecutor} with the given initial parameters.
   *
   * @param resourceName the name of the executor (used for logging and idempotency of
   *     registration).
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     allowCoreThreadTimeOut is set.
   * @param threadFactory the factory to use when the executor creates a new thread.
   * @param debugCounting if true increment & decrement calls will print trace information to logs.
   */
  public IdlingScheduledThreadPoolExecutor(
      String resourceName, int corePoolSize, ThreadFactory threadFactory, boolean debugCounting) {
    super(corePoolSize, threadFactory);
    countingIdlingResource = new CountingIdlingResource(resourceName, debugCounting);
    Log.i(LOG_TAG, "Register idling resource for scheduled thread pool " + resourceName);
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
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    countingIdlingResource.increment();
    return super.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    countingIdlingResource.increment();
    return super.schedule(callable, delay, unit);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {}

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
