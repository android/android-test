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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

/**
 * The base class that service connections have to extend. Handles connection to the service proxy
 * and callbacks to the caller. Extended by {@link TestDiscoveryEventServiceConnection} and {@link
 * TestRunEventServiceConnection}.
 */
public class TestEventServiceConnectionBase<T extends IInterface>
    implements TestEventServiceConnection {
  private static final String TAG = "ConnectionBase";

  @NonNull private final TestEventClientConnectListener listener;
  @NonNull private final ServiceFromBinder<T> serviceFromBinder;
  @NonNull private final String serviceName;
  @Nullable private final String servicePackageName;
  @Nullable public T service = null;

  /** An interface to match the signature of {@link IInterface#asBinder()}. */
  public interface ServiceFromBinder<T extends IInterface> {
    T asInterface(IBinder binder);
  }

  private final ServiceConnection connection =
      new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
          TestEventServiceConnectionBase.this.service = serviceFromBinder.asInterface(binder);
          Log.d(TAG, "Connected to " + serviceName);
          // Notify the caller e.g. {@code AndroidJunitRunner} to start instrumentation since
          // service connection succeeded.
          listener.onTestEventClientConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
          service = null;
          Log.d(TAG, "Disconnected from " + serviceName);
        }
      };

  /** Initializes a new Orchestrator service connection. */
  public TestEventServiceConnectionBase(
      @NonNull String serviceName,
      @NonNull ServiceFromBinder<T> serviceFromBinder,
      @NonNull TestEventClientConnectListener listener) {
    checkNotNull(serviceName, "serviceName cannot be null");
    checkNotNull(listener, "listener cannot be null");
    checkNotNull(serviceFromBinder, "serviceFromBinder cannot be null");
    this.serviceName = getServiceNameOnly(serviceName);
    this.servicePackageName = getServicePackage(serviceName);
    this.listener = listener;
    this.serviceFromBinder = serviceFromBinder;
  }

  /** {@inheritDoc} */
  @Override
  public void connect(@NonNull Context context) {
    Intent intent = new Intent(serviceName);
    if (servicePackageName != null) {
      intent.setPackage(servicePackageName);
    }
    if (!context.bindService(intent, connection, Service.BIND_AUTO_CREATE)) {
      throw new IllegalStateException("Cannot connect to " + serviceName);
    }
  }

  /**
   * Splits the package name and service name parts from a string in the format
   * "com.sample.package/.foo.Service". The package name is optional. If the service name starts
   * with '.' then the package name is prepended to get the full service class name.
   */
  @NonNull
  @VisibleForTesting
  static String getServiceNameOnly(@NonNull String serviceName) {
    String[] parts = serviceName.split("/");
    if (parts.length == 2) {
      return parts[1].startsWith(".") ? parts[0] + parts[1] : parts[1];
    } else if (parts.length == 1) {
      return parts[0];
    } else {
      throw new IllegalArgumentException("Invalid serviceName [" + serviceName + "]");
    }
  }

  @Nullable
  @VisibleForTesting
  static String getServicePackage(@NonNull String serviceName) {
    String[] parts = serviceName.split("/");
    return parts.length >= 2 ? parts[0] : null;
  }
}
