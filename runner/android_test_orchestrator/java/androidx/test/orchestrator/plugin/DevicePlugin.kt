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
import com.google.protobuf.Any

/**
 * Device plugin interface for [AndroidTestOrchestrator].
 *
 * Device plugins allow tests authors to setup the test fixture for their test suite, but also
 * individual test cases directly on an Android device.
 */
interface DevicePlugin {
  /**
   * Configuration for Nitrogen Runner. Allows the plugin to configure itself
   */
  fun configure(config: Any)

  /**
   * Called before any of the tests have been run.
   * @param shellExecutor to execute shell commands on device
   */
  fun beforeAll(shellExecutor: ShellExecutor)

  /**
   * Called before a test is executed
   * @param testCase the test case that will be executed
   * @param shellExecutor to execute shell commands on device
   */
  fun beforeEach(testCase: TestCase, shellExecutor: ShellExecutor)

  /**
   * Called after a test was executed
   * @param testResult the test result of the test case
   * @param shellExecutor to execute shell commands on device
   */
  fun afterEach(testResult: TestResult, shellExecutor: ShellExecutor): TestResult

  /**
   * Called after all any of the tests have been run.
   * @param testSuiteResult the test result of the test suite
   * @param shellExecutor to execute shell commands on device
   */
  fun afterAll(testSuiteResult: TestSuiteResult, shellExecutor: ShellExecutor): TestSuiteResult
}
