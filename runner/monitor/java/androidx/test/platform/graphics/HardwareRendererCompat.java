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
import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.internal.util.ReflectionUtil;
import androidx.test.internal.util.ReflectionUtil.ReflectionException;
import androidx.test.internal.util.ReflectionUtil.ReflectionParams;

/**
 * Helper class that provides {@link HardwareRenderer#isDrawingEnabled()} and {@link
 * android.graphics.HardwareRenderer#setDrawingEnabled(boolean)} functionality on emulator platforms
 * that backported this functionality from a future android API.
 *
 * <p>This API is currently experimental and subject to change or removal.
 */
@ExperimentalTestApi
public class HardwareRendererCompat {

  private static final String TAG = "HardwareRendererCompat";

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
      return (boolean) ReflectionUtil.callStaticMethod(HardwareRenderer.class, "isDrawingEnabled");
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
      ReflectionUtil.callStaticMethod(
          HardwareRenderer.class,
          "setDrawingEnabled",
          new ReflectionParams(boolean.class, renderingEnabled));
    } catch (ReflectionException e) {
      Log.i(TAG, "Failed to reflectively call HardwareRenderer#setDrawingEnabled, ignoring", e);
    }
  }

  /**
   * Convenience method to set setDrawingEnabled(true) if and only if it is currently disabled.
   *
   * @return the previous isDrawingEnabled() state
   */
  public static boolean enableDrawingIfNecessary() {
    boolean isDrawingEnabled = isDrawingEnabled();
    if (!isDrawingEnabled) {
      HardwareRendererCompat.setDrawingEnabled(true);
    }
    return isDrawingEnabled;
  }
}
