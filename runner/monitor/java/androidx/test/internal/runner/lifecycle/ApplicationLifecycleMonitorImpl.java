/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.internal.runner.lifecycle;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Application;
import android.util.Log;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.lifecycle.ApplicationLifecycleMonitor;
import androidx.test.runner.lifecycle.ApplicationStage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Implementation of a ApplicationLifecycleMonitor */
public class ApplicationLifecycleMonitorImpl implements ApplicationLifecycleMonitor {

  private static final String TAG = "ApplicationLifecycleMonitorImpl";

  // Accessed from any thread.
  private final List<WeakReference<ApplicationLifecycleCallback>> callbacks = new ArrayList<>();

  @Override
  public void addLifecycleCallback(ApplicationLifecycleCallback callback) {
    // there will never be too many callbacks, so iterating over a list will probably
    // be faster then the constant time costs of setting up and maintaining a map.
    checkNotNull(callback);

    synchronized (callbacks) {
      boolean needsAdd = true;
      Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ApplicationLifecycleCallback storedCallback = refIter.next().get();
        if (null == storedCallback) {
          refIter.remove();
        } else if (storedCallback == callback) {
          needsAdd = false;
        }
      }
      if (needsAdd) {
        callbacks.add(new WeakReference<>(callback));
      }
    }
  }

  @Override
  public void removeLifecycleCallback(ApplicationLifecycleCallback callback) {
    checkNotNull(callback);

    synchronized (callbacks) {
      Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ApplicationLifecycleCallback storedCallback = refIter.next().get();
        if (null == storedCallback) {
          refIter.remove();
        } else if (storedCallback == callback) {
          refIter.remove();
        }
      }
    }
  }

  public void signalLifecycleChange(Application app, ApplicationStage stage) {
    synchronized (callbacks) {
      Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = callbacks.iterator();
      while (refIter.hasNext()) {
        ApplicationLifecycleCallback callback = refIter.next().get();
        if (null == callback) {
          refIter.remove();
        } else {
          try {
            Log.d(TAG, "running callback: " + callback);
            callback.onApplicationLifecycleChanged(app, stage);
            Log.d(TAG, "callback completes: " + callback);
          } catch (RuntimeException re) {
            Log.e(
                TAG,
                String.format(
                    "Callback threw exception! (callback: %s stage: %s)", callback, stage),
                re);
          }
        }
      }
    }
  }
}
