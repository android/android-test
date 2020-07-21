/*
 * Copyright (C) 2020 The Android Open Source Project
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

package androidx.test.internal.events.client;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Connects to the remote service e.g. {@link
 * androidx.test.services.events.discovery.ITestDiscoveryEvent} or {@link
 * androidx.test.services.events.run.ITestRunEvent} and notifies {@link
 * androidx.test.runner.AndroidJUnitRunner} (the caller) when the connection is established via the
 * {@link TestEventClientConnectListener} interface.
 */
public interface TestEventServiceConnection {

  /**
   * Connects/binds to the service. The connect operation is asynchronous, so the caller needs to
   * wait for {@link TestEventClientConnectListener#onTestEventClientConnect()} before attempting to
   * use the service.
   *
   * @param context the instrumentation {@link Context} to use for binding to the service
   */
  void connect(@NonNull Context context);
}
