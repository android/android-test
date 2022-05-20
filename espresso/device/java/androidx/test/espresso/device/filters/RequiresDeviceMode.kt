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

import androidx.test.filters.CustomFilter
import java.lang.annotation.Repeatable

/**
 * Indicates that a specific test should not be run on a device that does not support the provided
 * device mode.
 *
 * <p> This annotation is repeatable. It will be executed only if the test is running on a foldable
 * device that can be set to the all of the provided device modes.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable(RequiresDeviceModes::class)
@CustomFilter(filterClass = RequiresDeviceModeFilter::class)
annotation class RequiresDeviceMode(val mode: Int = 1)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@CustomFilter(filterClass = RequiresDeviceModeFilter::class)
annotation class RequiresDeviceModes(val value: Array<RequiresDeviceMode>)
