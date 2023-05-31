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

@file:JvmName("UiAutomationUtil")

package androidx.test.espresso.device.common

import android.os.Build
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import androidx.test.platform.app.InstrumentationRegistry
import java.nio.charset.Charset

/** Collection of utility methods using UiAutomation APIs */

/**
 * Executes shell command and returns any stdout output
 *
 * @param command the shell command to execute
 * @return stdout output
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun executeShellCommand(command: String): String {
  val parcelFileDescriptor =
    InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command)
  val output: String
  AutoCloseInputStream(parcelFileDescriptor).use { inputStream ->
    output = inputStream.readBytes().toString(Charset.defaultCharset())
  }
  return output
}
