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

import androidx.test.InstrumentationRegistry
import androidx.test.services.shellexecutor.ShellExecutor
import androidx.test.tools.runner.core.proto.TestCaseProto.TestCase
import java.io.File
import java.io.InputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat

/*
 * Base class for device plugin.
 */
abstract class BasePlugin : DevicePlugin {
  fun runShellCommandSync(
    shellExecutor: ShellExecutor,
    command: String,
    params: List<String> = emptyList()
  ): String {
    try {
      return shellExecutor.executeShellCommandSync(command, params, emptyMap(), false)
    } catch (e: Exception) {
      throw OnDevicePluginException("Failed to execute shell command $command.", e)
    }
  }

  fun runShellCommand(
    shellExecutor: ShellExecutor,
    command: String,
    params: List<String> = emptyList()
  ): InputStream {
    try {
      return shellExecutor.executeShellCommand(command, params, emptyMap(), true)
    } catch (e: Exception) {
      throw OnDevicePluginException("Failed to execute shell command $command.", e)
    }
  }

  /**
   * Create the parent folder and return the file path for the artifact file. If parent folder is
   * not set, use default folder from context {@code InstrumentationRegistry.getContext().filesDir}
   * If the parent folder doesn't exist, calls {@code mkdir} to create it.
   *
   * @param fileDir the parent folder
   * @param testCase the TestCase the artifact file belongs to
   * @param artifactType the type of artifact, e.g, logcat, screenshot, etc
   * @param fileType the type of artifact file, e.g, txt, png, etc
   * @return the file path for the artifact file
   */
  fun prepareArtifactFilePath(
    fileDir: String,
    testCase: TestCase,
    artifactType: String,
    fileType: String
  ): String {
    var parentPath: String
    if (fileDir.isEmpty()) {
      parentPath = InstrumentationRegistry.getContext().filesDir.getAbsolutePath()
    } else {
      parentPath = fileDir
    }

    // Generate a unique file path for the test
    val parentFile = File(parentPath)
    if (!parentFile.exists()) {
      if (!parentFile.mkdirs()) {
        throw OnDevicePluginException("Failed to create folder: $parentPath")
      }
    }

    val fileNameBuilder = StringBuilder()
    with(fileNameBuilder) {
      append(testCase.getTestClass())
      append("-")
      append(testCase.getTestMethod())
      append("-")
      append(artifactType)
      append("-")
      append(SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(Timestamp(System.currentTimeMillis())))
      append(".txt")
    }
    return File(parentPath, fileNameBuilder.toString()).getPath()
  }
}
