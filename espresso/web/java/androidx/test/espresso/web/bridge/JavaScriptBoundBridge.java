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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.util.Log;
import android.webkit.JavascriptInterface;
import java.util.ArrayList;
import java.util.List;

/**
 * The actual class injected as a JavascriptInterface in every web view.
 */
final class JavaScriptBoundBridge {
  private static final String TAG = "JS_BRIDGE";
  private final List<Conduit> conduits = new ArrayList<>();

  void addConduit(Conduit conduit) {
    checkNotNull(conduit);
    synchronized (conduits) {
      conduits.add(conduit);
    }
  }

  /**
   * Called via javascript with the results of some computation.
   */
  @JavascriptInterface
  public void setResult(String token, String result) {
    Log.d(TAG, "Token: " + token + " result: " + result);
    synchronized (conduits) {
      for (int i = 0; i < conduits.size(); i++) {
        Conduit conduit = conduits.get(i);
        if (conduit.getToken().equals(token)) {
          conduit.internalGetResult().set(result);
          conduits.remove(i);
          return;
        }
      }
    }
    Log.e(TAG, "UFO result received - token: " + token + " message: " + result);
  }

  @JavascriptInterface
  public void setError(String token, String error) {
    Log.d(TAG, "Token: " + token + " result: " + error);
    synchronized (conduits) {
      for (int i = 0; i < conduits.size(); i++) {
        Conduit conduit = conduits.get(i);
        if (conduit.getToken().equals(token)) {
          conduit.internalGetResult().setException(new RuntimeException(error));
          conduits.remove(i);
          return;
        }
      }
    }
    Log.e(TAG, "UFO error received - token: " + token + " message: " + error);
  }

  @JavascriptInterface
  public void log_i(String tag, String message) {
    Log.i(tag, message);
  }
}
