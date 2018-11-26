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

import android.graphics.Bitmap;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/**
 * Captures a {@link Bitmap} of the {@link View} hierarchy.
 *
 * <p>Note: This method of taking screenshots will only capture the content of the {@link View} and
 * will not necessarily represent everything that is on the currently on the screen. Unfortunately,
 * this is the best method to view the content of the screen for API 17 and below. For API 18 and
 * above use {@link Screenshot#capture}
 */
final class TakeScreenshotCallable implements Callable<Bitmap> {
  private static final String TAG = "TakeScreenshotCallable";
  private WeakReference<View> viewRef;

  /** Factory class to create a {@link TakeScreenshotCallable} object. */
  @VisibleForTesting
  static class Factory {
    Callable<Bitmap> create(View view) {
      return new TakeScreenshotCallable(view);
    }
  }

  private TakeScreenshotCallable(View view) {
    this.viewRef = new WeakReference<View>(view);
  }

  @Override
  public Bitmap call() {
    Bitmap bitmap = null;
    viewRef.get().setDrawingCacheEnabled(true);
    try {
      bitmap = Bitmap.createBitmap(viewRef.get().getDrawingCache());
    } catch (OutOfMemoryError omm) {
      Log.e(TAG, "Out of memory exception while trying to take a screenshot.", omm);
    } finally {
      viewRef.get().setDrawingCacheEnabled(false);
    }
    return bitmap;
  }
}
