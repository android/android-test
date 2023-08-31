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
package androidx.test.platform.app

import android.app.Activity
import android.app.AppComponentFactory
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RestrictTo

/**
 * An exposed registry instance that holds a reference to an application [AppComponentFactory] which
 * will be used by a test [Instrumentation].
 *
 * [AppComponentFactoryRegistry] is a low level APIs, and is used internally by Android testing
 * frameworks. It is **NOT** designed for direct use by third party clients.
 *
 * TODO(b/275323224): In order to avoid breaking open source, support for
 *  `RoboMonitoringInstrumentation` will be done later, once `AppComponentFactoryRegistry` is
 *  available in a published `androidx.test:monitor` release.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object AppComponentFactoryRegistry {

  /**
   * [AppComponentFactory] to be used by the current test [Instrumentation].
   *
   * @throws [IllegalArgumentException] if set from an Android version smaller than 28.
   */
  @JvmStatic
  @Volatile
  var appComponentFactory: AppComponentFactory? = null
    set(value) {
      check(isVersionCodeAtLeastP()) {
        "AppComponentFactoryRegistry is not supported on 'VERSION.SDK_INT < VERSION_CODES.P'"
      }
      field = value
    }

  /**
   * Returns an instance of an [Application] given a [className] using the registered
   * [AppComponentFactory] as factory to instantiate it.
   *
   * It may return null if:
   * - an instance of [AppComponentFactory] has not been registered with [appComponentFactory].
   * - the registered [AppComponentFactory] can not create an instance of the requested [className].
   * - the current Android [Build.VERSION.SDK_INT] is smaller than [Build.VERSION_CODES.P].
   *
   * This function is a shorthand for getting an instance of the registered factory or null.
   *
   * @see [AppComponentFactory.instantiateApplication]
   */
  @JvmStatic
  fun instantiateApplication(
    cl: ClassLoader,
    className: String,
  ): Application? =
    if (isVersionCodeAtLeastP()) {
      appComponentFactory?.instantiateApplication(cl, className)
    } else {
      null
    }

  /**
   * Returns an instance of an [Activity] given a [className] using the registered
   * [AppComponentFactory] as factory to instantiate it.
   *
   * It may return null if:
   * - an instance of [AppComponentFactory] has not been registered with [appComponentFactory].
   * - the registered [AppComponentFactory] can not create an instance of the requested [className].
   * - the current Android [Build.VERSION.SDK_INT] is smaller than [Build.VERSION_CODES.P].
   *
   * This function is a shorthand for getting an instance of the registered factory or null.
   *
   * @see [AppComponentFactory.instantiateApplication]
   */
  @JvmStatic
  fun instantiateActivity(
    cl: ClassLoader,
    className: String,
    intent: Intent? = null,
  ): Activity? =
    if (isVersionCodeAtLeastP()) {
      appComponentFactory?.instantiateActivity(cl, className, intent)
    } else {
      null
    }

  @ChecksSdkIntAtLeast(api = VERSION_CODES.P)
  private fun isVersionCodeAtLeastP(): Boolean = VERSION.SDK_INT >= VERSION_CODES.P
}
