/*
 * Copyright (C) 2023 The Android Open Source Project
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

@file:JvmName("AccelerometerRotationUtil")

package androidx.test.espresso.device.common

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
fun getAccelerometerRotationSetting(): AccelerometerRotation =
  if (
    Settings.System.getInt(
      InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
      Settings.System.ACCELEROMETER_ROTATION,
      0
    ) == 1
  ) {
    AccelerometerRotation.ENABLED
  } else {
    AccelerometerRotation.DISABLED
  }

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun setAccelerometerRotationSetting(accelerometerRotation: AccelerometerRotation) {
  val context = InstrumentationRegistry.getInstrumentation().getTargetContext()
  val settingsLatch: CountDownLatch = CountDownLatch(1)
  val thread: HandlerThread = HandlerThread("Observer_Thread")
  thread.start()
  val runnableHandler: Handler = Handler(thread.getLooper())
  val settingsObserver: SettingsObserver =
    SettingsObserver(
      runnableHandler,
      context,
      settingsLatch,
      Settings.System.ACCELEROMETER_ROTATION
    )
  settingsObserver.observe()
  executeShellCommand("settings put system accelerometer_rotation ${accelerometerRotation.value}")
  settingsLatch.await(5, TimeUnit.SECONDS)
  settingsObserver.stopObserver()
  thread.quitSafely()
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
enum class AccelerometerRotation(val value: Int) {
  DISABLED(0),
  ENABLED(1)
}
