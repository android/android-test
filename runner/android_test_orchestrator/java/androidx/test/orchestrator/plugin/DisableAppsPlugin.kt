/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.orchestrator.plugin

import android.util.Log
import androidx.test.services.shellexecutor.ShellExecutor
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import androidx.test.tools.runner.core.proto.TestResultProto.TestResult
import androidx.test.tools.runner.core.proto.TestSuiteResultProto.TestSuiteResult
import androidx.test.orchestrator.plugin.proto.DisableAppsPluginConfigProto.DisableAppsPluginConfig
import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException

const val TAG = "DisableAppsPlugin"
/**
 * This plugin disables the listed apps on Android device.
 * It runs {@code adb pm disable} command before all tests.
 * @see <a href="https://developer.android.com/studio/command-line/adb#pm">adb pm</a>
 */
class DisableAppsPlugin : BasePlugin() {
  private lateinit var pluginConfig: DisableAppsPluginConfig

  /** {@inheritDoc} */
  override fun configure(config: Any) {
    try {
      pluginConfig = DisableAppsPluginConfig.parseFrom(config.getValue())
    } catch (ipbe: InvalidProtocolBufferException) {
      throw OnDevicePluginException("Failed to parse DisableAppsPluginConfig proto.", ipbe)
    }
  }

  /**
   * Before all tests, run adb pm disable to disable each listed app.
   */
  override fun beforeAll(shellExecutor: ShellExecutor) {
    for (app in pluginConfig.disabledPackagesList) {
      val params = listOf("disable", "$app")
      val output = runShellCommandSync(shellExecutor, "pm", params)
      when { (output.contains("new state: disabled")) ->
          Log.i(TAG, "Succeeded to disable package: $app")
        else -> Log.e(TAG, "Failed to disable package: $app")
      }
    }
  }

  /** {@inheritDoc} */
  override fun beforeEach(testCase: TestCase, shellExecutor: ShellExecutor) {}

  /** {@inheritDoc} */
  override fun afterEach(testResult: TestResult, shellExecutor: ShellExecutor): TestResult {
    return testResult
  }

  /** {@inheritDoc} */
  override fun afterAll(testSuiteResult: TestSuiteResult, shellExecutor: ShellExecutor):
      TestSuiteResult {
    return testSuiteResult
  }
}
