/*
 * Copyright 2018 The Android Open Source Project
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
package androidx.test.core.app

import android.app.Activity
import android.arch.lifecycle.Lifecycle.State

/**
 * Launches an activity of a given class and constructs ActivityScenario with the activity. Waits
 * for the lifecycle state transitions to be complete.
 *
 * Normally this would be [State.RESUMED], but may be another state.
 *
 * This method cannot be called from the main thread except in Robolectric tests.
 *
 * @throws AssertionError if the lifecycle state transition never completes within the timeout
 * @return ActivityScenario which you can use to make further state transitions
 */
inline fun <reified A : Activity> launchActivity() = ActivityScenario.launch(A::class.java)
