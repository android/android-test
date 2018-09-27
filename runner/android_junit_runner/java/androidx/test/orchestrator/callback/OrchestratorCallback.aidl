/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.orchestrator.callback;

/**
 * Defines an interface for remote {@link Instrumentation} service to speak to the
 * {@link AndroidTestOrchestrator]
 */
interface OrchestratorCallback {
  /**
   * Remote instrumentations, when given the parameter listTestsForOrchestrator, must add each test
   * they wish executed to AndroidTestOrchestrator before terminating.
   */
  void addTest(in String test);

  /**
   * Remote instrumentations should pass a notification along to AndroidTestOrchestrator whenever they get a
   * notification of test progress.  Use {@link OrchestratorService} constants to determine the notification
   * type.
   */
  void sendTestNotification(in Bundle bundle);
}