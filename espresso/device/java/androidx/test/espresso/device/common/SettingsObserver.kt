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
package androidx.test.espresso.device.common

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings.System
import java.util.concurrent.CountDownLatch

class SettingsObserver(
  handler: Handler,
  val context: Context,
  val latch: CountDownLatch,
  val settingToObserve: String
) : ContentObserver(handler) {
  fun observe() {
    val resolver: ContentResolver = context.getContentResolver()
    resolver.registerContentObserver(System.getUriFor(settingToObserve), false, this)
  }

  fun stopObserver() {
    val resolver: ContentResolver = context.getContentResolver()
    resolver.unregisterContentObserver(this)
  }

  override fun onChange(selfChange: Boolean) {
    latch.countDown()
  }
}
