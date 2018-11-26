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

package androidx.test.internal.runner.intent;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;
import androidx.test.runner.intent.IntentCallback;
import androidx.test.runner.intent.IntentMonitor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Monitors all outgoing {@link Intent}s and signals intent to all registered Callbacks.
 *
 * <p>This intent monitor will be notified by {@link
 * androidx.test.runner.MonitoringInstrumentation} when a new activity was started using
 * {@link Activity#startActivity(Intent)}.
 */
public final class IntentMonitorImpl implements IntentMonitor {

  private static final String TAG = "IntentMonitorImpl";

  // Accessed from any thread. Wrapping callbacks in WeakReference because we don't fully
  // trust the users to remove the callbacks (and we don't want to build up a set of dangling
  // ones as the instrumentation runs).
  List<WeakReference<IntentCallback>> callbacks =
      Collections.synchronizedList(new ArrayList<WeakReference<IntentCallback>>());

  /**
   * Registers a intent callback which is notified if an outgoing intent was detected by {@link
   * androidx.test.runner.MonitoringInstrumentation}.
   */
  @Override
  public void addIntentCallback(@NonNull IntentCallback callback) {
    if (null == callback) {
      throw new NullPointerException("callback cannot be null!");
    }
    boolean needsAdd = true;
    Iterator<WeakReference<IntentCallback>> refIter = callbacks.iterator();
    while (refIter.hasNext()) {
      IntentCallback storedCallback = refIter.next().get();
      if (null == storedCallback) {
        refIter.remove();
      } else if (storedCallback == callback) {
        needsAdd = false;
      }
    }
    if (needsAdd) {
      callbacks.add(new WeakReference<IntentCallback>(callback));
    }
  }

  /** Removes a previously added intent callback. */
  @Override
  public void removeIntentCallback(@NonNull IntentCallback callback) {
    if (null == callback) {
      throw new NullPointerException("callback cannot be null!");
    }
    Iterator<WeakReference<IntentCallback>> refIter = callbacks.iterator();
    while (refIter.hasNext()) {
      IntentCallback storedCallback = refIter.next().get();
      if (null == storedCallback) {
        refIter.remove();
      } else if (storedCallback == callback) {
        refIter.remove();
      }
    }
  }

  /**
   * Signal an incoming {@link Intent} to all registered {@link IntentCallback}s.
   *
   * <p>The callback is invoked with with a full copy of the intent.
   *
   * @param intent the intent to signal
   */
  public void signalIntent(Intent intent) {
    Iterator<WeakReference<IntentCallback>> refIter = callbacks.iterator();
    while (refIter.hasNext()) {
      IntentCallback callback = refIter.next().get();
      if (null == callback) {
        refIter.remove();
      } else {
        try {
          // invoke callback with a copy of the intent (in case the object is mutable).
          callback.onIntentSent(new Intent(intent));
        } catch (RuntimeException e) {
          Log.e(
              TAG,
              String.format(
                  "Callback threw exception! (callback: %s intent: %s)", callback, intent),
              e);
        }
      }
    }
  }
}
