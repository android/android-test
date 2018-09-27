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

import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.services.shellexecutor.ShellExecSharedConstants
import androidx.test.services.shellexecutor.ShellExecutor
import androidx.test.services.shellexecutor.ShellExecutorImpl
import org.junit.Before
import org.junit.runner.RunWith

/** Base class for on-device plugin test. */
@RunWith(AndroidJUnit4::class)
open class BasePluginTest {
  protected lateinit var shellExecutor: ShellExecutor

  @Before
  fun initShellExec() {
    shellExecutor =
        ShellExecutorImpl(
            InstrumentationRegistry.getContext(),
            InstrumentationRegistry.getArguments().getString(ShellExecSharedConstants.BINDER_KEY))
  }
}
