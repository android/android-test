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

import androidx.test.orchestrator.plugin.proto.ScreenshotPluginConfigProto.ScreenshotPluginConfig
import androidx.test.services.shellexecutor.ShellExecutor
import androidx.test.tools.runner.core.proto.PathProto.Path
import androidx.test.tools.runner.core.proto.TestArtifactProto.Artifact
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import androidx.test.tools.runner.core.proto.TestResultProto.TestResult
import androidx.test.tools.runner.core.proto.TestStatusProto.TestStatus
import androidx.test.tools.runner.core.proto.TestSuiteResultProto.TestSuiteResult
import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException

const val ARTIFACT_TYPE = "screenshot"
const val FILE_TYPE = "png"

/**
 * This plugin takes a screenshot of Android device after a test is completed.
 * It uses adb screencap command to take a screenshot. There's no limitations on API levels.
 *
 * @see <a href="https://developer.android.com/studio/command-line/adb#screencap">adb screencap</a>
 * The screenshot file will be named as TestClass-TestMethod-screenshot-Timestamp.png in png image
 * format. It will be saved in the user specified path on device. If user didn't set the path, it
 * will be saved in the default path from the context
 * {@code InstrumentationRegistry.getContext().filesDir}. E.g,
 * "/data/user/0/androidx.test.orchestrator.plugin.tests/files/"
 * "ClassA-TestB-screenshot-2018_09_24_11_24_38.png"
 */
class ScreenshotPlugin : BasePlugin() {
  private lateinit var pluginConfig: ScreenshotPluginConfig

  /** {@inheritDoc} */
  override fun configure(config: Any) {
    try {
      pluginConfig = ScreenshotPluginConfig.parseFrom(config.getValue())
    } catch (ipbe: InvalidProtocolBufferException) {
      throw OnDevicePluginException("Failed to parse ScreenshotPluginConfig proto.", ipbe)
    }
  }

  /** {@inheritDoc} */
  override fun beforeAll(shellExecutor: ShellExecutor) {}

  /** {@inheritDoc} */
  override fun beforeEach(testCase: TestCase, shellExecutor: ShellExecutor) {}

  /**
   * After each test is completed, run {@code adb screencap} to take screenshot of the device based
   * on the test status. The png image file will be saved in the user specified path or default
   * path. It's the default path from the context
   * {@code InstrumentationRegistry.getContext().filesDir}.
   * E.g, "/data/user/0/androidx.test.orchestrator.plugin.tests/files/"
   */
  override fun afterEach(testResult: TestResult, shellExecutor: ShellExecutor): TestResult {
    if (pluginConfig.getScreenshotOnPass() || testResult.getTestStatus() != TestStatus.PASSED) {
      // Generate a unique file path for the test
      val filePath = prepareArtifactFilePath(pluginConfig.getScreenshotDir(),
          testResult.getTestCase(), ARTIFACT_TYPE, FILE_TYPE)
      // Take Screenshot
      var command = "screencap -p $filePath"
      runShellCommandSync(shellExecutor, command)

      // Set filePath in testResult
      var testResultBuilder = testResult.toBuilder()
      testResultBuilder.addOutputArtifact(Artifact.newBuilder()
          .setDestinationPath(Path.newBuilder().setPath(filePath))
          .setMimeType(SCREENSHOT_MIME_TYPE))
      return testResultBuilder.build()
    }
    return testResult
  }

  /** {@inheritDoc} */
  override fun afterAll(testSuiteResult: TestSuiteResult, shellExecutor: ShellExecutor):
      TestSuiteResult {
    return testSuiteResult
  }
}
