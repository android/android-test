/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static androidx.test.internal.util.Checks.checkNotNull;
import static com.google.common.base.Throwables.throwIfUnchecked;

import android.util.Log;
import android.view.View;
import androidx.test.espresso.RootViewException;
import androidx.test.espresso.base.DefaultFailureHandler.TypedFailureHandler;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.Matcher;

/**
 * An Espresso failure handler that handles a {@link RootViewException} to dump the full view
 * hierarchy to an artifact file.
 */
class ViewHierarchyExceptionHandler<T extends Throwable & RootViewException>
    extends TypedFailureHandler<T> {
  private static final String TAG = ViewHierarchyExceptionHandler.class.getSimpleName();

  private final PlatformTestStorage testStorage;
  private final AtomicInteger failureCount;

  public ViewHierarchyExceptionHandler(
      PlatformTestStorage testStorage, AtomicInteger failureCount, Class<T> expectedType) {
    super(expectedType);
    this.testStorage = checkNotNull(testStorage);
    this.failureCount = failureCount;
  }

  @Override
  public void handleSafely(T error, Matcher<View> viewMatcher) {
    dumpFullViewHierarchyToFile(error);
    error.setStackTrace(Thread.currentThread().getStackTrace());
    throwIfUnchecked(error);
    throw new RuntimeException(error);
  }

  private void dumpFullViewHierarchyToFile(T error) {
    String viewHierarchyMsg =
        HumanReadables.getViewHierarchyErrorMessage(error.getRootView(), null, "", null);
    String viewHierarchyFile = "view-hierarchy-" + failureCount + ".txt";
    try {
      addOutputFile(viewHierarchyFile, viewHierarchyMsg);
    } catch (IOException e) {
      // Log and ignore.
      Log.w(TAG, "Failed to save the view hierarchy to file " + viewHierarchyFile, e);
    }
  }

  private void addOutputFile(String filename, String content) throws IOException {
    try (OutputStream out = testStorage.openOutputFile(filename)) {
      out.write(content.getBytes());
    }
  }
}
