/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.web.bridge;

import static androidx.test.internal.util.Checks.checkState;

import android.os.Build;
import android.os.Looper;
import android.util.Log;
import androidx.concurrent.futures.ResolvableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a gateway for Java and Javascript code to communicate to eachother.
 */
public final class JavaScriptBridge {
  private static final AtomicInteger tokenGenerator = new AtomicInteger(0);
  static final String TAG = "JS_BRIDGE";
  static final String JS_BRIDGE_NAME = "__g_wd_jsb";

  private static volatile boolean initialized = false;
  private static JavaScriptBoundBridge boundBridge;

  /**
   * Creates a Conduit object which allows Java to wrap Javascript code within a handler that will
   * forward evaluation results back to the Java process.
   *
   * <p>Conduits can be used for only 1 evaluation. Creating new ones is relatively cheap.
   */
  public static Conduit makeConduit() {
    checkState(initialized, "Install bridge not called!");
    checkState(null != boundBridge, "Bridge not configured; chromium webviews do not need bridge");
    Conduit conduit =
        new Conduit.Builder()
            .withBridgeName(JS_BRIDGE_NAME)
            .withToken(String.valueOf(tokenGenerator.incrementAndGet()))
            .withSuccessMethod("setResult")
            .withErrorMethod("setError")
            .withJsResult(ResolvableFuture.<String>create())
            .build();
    boundBridge.addConduit(conduit);
    return conduit;
  }

  /**
   * Sets up Java / Javascript bridging on every WebView in the app.
   *
   * <p>This method must be called very early (eg: before webviews are loaded in your app).
   * GoogleInstrumentation invokes this method if this library is present on your classpath.
   *
   * <p>This method must be called from the main thread. It'll return immedately if the bridge
   * is already installed.
   */
  public static void installBridge() {
    checkState(Looper.getMainLooper() == Looper.myLooper(), "Must be on main thread!");
    if (initialized) {
      return;
    }
    try {
      if (Build.VERSION.SDK_INT < 19) {
        boundBridge = new AndroidJavaScriptBridgeInstaller().install();
      }
    } catch (JavaScriptBridgeInstallException e) {
      Log.e(TAG, "Unable to bridge web views!", e);
    }
    initialized = true;
  }
}
