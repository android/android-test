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

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * A decorator around a DeviceBroker to collect the exported properties of the
 * devices it creates.
 *
 */
public class BrokeredDeviceExportedPropertyRecorder implements DeviceBroker {

  private final DeviceBroker delegate;
  private Set<Map<String, Object>> exportedProperties;

  /**
   * Decorates a device broker to record the properties of every device it creates.
   *
   * @param delegate the (non-null) broker to delegate to.
   */
  public BrokeredDeviceExportedPropertyRecorder(DeviceBroker delegate) {
    this.delegate = checkNotNull(delegate);
    exportedProperties = Sets.newConcurrentHashSet();
  }

  @Override
  public BrokeredDevice leaseDevice() {
    BrokeredDevice device = delegate.leaseDevice();
    exportedProperties.add(device.getExportedProperties());
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

  public Set<Map<String, Object>> getLeasedDeviceProperties() {
    return exportedProperties;
  }
}
