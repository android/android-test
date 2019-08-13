/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.test.services.events.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

/**
 * Clients of an {@link OrchestratorConnection} implementation must implement this interface to
 * receive the {@link #onTestEventClientConnect()} callback from the connection implementation to
 * indicate that the service connection has been successful.
 */
public interface TestEventClientConnectListener {

  /**
   * Called from the connection's {@link
   * android.content.ServiceConnection#onServiceConnected(ComponentName, IBinder)} implementation to
   * indicate that the async {@link OrchestratorConnection#connect(Context)} operation has finished.
   */
  void onTestEventClientConnect();
}
