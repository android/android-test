/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.test.espresso.util.concurrent;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A minimal implementation of Guava's ListeningExecutorService.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ListeningExecutorService extends AbstractExecutorService {

  private final ExecutorService delegate;

  public ListeningExecutorService(ExecutorService delegate) {
    this.delegate = delegate;
  }

  @Override
  protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return ListenableFutureTask.create(callable);
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  @Override
  public <T> ListenableFuture<T> submit(Callable<T> remotePerformCallable) {
    return (ListenableFuture<T>) super.submit(remotePerformCallable);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(command);
  }
}
