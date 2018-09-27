/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;


import java.util.Map;

/**
 * Executes an instrumentation prior to making the device avaliable to the caller.
 *
 */
public class InstrumentationExecutingDeviceBrokerDecorator implements DeviceBroker {

  private final DeviceBroker delegate;
  private final Instrumentation instrumentation;

  public InstrumentationExecutingDeviceBrokerDecorator(DeviceBroker delegate,
      Instrumentation instrumentation) {
    this.instrumentation = checkNotNull(instrumentation);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public BrokeredDevice leaseDevice() {
    BrokeredDevice device = delegate.leaseDevice();
    AdbController controller = device.getAdbController();
    controller.startInstrumentation(instrumentation);
    return device;
  }

  @Override
  public void freeDevice(BrokeredDevice device) {
    delegate.freeDevice(device);
  }

  @Override
  public Map<String, Object> getExportedProperties() {
    return delegate.getExportedProperties();
  }
}
