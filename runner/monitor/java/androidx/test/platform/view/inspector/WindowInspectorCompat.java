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
package androidx.test.platform.view.inspector;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.inspector.WindowInspector;
import androidx.annotation.RestrictTo;
import androidx.test.internal.platform.reflect.ReflectionException;
import androidx.test.internal.platform.reflect.ReflectiveField;
import androidx.test.internal.platform.reflect.ReflectiveMethod;
import androidx.test.internal.util.Checks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compat class that supports {@link android.viee.inspector.WindowInspector} functionality on older
 * Android SDKs.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // currently used by core, consider making public
public class WindowInspectorCompat {

  // type WindowManagerImpl for API < 17
  private static final ReflectiveMethod<Object> getWindowManagerImplReflectiveCall =
      new ReflectiveMethod<>("android.view.WindowManagerImpl", "getDefault");

  // type WindowManagerGlobal
  private static final ReflectiveMethod<Object> getWindowManagerGlobalReflectiveCall =
      new ReflectiveMethod<>("android.view.WindowManagerGlobal", "getInstance");

  private static final ReflectiveField<List<View>> windowViewsReflectiveField =
      new ReflectiveField<>("android.view.WindowManagerGlobal", "mViews");

  private static final ReflectiveField<View[]> windowViewsPreKitkatReflectiveField =
      new ReflectiveField<>("android.view.WindowManagerGlobal", "mViews");

  private static final ReflectiveField<View[]> windowViewsPreJBReflectiveField =
      new ReflectiveField<>("android.view.WindowManagerImpl", "mViews");

  /**
   * Thrown when there is a failure retrieving window views.
   *
   * <p>This should only occur if the device does not support the view retrieval mechanism used on
   * used on APIs < 29, before WindowInspector existed.
   *
   * @hide
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static class ViewRetrievalException extends Exception {

    ViewRetrievalException(Throwable cause) {
      super("failed to retrieve window views", cause);
    }
  }

  private WindowInspectorCompat() {}

  /**
   * Retrieves the list of window views attached to the current process.
   *
   * <p>On APIs 29 and above, this will call through to {@link
   * WindowInspector#getGlobalWindowViews()}. On older APIs, this will make a best effort attempt to
   * retrieve the window views.
   *
   * <p>Must be called from UI thread.
   *
   * @return the list of window Views
   * @throws IllegalStateException if called from a non-UI thread. ViewRetrievalException if views
   *     could not be retrieved.
   */
  public static List<View> getGlobalWindowViews() throws ViewRetrievalException {
    Checks.checkMainThread();

    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      return WindowInspector.getGlobalWindowViews();
    } else {
      try {
        return getViews(getWindowManager());
      } catch (ReflectionException e) {
        throw new ViewRetrievalException(e.getCause());
      }
    }
  }

  private static Object getWindowManager() throws ReflectionException {
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      return getWindowManagerGlobalReflectiveCall.invokeStatic();
    } else {
      return getWindowManagerImplReflectiveCall.invokeStatic();
    }
  }

  private static List<View> getViews(Object windowManagerGlobal) throws ReflectionException {
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      return windowViewsReflectiveField.get(windowManagerGlobal);
    } else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      View[] views = windowViewsPreKitkatReflectiveField.get(windowManagerGlobal);
      return views != null ? Arrays.asList(views) : new ArrayList<>();
    } else {
      View[] views = windowViewsPreJBReflectiveField.get(windowManagerGlobal);
      return views != null ? Arrays.asList(views) : new ArrayList<>();
    }
  }
}
