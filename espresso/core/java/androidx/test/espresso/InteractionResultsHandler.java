/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.espresso;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.test.espresso.remote.NoRemoteEspressoInstanceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utility functions to gather results from local and remote espresso processes.
 *
 * <p>The {@link #gatherAnyResult()} method will block until the first successful interaction
 * response is received or all interactions finished executing.
 *
 * <p>In the case where all interactions fail, InteractionResultHandler will favor any {@link
 * EspressoException} over {@link NoRemoteEspressoInstanceException}. Since local Espresso
 * interaction exception is more useful for the the test author.
 */
@VisibleForTesting
final class InteractionResultsHandler {
  private static final String TAG = "InteractionResultsHandl";
  private static final int LOCAL_OR_REMOTE_ERROR_PRIORITY = Integer.MAX_VALUE;

  private InteractionResultsHandler() {}

  /** Awaits for the 1st meaningful result from the futures and returns it to the caller. */
  static <T> T gatherAnyResult(List<ListenableFuture<T>> tasks) {
    return gatherAnyResult(tasks, MoreExecutors.directExecutor());
  }

  @VisibleForTesting
  static <T> T gatherAnyResult(List<ListenableFuture<T>> tasks, Executor executor) {
    checkNotNull(tasks);
    checkState(!tasks.isEmpty());
    int active = tasks.size();
    final LinkedBlockingQueue<ExecutionResult<T>> resultQ = new LinkedBlockingQueue<>(active);

    for (ListenableFuture<T> t : tasks) {
      final ListenableFuture<T> myTask = t;
      myTask.addListener(
          new Runnable() {
            @Override
            public void run() {
              if (myTask.isCancelled()) {
                return;
              }
              resultQ.offer(adaptResult(myTask));
            }
          },
          executor);
    }

    ExecutionResult<T> bestResult = null;
    try {
      while (true) {
        if (active == 0 || (bestResult != null && bestResult.isPriority())) {
          return finalResult(bestResult);
        }
        ExecutionResult<T> result = resultQ.take();
        active--;
        bestResult = pickResult(bestResult, result);
      }
    } catch (InterruptedException ie) {
      throw new RuntimeException("Interrupted while interacting", ie);
    } finally {
      for (ListenableFuture<T> t : tasks) {
        t.cancel(true);
      }
    }
  }

  private static <T> T finalResult(ExecutionResult<T> result) {
    if (result.isSuccess()) {

      return result.getResult();
    }
    Throwable t = result.getFailure();
    if (t instanceof ExecutionException) {
      Throwable cause = t.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        throw new RuntimeException("Unknown error during interactions", result.getFailure());
      }
    } else if (t instanceof InterruptedException) {
      throw new IllegalStateException("Interrupted while interacting remotely", t);
    } else {
      throw new RuntimeException("Error interacting remotely", t);
    }
  }

  private static <T> ExecutionResult<T> adaptResult(Future<T> task) {
    try {
      checkState(task.isDone());
      return ExecutionResult.success(task.get());
    } catch (ExecutionException ex) {
      return ExecutionResult.error(ex, LOCAL_OR_REMOTE_ERROR_PRIORITY == getPriority(ex));
    } catch (InterruptedException ie) {
      return ExecutionResult.error(ie);
    } catch (RuntimeException re) {
      return ExecutionResult.error(re);
    } catch (Error e) {
      return ExecutionResult.error(e);
    }
  }

  private static <T> ExecutionResult<T> pickResult(ExecutionResult<T> one, ExecutionResult<T> two) {
    if (two == null) {
      return one;
    } else if (one == null) {
      return two;
    }
    if (one.isSuccess()) {
      return one;
    } else if (two.isSuccess()) {
      return two;
    }
    if (getPriority(one.getFailure()) > getPriority(two.getFailure())) {
      return one;
    } else {
      return two;
    }
  }

  /**
   * Returns an integer representing the priority of the given exception, where Integer.MAX_VALUE is
   * the highest priority and Integer.MIN_VALUE is the lowest.
   */
  private static int getPriority(Throwable t) {
    if (null == t) {
      return Integer.MIN_VALUE;
    }
    if (!(t instanceof ExecutionException)) {
      // prefer main flow execution exceptions.
      return Integer.MIN_VALUE + 1;
    }
    if (t.getCause() instanceof NoRemoteEspressoInstanceException) {
      // Local interaction exception should take precedence over NoRemoteEspressoInstanceException
      return 0;
    } else if (t.getCause() instanceof NoActivityResumedException) {
      // Local or remote assertion errors should take precedence over NoActivityResumedException
      return 1;
    } else {
      // Local or remote assertion errors should take precedence over everything else
      return LOCAL_OR_REMOTE_ERROR_PRIORITY; // Integer.MAX_VALUE
    }
  }

  private static class ExecutionResult<T> {
    private final T result;
    private final boolean success;
    private final Throwable failure;
    private final boolean priority;

    private ExecutionResult(T result, boolean success, Throwable failure, boolean priority) {
      this.result = result;
      this.success = success;
      this.failure = failure;
      this.priority = priority;
    }

    public T getResult() {
      checkState(success);
      return result;
    }

    public boolean isPriority() {
      return priority;
    }

    public boolean isSuccess() {
      return success;
    }

    public Throwable getFailure() {
      checkState(!success);
      return failure;
    }

    public static <T> ExecutionResult<T> success(T result) {
      return new ExecutionResult(result, true, null, true);
    }

    public static <T> ExecutionResult<T> error(Throwable error) {
      return error(error, false);
    }

    public static <T> ExecutionResult<T> error(Throwable error, boolean priorityFailure) {
      return new ExecutionResult(null, false, error, priorityFailure);
    }

    @Override
    public String toString() {
      return toStringHelper(this)
          .omitNullValues()
          .add("priority", priority)
          .add("success", success)
          .add("result", result)
          .add("failure", failure)
          .toString();
    }
  }
}
