/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.apps.common.testing.suite.filter;

import static com.google.android.apps.common.testing.suite.filter.AnnotationPredicates.newAnnotationPresentAnywherePredicate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.not;

import com.google.android.apps.common.testing.broker.BrokeredDevice;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Creates a predicate which returns false if a test will execute on an emulator and the test method
 * or class was annotated with RequiresDevice.
 *
 * <p>Otherwise it will always return true (if the anno is not present, or the test is not running
 * on an emulator).
 */
class DeviceTypeFilter implements Function<BrokeredDevice, Predicate<InfoPb>> {
  private static final String REQUIRES_DEVICE_CLASSNAME =
      "com.google.android.apps.common.testing.testrunner.annotations.RequiresDevice";

  @Override
  public Predicate<InfoPb> apply(BrokeredDevice device) {
    checkNotNull(device);
    if (device.getDeviceType() == BrokeredDevice.DeviceType.PHYSICAL) {
      return alwaysTrue();
    } else {
      return not(newAnnotationPresentAnywherePredicate(REQUIRES_DEVICE_CLASSNAME));
    }
  }
}
