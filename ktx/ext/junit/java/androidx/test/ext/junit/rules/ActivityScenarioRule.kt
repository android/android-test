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
package androidx.test.ext.junit.rules

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Constructs an [ActivityScenarioRule] of a given class.
 *
 * @param intent an intent to start activity or null to use the default one
 * @param activityOptions an activity options bundle to be passed along with the intent to start
 *        activity
 * @return ActivityScenarioRule which you can use to access to [ActivityScenario] from your tests
 */
inline fun <reified A : Activity> activityScenarioRule(
  intent: Intent? = null,
  activityOptions: Bundle? = null
):
ActivityScenarioRule<A> = when (intent) {
  null -> ActivityScenarioRule(A::class.java, activityOptions)
  else -> ActivityScenarioRule(intent, activityOptions)
}
