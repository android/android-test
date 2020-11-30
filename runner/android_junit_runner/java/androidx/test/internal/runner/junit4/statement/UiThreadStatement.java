/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.internal.runner.junit4.statement;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.Looper;
import android.util.Log;
import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/** {@link Statement} that executes a test on the application's main thread (or UI thread). */
public class UiThreadStatement extends Statement {
  private static final String TAG = "UiThreadStatement";

  private final Statement base;

  private final boolean runOnUiThread;

  public UiThreadStatement(Statement base, boolean runOnUiThread) {
    this.base = base;
    this.runOnUiThread = runOnUiThread;
  }

  public boolean isRunOnUiThread() {
    return runOnUiThread;
  }

  @Override
  public void evaluate() throws Throwable {
    if (runOnUiThread) {
      final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
      runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              try {
                base.evaluate();
              } catch (Throwable throwable) {
                exceptionRef.set(throwable);
              }
            }
          });
      Throwable throwable = exceptionRef.get();
      if (throwable != null) {
        throw throwable;
      }
    } else {
      base.evaluate();
    }
  }

  public static boolean shouldRunOnUiThread(FrameworkMethod method) {
    Class<? extends Annotation> deprecatedUiThreadTestClass =
        loadUiThreadClass("android.test.UiThreadTest");
    if (hasAnnotation(method, deprecatedUiThreadTestClass)) {
      return true;
    } else {
      // to avoid circular dependency on Rules module use the class name directly
      @SuppressWarnings("unchecked") // reflection
      Class<? extends Annotation> uiThreadTestClass =
          loadUiThreadClass("androidx.test.annotation.UiThreadTest");
      if (hasAnnotation(method, deprecatedUiThreadTestClass)
          || hasAnnotation(method, uiThreadTestClass)) {
          return true;
        }
    }
    return false;
  }

  private static boolean hasAnnotation(
      FrameworkMethod method, Class<? extends Annotation> annotationClass) {
    return annotationClass != null
        && (method.getAnnotation(annotationClass) != null
            || classHasAnnotation(method, annotationClass));
  }

  private static boolean classHasAnnotation(
      FrameworkMethod method, Class<? extends Annotation> annotationClass) {
    Class<?> declaringClass = method.getDeclaringClass();

    for (Class<?> declaredInterface : declaringClass.getInterfaces()) {
      if (declaredInterface.isAnnotationPresent(annotationClass)) {
        return true;
      }
    }

    while (declaringClass != null) {
      if (declaringClass.isAnnotationPresent(annotationClass)) {
        return true;
      }

      declaringClass = declaringClass.getSuperclass();
    }

    return false;
  }

  private static Class<? extends Annotation> loadUiThreadClass(String className) {
    try {
      return (Class<? extends Annotation>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static void runOnUiThread(final Runnable runnable) throws Throwable {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      Log.w(
          TAG,
          "Already on the UI thread, this method should not be called from the "
              + "main application thread");
      runnable.run();
    } else {
      FutureTask<Void> task = new FutureTask<>(runnable, null);
      getInstrumentation().runOnMainSync(task);
      try {
        task.get();
      } catch (ExecutionException e) {
        // Expose the original exception
        throw e.getCause();
      }
    }
  }
}
