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

package androidx.test.runner.screenshot;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.view.View;
import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.Beta;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * The Screenshot instance provides methods to capture a {@link ScreenCapture} during
 * instrumentation tests run on an android device.
 *
 * <p>The Screenshot instance keeps track of a set of {@link ScreenCaptureProcessor}s that will be
 * passed to each {@link ScreenCapture} object when they are created during any test. These {@link
 * ScreenCaptureProcessor}s are capable of processing the {@link ScreenCapture} that was created.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public final class Screenshot {
  private static int androidRuntimeVersion = Build.VERSION.SDK_INT;
  private static UiAutomationWrapper uiWrapper = new UiAutomationWrapper();
  // Set of processors to pass to every ScreenCapture when it is created.
  private static Set<ScreenCaptureProcessor> screenCaptureProcessorSet = new HashSet<>();
  private static TakeScreenshotCallable.Factory takeScreenshotCallableFactory =
      new TakeScreenshotCallable.Factory();

  /**
   * Creates a {@link ScreenCapture} that contains a {@link Bitmap} of the visible screen content
   * for Build.VERSION_CODES.JELLY_BEAN_MR2 and above.
   *
   * <p>The {@link ScreenCapture} that is returned will also contain the set of {@link
   * ScreenCaptureProcessor}s that have been set in this instance.
   *
   * <p>Note: Only use this method if all your tests run on API versions
   * Build.VERSION_CODES.JELLY_BEAN_MR2 or above. If you need to take screenshots on lower API
   * levels, you need to use {@link #capture(Activity)} or {@link #capture(View)} for those
   * versions.
   *
   * @return a {@link ScreenCapture} that contains the bitmap of the visible screen content.
   * @throws IllegalStateException if used on API below Build.VERSION_CODES.JELLY_BEAN_MR2
   * @throws ScreenShotException If there was an error capturing the screenshot
   */
  public static ScreenCapture capture() throws ScreenShotException {
    try {
      return captureImpl(null);
    } catch (NullPointerException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new ScreenShotException(e);
    } catch (InterruptedException e) {
      throw new ScreenShotException(e);
    } catch (ExecutionException e) {
      throw new ScreenShotException(e);
    }
  }

  /**
   * Creates a {@link ScreenCapture} that contains a {@link Bitmap} of the given activity's root
   * {@link View} hierarchy content.
   *
   * <p>The {@link ScreenCapture} that is returned will also contain the set of {@link
   * ScreenCaptureProcessor}s that have been set in this instance.
   *
   * @param activity the {@link Activity} who's root {@link View} will be used to create a {@link
   *     Bitmap}
   * @return a {@link ScreenCapture} that contains the bitmap of the given activity's root {@link
   *     View}.
   * @throws NullPointerException if given activity is null
   * @throws ScreenShotException If there was an error capturing the screenshot
   */
  public static ScreenCapture capture(@NonNull Activity activity) throws ScreenShotException {
    checkNotNull(activity, "activity cannot be null!");
    try {
      return captureImpl(activity.getWindow().getDecorView().getRootView());
    } catch (IOException e) {
      throw new ScreenShotException(e);
    } catch (InterruptedException e) {
      throw new ScreenShotException(e);
    } catch (ExecutionException e) {
      throw new ScreenShotException(e);
    }
  }

  /**
   * Creates a {@link ScreenCapture} that contains a {@link Bitmap} of the given view's hierarchy
   * content.
   *
   * <p>The {@link ScreenCapture} that is returned will also contain the set of {@link
   * ScreenCaptureProcessor}s that have been set in this instance.
   *
   * @param view the {@link View} to create a {@link Bitmap} of
   * @return {@link ScreenCapture} that contains the bitmap of the given view's hierarchy content.
   * @throws NullPointerException if given view is null
   * @throws ScreenShotException If there was an error capturing the screenshot
   */
  public static ScreenCapture capture(@NonNull View view) throws ScreenShotException {
    checkNotNull(view, "view cannot be null!");
    try {
      return captureImpl(view);
    } catch (IOException e) {
      throw new ScreenShotException(e);
    } catch (InterruptedException e) {
      throw new ScreenShotException(e);
    } catch (ExecutionException e) {
      throw new ScreenShotException(e);
    }
  }

  /**
   * Adds the given set of {@link ScreenCaptureProcessor}s to the current set of {@link
   * ScreenCaptureProcessor}s.
   *
   * <p>The current set of {@link ScreenCaptureProcessor}s will be passed to each {@link
   * ScreenCapture} that is created.
   *
   * @param screenCaptureProcessors the set of {@link ScreenCaptureProcessor}s to add
   */
  public static void addScreenCaptureProcessors(
      Set<ScreenCaptureProcessor> screenCaptureProcessors) {
    screenCaptureProcessorSet.addAll(screenCaptureProcessors);
  }

  /**
   * Sets the current set of {@link ScreenCaptureProcessor}s to the given set of {@link
   * ScreenCaptureProcessor}s.
   *
   * <p>The current set of {@link ScreenCaptureProcessor}s will be passed to each {@link
   * ScreenCapture} that is created.
   *
   * @param screenCaptureProcessors the set of {@link ScreenCaptureProcessor}s to use
   */
  public static void setScreenshotProcessors(Set<ScreenCaptureProcessor> screenCaptureProcessors) {
    screenCaptureProcessorSet = screenCaptureProcessors;
  }

  private static ScreenCapture captureImpl(View targetView)
      throws IOException, InterruptedException, ExecutionException {
    Bitmap bitmap;
    if (targetView == null && androidRuntimeVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      bitmap = captureUiAutomatorImpl();
    } else {
      bitmap = captureViewBasedImpl(targetView);
    }
    return new ScreenCapture(bitmap).setProcessors(screenCaptureProcessorSet);
  }

  private static Bitmap captureUiAutomatorImpl() {
    return uiWrapper.takeScreenshot();
  }

  private static Bitmap captureViewBasedImpl(@NonNull final View view)
      throws InterruptedException, ExecutionException {
    checkNotNull(
        view,
        "Taking view based screenshot requires using either takeScreenshot(view) or"
            + " takeScreenshot(activity) where view and activity are non-null.");
    Callable<Bitmap> takeScreenshotCallable = takeScreenshotCallableFactory.create(view);
    FutureTask<Bitmap> task = new FutureTask<>(takeScreenshotCallable);
    // If we already run on the main thread just execute the task
    if (Looper.myLooper() == Looper.getMainLooper()) {
      task.run();
    } else {
      InstrumentationRegistry.getInstrumentation().runOnMainSync(task);
    }
    return task.get(); // Blocks
  }

  @VisibleForTesting
  static void setTakeScreenshotCallableFactory(TakeScreenshotCallable.Factory factory) {
    takeScreenshotCallableFactory = factory;
  }

  @VisibleForTesting
  static void setUiAutomationWrapper(UiAutomationWrapper wrapper) {
    uiWrapper = wrapper;
  }

  @VisibleForTesting
  static void setAndroidRuntimeVersion(int sdkInt) {
    androidRuntimeVersion = sdkInt;
  }

  /** An Exception associated with failing to capture a screenshot. */
  static final class ScreenShotException extends RuntimeException {
    ScreenShotException(Throwable cause) {
      super(cause);
    }
  }
}
