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
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * Minimal fork of Guava's ListenableFutureTask to avoid the full dependency.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V> {

  // The execution list to hold our listeners.
  private final ExecutionList executionList = new ExecutionList();

  /**
   * Creates a {@code ListenableFutureTask} that will upon running, execute the given {@code
   * Callable}.
   *
   * @param callable the callable task
   */
  public static <V> ListenableFutureTask<V> create(Callable<V> callable) {
    return new ListenableFutureTask<>(callable);
  }

  protected ListenableFutureTask(Callable<V> callable) {
    super(callable);
  }

  @Override
  public void addListener(Runnable listener, Executor exec) {
    executionList.add(listener, exec);
  }

  /** Internal implementation detail used to invoke the listeners. */
  @Override
  protected void done() {
    executionList.execute();
  }
}
