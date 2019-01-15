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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 * Provides a device broker to use in tests.
 *
 * <p>The device broker is a singleton gateway to accessing new devices. It handles the details of
 * starting new emulator instances or managing a pool of physical devices.
 */
public class DeviceBrokerFactory {

  /**
   * The preferred way to get a device broker.
   *
   * @return the device broker currently in use.
   */
  public static synchronized DeviceBroker getInstance(String[] testArgs) {
    return Guice.createInjector(
            Modules.combine(
                new ExecReporterModule(),
                new LocalBrokerModule(),
                new ProxyConfiguringModule(),
                new WrappedBrokerModule(),
                new PortManagerModule(),
                new DeviceBrokerOptions.Builder().withCommandlineArgs(testArgs).build(),
                new EnvironmentModule(),
                new DeviceBrokerModule()))
        .getInstance(DeviceBroker.class);
  }
}
