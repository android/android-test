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

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import androidx.test.core.app.DeviceCapture;
import androidx.test.core.graphics.BitmapStorage;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.EspressoException;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.base.ViewHierarchyExceptionHandler.Truncater;
import androidx.test.espresso.internal.inject.TargetContext;
import androidx.test.internal.platform.util.TestOutputEmitter;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.platform.io.PlatformTestStorageRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;

/**
 * Espresso's default {@link FailureHandler}. If this does not fit your needs, feel free to provide
 * your own implementation via Espresso.setFailureHandler(FailureHandler).
 */
public final class DefaultFailureHandler implements FailureHandler {

  private static final AtomicInteger failureCount = new AtomicInteger(0);
  private final List<FailureHandler> handlers = new ArrayList<>();
  private final PlatformTestStorage testStorage;

  public DefaultFailureHandler(@TargetContext Context appContext) {
    this(appContext, PlatformTestStorageRegistry.getInstance());
  }

  @Inject
  DefaultFailureHandler(@TargetContext Context appContext, PlatformTestStorage testStorage) {
    // Adds a chain of exception handlers.
    // Order matters and a matching failure handler in the chain will throw after the exception is
    // handled. Always adds the handler of the child class ahead of its superclasses to make sure
    // the exception is handled by its corresponding handler.
    //
    // The hierarchy of the exception types handled is:
    // NoMatchingViewException -->
    // PerformException ---------> EspressoException
    //                  ---------> Throwable
    // AssertionError ----------->
    this.testStorage = testStorage;
    handlers.add(
        new ViewHierarchyExceptionHandler<>(
            testStorage,
            failureCount,
            NoMatchingViewException.class,
            getNoMatchingViewExceptionTruncater()));
    handlers.add(
        new ViewHierarchyExceptionHandler<>(
            testStorage,
            failureCount,
            AmbiguousViewMatcherException.class,
            getAmbiguousViewMatcherExceptionTruncater()));
    handlers.add(new PerformExceptionHandler(checkNotNull(appContext), PerformException.class));
    // On API 15, junit.framework.AssertionFailedError is not a subclass of AssertionError.
    handlers.add(new AssertionErrorHandler(AssertionFailedError.class, AssertionError.class));
    handlers.add(new EspressoExceptionHandler(EspressoException.class));
    handlers.add(new ThrowableHandler());
  }

  static Truncater<NoMatchingViewException> getNoMatchingViewExceptionTruncater() {
    return (exception, msgLen, viewHierarchyFile) ->
        new NoMatchingViewException.Builder()
            .from(exception)
            .withMaxMsgLen(msgLen)
            .withViewHierarchyFile(viewHierarchyFile)
            .build();
  }

  static Truncater<AmbiguousViewMatcherException> getAmbiguousViewMatcherExceptionTruncater() {
    return (exception, msgLen, viewHierarchyFile) ->
        new AmbiguousViewMatcherException.Builder()
            .from(exception)
            .withMaxMsgLen(msgLen)
            .withViewHierarchyFile(viewHierarchyFile)
            .build();
  }

  @Override
  public void handle(Throwable error, Matcher<View> viewMatcher) {
    int count = failureCount.incrementAndGet();
    TestOutputEmitter.captureWindowHierarchy("explore-window-hierarchy-" + count + ".xml");
    takeScreenshot("view-op-error-" + count);

    // Iterates through the list of handlers to handle the exception, but at most one handler will
    // update the exception and throw at the end of the handling.
    for (FailureHandler handler : handlers) {
      handler.handle(error, viewMatcher);
    }
  }

  private void takeScreenshot(String outputName) {
    try {
      // takeScreenshot only supported on API >= 18
      if (VERSION.SDK_INT >= 18) {
        BitmapStorage.writeToTestStorage(
            DeviceCapture.takeScreenshotNoSync(), testStorage, outputName);
      } else {
        TestOutputEmitter.takeScreenshot(outputName + ".png");
      }
    } catch (RuntimeException | Error | IOException e) {
      Log.w("DefaultFailureHandler", "Failed to take screenshot", e);
    }
  }

  /** Handles failure for given types of exceptions. Does nothing if the types do not match. */
  abstract static class TypedFailureHandler<T> implements FailureHandler {
    private final List<Class<?>> acceptedTypes;

    /**
     * Constructor.
     *
     * @param acceptedTypes accepted types by this failure handler.
     */
    public TypedFailureHandler(Class<?>... acceptedTypes) {
      this.acceptedTypes = checkNotNull(Arrays.asList(acceptedTypes));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void handle(Throwable error, Matcher<View> viewMatcher) {
      if (error != null) {
        for (Class<?> acceptedType : acceptedTypes) {
          if (acceptedType.isInstance(error)) {
            handleSafely((T) error, viewMatcher);
            break;
          }
        }
      }
    }

    abstract void handleSafely(T error, Matcher<View> viewMatcher);
  }
}

