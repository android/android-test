/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.test.espresso.web.util.concurrent;

import static androidx.test.espresso.web.util.concurrent.Futures.getUninterruptibly;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.concurrent.futures.DirectExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class is based on the ListFuture in Guava and to use the CallbackToFutureAdapter instead of
 * the AbstractFuture.
 *
 * <p>Class that implements {@link Futures#allAsList(Collection)} and {@link
 * Futures#successfulAsList(Collection)}. The idea is to create a (null-filled) List and register a
 * listener with each component future to fill out the value in the List when that future completes.
 */
class ListFuture<V> implements ListenableFuture<List<V>> {
  @Nullable List<? extends ListenableFuture<? extends V>> futures;
  @Nullable List<V> values;
  private final boolean allMustSucceed;
  @NonNull private final AtomicInteger remaining;
  @NonNull private final ListenableFuture<List<V>> result;
  CallbackToFutureAdapter.Completer<List<V>> resultNotifier;

  /**
   * Constructor.
   *
   * @param futures all the futures to build the list from
   * @param allMustSucceed whether a single failure or cancellation should propagate to this future
   * @param listenerExecutor used to run listeners on all the passed in futures.
   */
  ListFuture(
      @NonNull List<? extends ListenableFuture<? extends V>> futures,
      boolean allMustSucceed,
      @NonNull Executor listenerExecutor) {
    this.futures = checkNotNull(futures);
    values = new ArrayList<>(futures.size());
    this.allMustSucceed = allMustSucceed;
    remaining = new AtomicInteger(futures.size());
    result =
        CallbackToFutureAdapter.getFuture(
            new CallbackToFutureAdapter.Resolver<List<V>>() {
              @Override
              public Object attachCompleter(
                  @NonNull CallbackToFutureAdapter.Completer<List<V>> completer) {
                checkState(resultNotifier == null, "The result can only set once!");
                resultNotifier = completer;
                return "ListFuture[" + this + "]";
              }
            });

    init(listenerExecutor);
  }

  private void init(@NonNull Executor listenerExecutor) {
    // First, schedule cleanup to execute when the Future is done.
    addListener(
        new Runnable() {
          @Override
          public void run() {
            // By now the mValues array has either been set as the Future's value,
            // or (in case of failure) is no longer useful.
            ListFuture.this.values = null;

            // Let go of the memory held by other mFutures
            ListFuture.this.futures = null;
          }
        },
        DirectExecutor.INSTANCE);

    // Now begin the "real" initialization.

    // Corner case: List is empty.
    if (futures.isEmpty()) {
      resultNotifier.set(new ArrayList<>(values));
      return;
    }

    // Populate the results list with null initially.
    for (int i = 0; i < futures.size(); ++i) {
      values.add(null);
    }

    // Register a listener on each Future in the list to update
    // the state of this future.
    // Note that if all the mFutures on the list are done prior to completing
    // this loop, the last call to addListener() will callback to
    // setOneValue(), transitively call our cleanup listener, and set
    // mFutures to null.
    // We store a reference to mFutures to avoid the NPE.
    List<? extends ListenableFuture<? extends V>> localFutures = futures;
    for (int i = 0; i < localFutures.size(); i++) {
      final ListenableFuture<? extends V> listenable = localFutures.get(i);
      final int index = i;
      listenable.addListener(
          new Runnable() {
            @Override
            public void run() {
              setOneValue(index, listenable);
            }
          },
          listenerExecutor);
    }
  }

  /** Sets the value at the given index to that of the given future. */
  void setOneValue(int index, @NonNull Future<? extends V> future) {
    List<V> localValues = values;
    if (isDone() || localValues == null) {
      // Some other future failed or has been cancelled, causing this one to
      // also be cancelled or have an exception set. This should only happen
      // if mAllMustSucceed is true.
      checkState(allMustSucceed, "Future was done before all dependencies completed");
      return;
    }

    try {
      checkState(future.isDone(), "Tried to set value from future which is not done");
      localValues.set(index, getUninterruptibly(future));
    } catch (CancellationException e) {
      if (allMustSucceed) {
        // Set ourselves as cancelled. Let the input futures keep running
        // as some of them may be used elsewhere.
        // (Currently we don't override interruptTask, so
        // mayInterruptIfRunning==false isn't technically necessary.)
        cancel(false);
      }
    } catch (ExecutionException e) {
      if (allMustSucceed) {
        // As soon as the first one fails, throw the exception up.
        // The mResult of all other inputs is then ignored.
        resultNotifier.setException(e.getCause());
      }
    } catch (RuntimeException e) {
      if (allMustSucceed) {
        resultNotifier.setException(e);
      }
    } catch (Error e) {
      // Propagate errors up ASAP - our superclass will rethrow the error
      resultNotifier.setException(e);
    } finally {
      int newRemaining = remaining.decrementAndGet();
      checkState(newRemaining >= 0, "Less than 0 remaining futures");
      if (newRemaining == 0) {
        localValues = values;
        if (localValues != null) {
          resultNotifier.set(new ArrayList<>(localValues));
        } else {
          checkState(isDone());
        }
      }
    }
  }

  @Override
  public void addListener(@NonNull Runnable listener, @NonNull Executor executor) {
    result.addListener(listener, executor);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (futures != null) {
      for (ListenableFuture<? extends V> f : futures) {
        f.cancel(mayInterruptIfRunning);
      }
    }

    return result.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return result.isCancelled();
  }

  @Override
  public boolean isDone() {
    return result.isDone();
  }

  @Override
  @Nullable
  public List<V> get() throws InterruptedException, ExecutionException {
    callAllGets();

    // This may still block in spite of the calls above, as the listeners may
    // be scheduled for execution in other threads.
    return result.get();
  }

  @Override
  public List<V> get(long timeout, @NonNull TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return result.get(timeout, unit);
  }

  /**
   * Calls the get method of all dependency futures to work around a bug in some ListenableFutures
   * where the listeners aren't called until get() is called.
   */
  private void callAllGets() throws InterruptedException {
    List<? extends ListenableFuture<? extends V>> oldFutures = futures;
    if (oldFutures != null && !isDone()) {
      for (ListenableFuture<? extends V> future : oldFutures) {
        // We wait for a little while for the future, but if it's not done,
        // we check that no other futures caused a cancellation or failure.
        // This can introduce a delay of up to 10ms in reporting an exception.
        while (!future.isDone()) {
          try {
            future.get();
          } catch (Error e) {
            throw e;
          } catch (InterruptedException e) {
            throw e;
          } catch (Throwable e) {
            // ExecutionException / CancellationException / RuntimeException
            if (allMustSucceed) {
              return;
            } else {
              continue;
            }
          }
        }
      }
    }
  }
}
