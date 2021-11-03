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
package androidx.test.platform.graphics;

import android.graphics.HardwareRenderer;
import android.os.Build.VERSION;
import android.util.Log;
import androidx.test.internal.platform.reflect.ReflectionException;
import androidx.test.internal.platform.reflect.ReflectiveMethod;

/**
 * Helper class that provides {@link HardwareRenderer#isDrawingEnabled()} and {@link
 * android.graphics.HardwareRenderer#setDrawingEnabled(boolean)} functionality on emulator platforms
 * that backported this functionality from a future android API.
 */
public class HardwareRendererCompat {

  private static final String TAG = "HardwareRendererCompat";

  private static final ReflectiveMethod<Boolean> isDrawingEnabledReflectiveCall =
      new ReflectiveMethod<>("android.graphics.HardwareRenderer", "isDrawingEnabled");

  private static final ReflectiveMethod<Void> setDrawingEnabledReflectiveCall =
      new ReflectiveMethod<>(
          "android.graphics.HardwareRenderer", "setDrawingEnabled", boolean.class);

  private HardwareRendererCompat() {}

  /**
   * Call to {@link HardwareRenderer#isDrawingEnabled()}
   *
   * <p>Will always return true if {@link HardwareRenderer#isDrawingEnabled()} does not exist on
   * this platform.
   */
  public static boolean isDrawingEnabled() {
    if (VERSION.SDK_INT < 30) {
      // unsupported on these apis
      return true;
    }
    try {
      return isDrawingEnabledReflectiveCall.invokeStatic();
    } catch (ReflectionException e) {
      Log.i(
          TAG, "Failed to reflectively call HardwareRenderer#isDrawingEnabled, returning true", e);
      return true;
    }
  }

  /**
   * Call to {@link HardwareRenderer#setDrawingEnabled(boolean renderingEnabled)}
   *
   * <p>Has no effective if this method does not exist on this platform.
   */
  public static void setDrawingEnabled(boolean renderingEnabled) {
    if (VERSION.SDK_INT < 30) {
      // unsupported on these apis
      return;
    }

    try {
      setDrawingEnabledReflectiveCall.invokeStatic(renderingEnabled);
    } catch (ReflectionException e) {
      Log.i(TAG, "Failed to reflectively call HardwareRenderer#setDrawingEnabled, ignoring", e);
    }
  }

}
