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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * The base class that service connections have to extend. Handles connection to the service proxy
 * and callbacks to the caller.
 */
public class ConnectionBase<T extends IInterface> implements OrchestratorConnection {
  private static final String TAG = "OrchestratorConnection";

  private final TestEventClientConnectListener listener;
  private final ServiceFromBinder<T> serviceFromBinder;
  private final String serviceName;
  private final String servicePackage;
  @Nullable public T service = null;

  /** An interface to match the signature of {@link IInterface#asBinder()}. */
  public interface ServiceFromBinder<T extends IInterface> {
    T asInterface(IBinder binder);
  }

  private final ServiceConnection connection =
      new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
          ConnectionBase.this.service = serviceFromBinder.asInterface(binder);
          Log.i(TAG, "Connected to " + serviceName);
          // Notify the caller e.g. {@code AndroidJunitRunner} to start instrumentation since
          // service connection succeeded.
          listener.onTestEventClientConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
          service = null;
          Log.i(TAG, "Disconnected from " + serviceName);
        }
      };

  public ConnectionBase(
      @NonNull String serviceName,
      @NonNull String servicePackage,
      @NonNull ServiceFromBinder<T> serviceFromBinder,
      @NonNull TestEventClientConnectListener listener) {
    this.serviceName = serviceName;
    this.servicePackage = servicePackage;
    this.listener = listener;
    this.serviceFromBinder = serviceFromBinder;
  }

  /** {@inheritDoc} */
  @Override
  public void connect(@NonNull Context context) {
    Intent intent = new Intent(serviceName);
    intent.setPackage(servicePackage);
    if (!context.bindService(intent, connection, Service.BIND_AUTO_CREATE)) {
      throw new IllegalStateException("Cannot connect to " + serviceName);
    }
  }
}
