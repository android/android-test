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

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Extracts ThreadPoolExecutors used by pieces of android.
 *
 * <p>We do some work to ensure that we load the classes containing these thread pools on the main
 * thread, since they may have static initialization that assumes access to the main looper.
 */
@Singleton
final class ThreadPoolExecutorExtractor {
  private static final String ASYNC_TASK_CLASS_NAME = "android.os.AsyncTask";
  private static final String MODERN_ASYNC_TASK_CLASS_NAME =
      "androidx.loader.content.ModernAsyncTask";
  private static final String MODERN_ASYNC_TASK_FIELD_NAME = "THREAD_POOL_EXECUTOR";
  private static final String LEGACY_ASYNC_TASK_FIELD_NAME = "sExecutor";
  private final Handler mainHandler;

  @Inject
  ThreadPoolExecutorExtractor(Looper looper) {
    mainHandler = new Handler(looper);
  }

  @Nullable
  public ThreadPoolExecutor getAsyncTaskThreadPool() {
    FutureTask<ThreadPoolExecutor> getTask = null;
    if (Build.VERSION.SDK_INT < 11) {
      getTask = new FutureTask<ThreadPoolExecutor>(LEGACY_ASYNC_TASK_EXECUTOR);
    } else {
      getTask = new FutureTask<ThreadPoolExecutor>(POST_HONEYCOMB_ASYNC_TASK_EXECUTOR);
    }

    try {
      return runOnMainThread(getTask).get();
    } catch (InterruptedException ie) {
      throw new RuntimeException("Interrupted while trying to get the async task executor!", ie);
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }

  @Nullable
  public ThreadPoolExecutor getCompatAsyncTaskThreadPool() {
    try {
      return runOnMainThread(new FutureTask<>(MODERN_ASYNC_TASK_EXTRACTOR)).get();
    } catch (InterruptedException ie) {
      throw new RuntimeException("Interrupted while trying to get the compat async executor!", ie);
    } catch (ExecutionException ee) {
      throw new RuntimeException(ee.getCause());
    }
  }

  private <T> FutureTask<T> runOnMainThread(final FutureTask<T> futureToRun) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      final CountDownLatch latch = new CountDownLatch(1);
      mainHandler.post(
          new Runnable() {
            @Override
            public void run() {
              try {
                futureToRun.run();
              } finally {
                latch.countDown();
              }
            }
          });
      try {
        latch.await();
      } catch (InterruptedException ie) {
        if (!futureToRun.isDone()) {
          throw new RuntimeException("Interrupted while waiting for task to complete.", ie);
        }
      }
    } else {
      futureToRun.run();
    }

    return futureToRun;
  }

  private static final Callable<ThreadPoolExecutor> MODERN_ASYNC_TASK_EXTRACTOR =
      () -> {
        try {
          Class<?> modernClazz = Class.forName(MODERN_ASYNC_TASK_CLASS_NAME);
          if (modernClazz == null) {
            return null;
          }
          Field executorField = modernClazz.getDeclaredField(MODERN_ASYNC_TASK_FIELD_NAME);
          if (executorField == null) {
            return null;
          }
          executorField.setAccessible(true);
          return (ThreadPoolExecutor) executorField.get(null);
        } catch (ClassNotFoundException cnfe) {
          return null;
        } catch (NoSuchFieldException nsfe) {
          return null;
        }
      };

  private static final Callable<Class<?>> LOAD_ASYNC_TASK_CLASS =
      () -> Class.forName(ASYNC_TASK_CLASS_NAME);

  private static final Callable<ThreadPoolExecutor> LEGACY_ASYNC_TASK_EXECUTOR =
      () -> {
        try {
          Field executorField =
              LOAD_ASYNC_TASK_CLASS.call().getDeclaredField(LEGACY_ASYNC_TASK_FIELD_NAME);
          executorField.setAccessible(true);
          return (ThreadPoolExecutor) executorField.get(null);
        } catch (ClassNotFoundException cnfe) {
          return null;
        } catch (NoSuchFieldException nsfe) {
          return null;
        }
      };

  private static final Callable<ThreadPoolExecutor> POST_HONEYCOMB_ASYNC_TASK_EXECUTOR =
      () -> {
        try {
          Field executorField = LOAD_ASYNC_TASK_CLASS.call().getField(MODERN_ASYNC_TASK_FIELD_NAME);
          return (ThreadPoolExecutor) executorField.get(null);
        } catch (ClassNotFoundException cnfe) {
          return null;
        } catch (NoSuchFieldException nsfe) {
          return null;
        }
      };
}
