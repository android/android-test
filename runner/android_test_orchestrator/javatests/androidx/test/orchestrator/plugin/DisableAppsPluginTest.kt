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

import com.google.common.truth.Truth.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import androidx.test.tools.runner.core.proto.TestResultProto.TestResult
import androidx.test.orchestrator.plugin.proto.DisableAppsPluginConfigProto.DisableAppsPluginConfig
import com.google.protobuf.Any
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/* The class for testing DisableAppsPlugin. */
@RunWith(AndroidJUnit4::class)
class DisableAppsPluginTest : BasePluginTest() {

  private lateinit var disableAppsPlugin: DisableAppsPlugin
  private lateinit var pluginConfigBuilder: DisableAppsPluginConfig.Builder
  private lateinit var testCase: androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
  private lateinit var testResult: TestResult

  @Before
  fun setUp() {
    disableAppsPlugin = DisableAppsPlugin()
    testCase = TestCase.newBuilder().setTestClass("ClassA")
        .setTestMethod("TestB").build()
  }

  @Test
  fun verifyDisableAppsCorrectly() {
    val params = listOf("list", "packages", "-e")
    val packages = shellExecutor.executeShellCommandSync("pm", params, null, false)
    val firstPackageNameStartIndex = packages.indexOf(":", 0) + 1
    val firstPackageNameEndIndex = packages.indexOf("\n", 0)
    if (firstPackageNameEndIndex > firstPackageNameStartIndex) {
      val app = packages.substring(firstPackageNameStartIndex, firstPackageNameEndIndex)
      disableAppsPlugin.configure(Any.newBuilder()
        .setValue(
          DisableAppsPluginConfig.newBuilder()
            .addDisabledPackages(app).build().toByteString())
        .build())
    disableAppsPlugin.beforeAll(shellExecutor)
    // Verify the app is disabled.
    val params = listOf("list", "packages", "-d")
    val output = shellExecutor.executeShellCommandSync("pm", params, null, false)
    assertThat(output).containsMatch(app)
    }
  }
}
