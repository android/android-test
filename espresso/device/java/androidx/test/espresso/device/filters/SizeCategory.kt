/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.device.filters

/**
 * Enum for size categories device can fit into. More detailed explanations of these categories can
 * be found at
 * https://developer.android.com/guide/topics/resources/providing-resources#ScreenSizeQualifier.
 */
enum class SizeCategory(val value: Int) {
  // Screens that are of similar size to a low-density QVGA screen. The minimum layout size for a
  // small screen is approximately 320x426 dp units.
  SMALL(0),
  // Screens that are of similar size to a medium-density HVGA screen. The minimum layout size for a
  // normal screen is approximately 320x470 dp units.
  NORMAL(1),
  // Screens that are of similar size to a medium-density VGA screen. The minimum layout size for a
  // large screen is approximately 480x640 dp units.
  LARGE(2),
  // Screens that are considerably larger than the traditional medium-density HVGA screen.
  // The minimum layout size for an xlarge screen is approximately 720x960 dp units.
  XLARGE(3)
}
