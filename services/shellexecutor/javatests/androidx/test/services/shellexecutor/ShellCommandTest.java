/*
 * Copyright (C) 2017 The Android Open Source Project
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

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Showcases shell executor functionality. */
@RunWith(AndroidJUnit4.class)
public class ShellCommandTest {

  @Rule
  public ActivityScenarioRule<DummyActivity> activityRule =
      new ActivityScenarioRule<>(DummyActivity.class);

  private static String getSecret() {
    return InstrumentationRegistry.getArguments().getString(ShellExecSharedConstants.BINDER_KEY);
  }

  private static String execShellCommand(
      String command, List<String> params, Map<String, String> env, boolean executeThroughShell)
      throws Exception {
    return ShellCommandClient.execOnServerSync(
        InstrumentationRegistry.getInstrumentation().getContext(),
        getSecret(),
        command,
        params,
        env,
        executeThroughShell,
        0L);
  }

  @Test
  public void testExecuteShellCommand() throws Exception {
    assertThat(execShellCommand("id", null, null, false), containsString("root"));
  }

  @Test
  public void testExecuteShellCommand_twice() throws Exception {
    execShellCommand("id", null, null, false);
    execShellCommand("id", null, null, false);
  }

  @Test
  public void testExecuteShellCommand_setEnvVar() throws Exception {
    Map<String, String> env = new HashMap<>();
    env.put("name", "Mr. Roboto");

    String results = execShellCommand("echo Hello $name", null, env, true);
    assertThat(results, containsString("Hello Mr. Roboto"));
  }

  @Test
  public void testExecuteShellCommand_setParams() throws Exception {
    List<String> params = Lists.newArrayList("-c", "2", "localhost");
    String results = execShellCommand("ping", params, null, false);
    assertThat(results, containsString("127.0.0.1"));
  }

  @Test
  public void testMultipleSimultaneousShellCommands() throws Exception {
    Thread spinlock =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  execShellCommand(
                      "'while [ \"$(getprop testing)\" != \"1\" ] ; do sleep 1; done'",
                      null,
                      null,
                      true);
                } catch (Exception e) {
                  fail("Exception on the spinlock command");
                }
              }
            });

    spinlock.run();
    execShellCommand("setprop testing 1", null, null, true);

    try {
      spinlock.join(5000);
    } catch (InterruptedException e) {
      fail("Spinlock command was unable to complete in 5s");
    }

    // Success!  The set prop 1 command ran while the spinlock was active
    // and spinlock ended without interruption.
  }

  @Test
  public void testLargeFileDump() throws Exception {
    // Dump large amounts of data into the buffer, which is more than the input stream buffer can
    // handle. If the buffer blocks and overflows this test will timeout.

    InputStream stream =
        ShellCommandClient.execOnServer(
            InstrumentationRegistry.getInstrumentation().getContext(),
            getSecret(),
            "dd if=/dev/urandom bs=2048 count=16384",
            null,
            null,
            true,
            0L);

    boolean weReadSomething = false;

    // We're using the async version so we must consume the stream here.
    byte[] buffer = new byte[ShellExecSharedConstants.BUFFER_SIZE];
    while (stream.read(buffer) != -1) {
      // Don't care what the data is, but want to verify we read something before terminating.
      weReadSomething = true;
    }

    assertThat(weReadSomething, is(true));
  }
}
