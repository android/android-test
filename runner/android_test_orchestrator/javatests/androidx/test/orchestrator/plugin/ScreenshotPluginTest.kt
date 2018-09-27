/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.orchestrator.plugin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.orchestrator.plugin.proto.ScreenshotPluginConfigProto.ScreenshotPluginConfig
import androidx.test.tools.runner.core.proto.TestArtifactProto.Artifact
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import androidx.test.tools.runner.core.proto.TestResultProto.TestResult
import androidx.test.tools.runner.core.proto.TestStatusProto.TestStatus
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Any
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The class for testing ScreenshotPlugin. */
@RunWith(AndroidJUnit4::class)
class ScreenshotPluginTest : BasePluginTest() {

  private lateinit var screenshotPlugin: ScreenshotPlugin
  private lateinit var pluginConfigBuilder: ScreenshotPluginConfig.Builder
  private lateinit var testCase: androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
  private lateinit var testResult: TestResult

  @Before
  fun setUp() {
    screenshotPlugin = ScreenshotPlugin()
    testCase = TestCase.newBuilder().setTestClass("ClassA")
        .setTestMethod("TestB").build()
  }

  @Test
  fun verifyScreenshotForFailedTestByDefault() {
    // Use default value in ScreenshotPluginConfig
    screenshotPlugin.configure(Any.newBuilder()
        .setValue(ScreenshotPluginConfig.newBuilder().build().toByteString())
        .build())

    // Verify that take screenshot when test is not PASSED.
    testResult = TestResult.newBuilder().setTestStatus(TestStatus.FAILED).build()
    testResult = screenshotPlugin.afterEach(testResult, shellExecutor)
    var artifact = testResult.getOutputArtifactList()
        .filter({ e: Artifact -> e.getMimeType().equals(SCREENSHOT_MIME_TYPE) })
    assertThat(artifact.size).isGreaterThan(0)
  }

  @Test
  fun verifyNoScreenshotForPassedTestByDefault() {
    // Use default value in ScreenshotPluginConfig
    screenshotPlugin.configure(Any.newBuilder()
        .setValue(ScreenshotPluginConfig.newBuilder().build().toByteString())
        .build())

    // Verify that don't take screenshot when test is PASSED.
    testResult = TestResult.newBuilder().setTestStatus(TestStatus.PASSED).build()
    testResult = screenshotPlugin.afterEach(testResult, shellExecutor)
    var artifact = testResult.getOutputArtifactList()
        .filter({ e: Artifact -> e.getMimeType().equals(SCREENSHOT_MIME_TYPE) })
    assertThat(artifact.size).isEqualTo(0)
  }

  @Test
  fun verifyScreenshotForPassedTest() {
    // Turn on screenshot for passed test in ScreenshotPluginConfig
    screenshotPlugin.configure(Any.newBuilder()
        .setValue(ScreenshotPluginConfig.newBuilder()
            .setScreenshotOnPass(true).build().toByteString())
        .build())

    // Verify that take screenshot when test is PASSED.
    testResult = TestResult.newBuilder().setTestStatus(TestStatus.PASSED).build()
    testResult = screenshotPlugin.afterEach(testResult, shellExecutor)
    var artifact = testResult.getOutputArtifactList()
        .filter({ e: Artifact -> e.getMimeType().equals(SCREENSHOT_MIME_TYPE) })
    assertThat(artifact.size).isGreaterThan(0)
  }

  @Test
  fun verifyScreenshotForNotPassedTest() {
    // Turn on screenshot for passed test in ScreenshotPluginConfig
    screenshotPlugin.configure(Any.newBuilder()
        .setValue(ScreenshotPluginConfig.newBuilder()
            .setScreenshotOnPass(true).build().toByteString())
        .build())

    // Verify that take screenshot when test is not PASSED.
    testResult = TestResult.newBuilder().setTestStatus(TestStatus.FAILED).build()
    testResult = screenshotPlugin.afterEach(testResult, shellExecutor)
    var artifact = testResult.getOutputArtifactList()
        .filter({ e: Artifact -> e.getMimeType().equals(SCREENSHOT_MIME_TYPE) })
    assertThat(artifact.size).isGreaterThan(0)
  }
}
