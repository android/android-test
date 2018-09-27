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

import java.util.Map;

/**
 * Provides tests with access to a device to execute on.
 *
 * The device may be physical or emulated depending on the
 * broker in use. Tests are expected to free their device
 * after using it so that it may be provided to another test.
 *
 */
public interface DeviceBroker {

  /**
   * Provisions a device to the test.
   *
   * This call may block if there are no devices avaliable. The returned device
   * will be cleanly initialized and all relevent packages will have been
   * installed on it.
   *
   * The caller is expected to free the device within a reasonable time.
   *
   * @return a BrokeredDevice.
   */
  public BrokeredDevice leaseDevice();

  /**
   * Returns the device to the broker.
   *
   * After returning the device to the broker do not attempt to connect to it.
   */
  public void freeDevice(BrokeredDevice device);

  /**
   * Returns constant properties about this device broker.
   *
   * This information should be static and not vary between the devices this
   * device broker returns.
   *
   * Test suites will use this to report device broker info into sponge once
   * per test run.
   */
  public Map<String, Object> getExportedProperties();
}
