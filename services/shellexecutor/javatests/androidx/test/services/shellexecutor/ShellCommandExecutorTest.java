/*
 * Copyright (C) 2021 The Android Open Source Project
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
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link ShellCommandExecutor}. */
@RunWith(AndroidJUnit4.class)
public class ShellCommandExecutorTest {

  private static final ExecutorService executor =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setNameFormat("ShellCommandExecutorTest#%d").build());

  private final ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor(executor);

  @Test
  public void shellCommandExecutorExecute_noTimeout() throws IOException, InterruptedException {
    Map<String, String> env = ImmutableMap.of("name", "Shell Exec");
    CountDownLatch closeLatch = new CountDownLatch(1);
    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream() {
          @Override
          public void close() throws IOException {
            super.close();
            closeLatch.countDown();
          }
        };
    ShellCommand shellCommand =
        new ShellCommand(
            "echo Hello $name",
            /* parameters= */ ImmutableList.of(),
            /* shellEnv */ env,
            /* executeThroughShell= */ true,
            /* timeoutMs */ 0);

    // Execute the command. This is asynchronous and may return before the command finishes.
    shellCommandExecutor.execute(shellCommand, outputStream);

    // Wait for command to finish by monitoring when the output stream gets closed by executor.
    closeLatch.await(1, SECONDS);

    assertThat(outputStream.toString("UTF-8")).containsMatch("Hello Shell Exec");
  }
}
