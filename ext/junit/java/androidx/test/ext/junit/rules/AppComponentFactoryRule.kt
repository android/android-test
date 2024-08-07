/*
 * Copyright 2023 The Android Open Source Project
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

import android.app.AppComponentFactory
import android.os.Build
import androidx.test.platform.app.AppComponentFactoryRegistry
import org.junit.Rule
import org.junit.rules.ExternalResource

/**
 * JUnit [Rule] that let you define a [AppComponentFactory] before the tests starts and clean-up
 * the factory after the test.
 *
 * This rule is designed to be used with [ActivityScenarioRule].
 *
 * Example:
 *
 * ```kotlin
 * // We use `order`  to to ensure the `AppComponentFactoryRule` will always run BEFORE the
 * // `ActivityScenarioRule` so that your custom `AppComponentFactory` is available when the
 * // activity is launched.
 * @get:Rule(order = 1)
 * val factoryRule = AppComponentFactoryRule(MyAppComponentFactory())
 *
 * @get:Rule(order = 2)
 * val activityRule = ActivityScenarioRule<MyActivity>()
 * ```
 */
class AppComponentFactoryRule(private val factory: AppComponentFactory) : ExternalResource() {

  override fun before() {
    check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      "AppComponentFactoryRule is not supported on 'VERSION.SDK_INT < VERSION_CODES.P'"
    }
    AppComponentFactoryRegistry.appComponentFactory = factory
  }

  override fun after() {
    AppComponentFactoryRegistry.appComponentFactory = null
  }
}
