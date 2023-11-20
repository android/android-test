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

import static androidx.test.internal.util.Checks.checkState;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import androidx.test.espresso.Root;
import androidx.test.platform.view.inspector.WindowInspectorCompat;
import androidx.test.platform.view.inspector.WindowInspectorCompat.ViewRetrievalException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * Provides access to all root views in an application.
 *
 * <p>95% of the time this is unnecessary and we can operate solely on current Activity's root view
 * as indicated by getWindow().getDecorView(). However in the case of popup windows, menus, and
 * dialogs the actual view hierarchy we should be operating on is in another root.
 *
 * <p>Obviously, you need to be on the main thread to use this.
 */
final class RootsOracle implements ActiveRootLister {

  private static final String TAG = RootsOracle.class.getSimpleName();

  private final Looper mainLooper;

  @Inject
  RootsOracle(Looper mainLooper) {
    this.mainLooper = mainLooper;
  }

  @Override
  public List<Root> listActiveRoots() {
    checkState(mainLooper.equals(Looper.myLooper()), "must be called on main thread.");

    try {
      List<Root> roots = new ArrayList<>();
      // return roots in reverse order, to match legacy behavior that assumes
      // window ordering by position
      for (View view : reverse(WindowInspectorCompat.getGlobalWindowViews())) {
        roots.add(
            new Root.Builder()
                .withDecorView(view)
                .withWindowLayoutParams((LayoutParams) view.getLayoutParams())
                .build());
      }
      // return an immutable list
      return ImmutableList.copyOf(roots);
    } catch (ViewRetrievalException e) {
      Log.w(TAG, "Failed to retrieve root views", e);
      return ImmutableList.of();
    }
  }

  private List<View> reverse(List<View> globalWindowViews) {
    Collections.reverse(globalWindowViews);
    return globalWindowViews;
  }
}
