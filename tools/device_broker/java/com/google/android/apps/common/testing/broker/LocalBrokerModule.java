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
import com.google.inject.multibindings.MapBinder;

/** Guice bindings for the local device broker. */
final class LocalBrokerModule extends AbstractModule {

  @Override
  protected void configure() {
    MapBinder<DeviceBrokerType, DeviceBroker> mapbinder =
        MapBinder.newMapBinder(binder(), DeviceBrokerType.class, DeviceBroker.class);
    mapbinder.addBinding(DeviceBrokerType.LOCAL_ADB_SERVER).to(LocalAdbServerDeviceBroker.class);
  }
}
