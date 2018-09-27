/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.orchestrator;

import android.os.Bundle;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;

/** Encapsulates all the logic for receiving callbacks from the app under test. */
class CallbackLogic extends OrchestratorCallback.Stub {
  private static final String TAG = "CallbackLogic";
  private static final Splitter CLASS_METHOD_SPLITTER = Splitter.on('#');

  private final List<String> listOfTests = new ArrayList<>();
  private final Object testLock = new Object();
  // Assigned by the local instrumentation object in the service connection callback thread,
  // read from many ibinder threads.
  private volatile OrchestrationListenerManager listenerManager;

  @Override
  public void addTest(String test) {
    synchronized (testLock) {
      List<String> classAndMethod = CLASS_METHOD_SPLITTER.splitToList(test);
      if (classAndMethod.size() > 1
          && (classAndMethod.get(1).isEmpty() || classAndMethod.get(1).equals("null"))) {
        listOfTests.add(classAndMethod.get(0));
      } else {
        listOfTests.add(test);
      }
    }
  }

  @Override
  public void sendTestNotification(Bundle bundle) {
    synchronized (testLock) {
      Preconditions.checkNotNull(
          listenerManager, "Unable to process test notification. No ListenerManager");
      listenerManager.handleNotification(bundle);
    }
  }

  List<String> provideCollectedTests() {
    synchronized (testLock) {
      return new ArrayList<>(listOfTests);
    }
  }

  void setListenerManager(OrchestrationListenerManager mListenerManager) {
    synchronized (testLock) {
      Preconditions.checkState(null == this.listenerManager, "Listener manager assigned twice.");
      this.listenerManager = Preconditions.checkNotNull(mListenerManager, "Listener manager null");
    }
  }
}
