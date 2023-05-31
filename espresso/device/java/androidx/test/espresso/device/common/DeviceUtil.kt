/*
 * Copyright (C) 2021 The Android Open Source Project
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

@file:JvmName("DeviceUtil")

package androidx.test.espresso.device.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import java.util.regex.Pattern

/** Collection of utility methods for getting information about the test device. */

/**
 * Detects if the test is running on an emulator or a real device using some heuristics based on the
 * device properties.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun isTestDeviceAnEmulator(): Boolean {
  val qemu: String? = System.getProperty("ro.kernel.qemu", "?")
  return qemu.equals("1") ||
    Build.HARDWARE.contains("goldfish") ||
    Build.HARDWARE.contains("ranchu")
}

/**
 * Detects if the test is running on Robolectric using some heuristics based on the device
 * properties.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun isRobolectricTest(): Boolean {
  return Build.FINGERPRINT.equals("robolectric")
}

/**
 * Returns the API level of the current test device.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun getDeviceApiLevel(): Int {
  return Build.VERSION.SDK_INT
}

/**
 * Maps device state names to identifiers by processing the output of "device_state print-states"
 *
 * The output of "cmd device_state print-states" contains names and identifiers of supported device
 * states, as shown below.
 * [ DeviceState{identifier=1, name='CLOSED'}, DeviceState{identifier=2, name='HALF_OPENED'}, DeviceState{identifier=3, name='OPENED'}, ]
 * This method returns a map where the keys are the device state names and the values are the device
 * state identifiers.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun getMapOfDeviceStateNamesToIdentifiers(): MutableMap<String, String> {
  if (getDeviceApiLevel() < 24) {
    // Executing shell commands requires API 24+. There are no foldable devices with API 23 or
    // below, so return an empty map.
    return mutableMapOf()
  }
  val deviceStateNameToIdentifier: MutableMap<String, String> = mutableMapOf()
  // Regex pattern that matches supported states, e.g. DeviceState{identifier=1, name='CLOSED',
  // app_accessible=true}
  val DEVICE_STATE_PATTERN_EXTRA_PARAMS: Pattern =
    Pattern.compile("DeviceState\\{identifier=(?<identifier>\\d+), name='(?<name>\\w+?)'.+?\\}")
  // Regex pattern that matches supported states, e.g. DeviceState{identifier=1, name='CLOSED'}
  val DEVICE_STATE_PATTERN_NO_EXTRA_PARAMS: Pattern =
    Pattern.compile("DeviceState\\{identifier=(?<identifier>\\d+), name='(?<name>\\w+?)'\\}")
  val supportedDeviceStates = listOf("CLOSED", "HALF_OPENED", "OPENED")

  val printedStates = executeShellCommand("cmd device_state print-states")
  val lines = printedStates.split("\n")
  for (line in lines) {
    val matcherExtraParams = DEVICE_STATE_PATTERN_EXTRA_PARAMS.matcher(line)
    val matcherNoExtraParams = DEVICE_STATE_PATTERN_NO_EXTRA_PARAMS.matcher(line)
    var deviceStateName: String? = null
    var deviceStateIdentifier: String? = null
    if (matcherExtraParams.find()) {
      deviceStateName = matcherExtraParams.group("name")
      deviceStateIdentifier = matcherExtraParams.group("identifier")
    } else if (matcherNoExtraParams.find()) {
      deviceStateName = matcherNoExtraParams.group("name")
      deviceStateIdentifier = matcherNoExtraParams.group("identifier")
    }
    if (
      deviceStateName != null &&
        deviceStateIdentifier != null &&
        deviceStateName in supportedDeviceStates
    ) {
      deviceStateNameToIdentifier.put(deviceStateName, deviceStateIdentifier)
    }
  }
  return deviceStateNameToIdentifier
}

/**
 * Returns a Pair containing the current width and height of the test device in pixels
 *
 * @hide
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@RestrictTo(RestrictTo.Scope.LIBRARY)
fun calculateCurrentDisplayWidthAndHeightPx(): Pair<Int, Int> {
  // "wm size" will output a string with the format
  // "Physical size: WxH
  //  Override size: WxH"
  val output = executeShellCommand("wm size")

  var subStringToFind = "Override size: "
  if (output.contains(subStringToFind)) {
    val displaySizes =
      output.substring(output.indexOf(subStringToFind) + subStringToFind.length).trim().split("x")
    val widthPx = displaySizes.get(0).toInt()
    val heightPx = displaySizes.get(1).toInt()
    return Pair(widthPx, heightPx)
  } else {
    // If the display size has not been overriden, the "wm size" output will only contain physical
    // size
    subStringToFind = "Physical size: "
    val displaySizes =
      output.substring(output.indexOf(subStringToFind) + subStringToFind.length).trim().split("x")
    val widthPx = displaySizes.get(0).toInt()
    val heightPx = displaySizes.get(1).split("\n").get(0).toInt()
    return Pair(widthPx, heightPx)
  }
}
