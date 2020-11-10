/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.services.shellexecutor;

import static com.google.common.truth.Truth.assertThat;

import android.os.RemoteException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link ShellExecutor} api */
@RunWith(AndroidJUnit4.class)
public class ShellExecutorTest {

  private ShellExecutor shellExecutor;

  @Before
  public void initShellExec() {
    this.shellExecutor =
        new ShellExecutorImpl(
            InstrumentationRegistry.getInstrumentation().getContext(),
            InstrumentationRegistry.getArguments().getString(ShellExecSharedConstants.BINDER_KEY));
  }

  @Test
  public void executeShellCommandSync() throws IOException, ClientNotConnected, RemoteException {
    Map<String, String> env = new HashMap<>();
    env.put("name", "Shell Exec");

    String results = shellExecutor.executeShellCommandSync("echo Hello $name", null, env, true);
    assertThat(results).containsMatch("Hello Shell Exec");
  }
}
