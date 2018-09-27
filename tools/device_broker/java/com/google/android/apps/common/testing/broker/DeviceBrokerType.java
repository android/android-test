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

/**
 * An enumeration of types of device brokers.
 *
 * Different device brokers have different strategies for provisioning devices.
 * All device brokers obey the same contract on provisioned devices.
 *
 */
public enum DeviceBrokerType {

  /**
   * Launches emulator start scripts created by the create_emulator_image build rule.
   */
  WRAPPED_EMULATOR,

  /**
   * Assumes that the user has started an adb server locally and uses the devices
   * attached to that server to run tests.
   */
  LOCAL_ADB_SERVER,

  /**
   * Takes a list of network addresses and attempts to connect to those devices for test execution.
   */
  NETWORKED_DEVICES,

  /**
   * Allocates a lab device from a Zerg server.
   */
  ZERG_LAB,
}
