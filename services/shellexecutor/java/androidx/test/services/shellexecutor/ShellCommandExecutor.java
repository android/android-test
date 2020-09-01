/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.services.shellexecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/** Executor to run shell commands with elevated permissions */
final class ShellCommandExecutor {

  private static final String TAG = "shell_cmd_exec";

  private final ExecutorService executor;

  ShellCommandExecutor(ExecutorService executor) {
    if (executor == null) {
      throw new IllegalArgumentException("You must provide an ExecutorService");
    }
    this.executor = executor;
  }

  private static void debug(String msg, Object... args) {
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, String.format(msg, args));
    }
  }

  public void execute(ShellCommand shellCommand, OutputStream writeStdoutTo) throws IOException {

    List<String> toExecute = new ArrayList<>();

    if (shellCommand.executeThroughShell()) {
      toExecute.add("sh");
      toExecute.add("-c");
    }
    toExecute.add(shellCommand.getCommand());
    debug("Command to execute: %s", shellCommand.getCommand());
    if (shellCommand.getParameters() != null) {
      for (String parameter : shellCommand.getParameters()) {
        debug("Added param: %s", parameter);
        toExecute.add(parameter);
      }
    }

    ProcessBuilder pb = new ProcessBuilder(toExecute);

    if (shellCommand.getShellEnv() != null && shellCommand.getShellEnv().keySet() != null) {
      for (String key : shellCommand.getShellEnv().keySet()) {
        String value = shellCommand.getShellEnv().get(key);
        debug("Set envVar %s:%s", key, value);
        pb.environment().put(key, value);
      }
    }

    pb.redirectErrorStream(true);
    final Process p = pb.start();
    p.getOutputStream().close();
    p.getErrorStream().close();

    // Using a {@link CountDownLatch} since {@code Process.waitFor(timeout, unit)} isn't supported.
    CountDownLatch processDone = new CountDownLatch(1);

    if (shellCommand.getTimeoutMs() > 0L) {
      // Thread waits for the process timeout and then destroys the executed process.
      executor.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                // keep track of overall timeout in case the thread is interrupted
                long timeout = MILLISECONDS.toNanos(shellCommand.getTimeoutMs());
                long remainingTimeMs = timeout;
                long startTime = System.nanoTime();
                while (processDone.getCount() > 0 && remainingTimeMs > 0) {
                  try {
                    // wait on [CountDownLatch]
                    processDone.await(remainingTimeMs, MILLISECONDS);
                  } catch (InterruptedException ie) {
                    // Don't die or rethrow, but do mark the thread as interrupted for other tasks.
                    Thread.currentThread().interrupt();
                  } finally {
                    remainingTimeMs = timeout - (System.nanoTime() - startTime);
                  }
                }
              } finally {
                if (processDone.getCount() > 0) {
                  p.destroy();
                }
              }
            }
          });
    }

    executor.execute(
        new Runnable() {
          @Override
          public void run() {
            try {
              while (true) {
                try {
                  int returnCode = p.waitFor();
                  debug("Process ended with return code %d", returnCode);
                  return;
                } catch (InterruptedException e) {
                  Log.e(TAG, "Process interrupted", e);
                  Thread.currentThread().interrupt();
                }
              }
            } finally {
              // mark subprocess as finished no matter what.
              processDone.countDown();
            }
          }
        });

    executor.execute(
        new Runnable() {
          @Override
          public void run() {
            InputStream stdout = p.getInputStream();
            byte[] buf = new byte[ShellExecSharedConstants.BUFFER_SIZE];

            while (true) {
              try {
                int read = stdout.read(buf);

                if (read == -1) {
                  break;
                }

                writeStdoutTo.write(buf, 0, read);
                writeStdoutTo.flush();
              } catch (IOException e) {
                // A broken pipe exception is quite possible here and not cause for alarm.
                Log.i(TAG, "Writer disconnected, terminating");
                break;
              }
            }

            try {
              writeStdoutTo.close();
            } catch (IOException ioe) {
              Log.w(TAG, "Close threw an exception", ioe);
            }
          }
        });
  }
}
