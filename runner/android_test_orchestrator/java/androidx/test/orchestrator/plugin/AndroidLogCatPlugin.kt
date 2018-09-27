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

import androidx.test.services.shellexecutor.ShellExecutor
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import androidx.test.tools.runner.core.proto.TestResultProto.TestResult
import androidx.test.tools.runner.core.proto.TestSuiteResultProto.TestSuiteResult
import androidx.test.orchestrator.plugin.proto.AndroidLogCatPluginConfigProto.AndroidLogCatPluginConfig
import com.google.protobuf.Any

/**
 * This plugin provides test authors to retrieve Android device log of a test.
 */
class AndroidLogCatPlugin : DevicePlugin {
  var pluginConfig: AndroidLogCatPluginConfig? = null

  override fun configure(config: Any) {
    pluginConfig = AndroidLogCatPluginConfig.parseFrom(config.getValue())
  }

  // TODO: add unit test after complete the fun
  override fun beforeAll(shellExecutor: ShellExecutor) {
    // Clear log buffer before start LogCat
    shellExecutor.executeShellCommandSync("logcat -c", listOf(), hashMapOf(), false)

    val tmpConfig = pluginConfig
    // Set buffer size
    if (tmpConfig != null && !tmpConfig.getLogBufferSizeKb().isEmpty()) {
      shellExecutor.executeShellCommandSync("logcat -G", listOf(tmpConfig.getLogBufferSizeKb()),
                                            hashMapOf(), false)
    }
  }

  override fun beforeEach(testCase: TestCase, shellExecutor: ShellExecutor) {}

  override fun afterEach(testResult: TestResult, shellExecutor: ShellExecutor): TestResult {
    return testResult
  }

  override fun afterAll(testSuiteResult: TestSuiteResult, shellExecutor: ShellExecutor):
      TestSuiteResult {
    return testSuiteResult
  }
}
