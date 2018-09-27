/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.services.shellexecutor;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/** Main runner class for the Shell Command Executor */
class ShellMain {

  private static final String TAG = "ShellMain";
  private static final ShellCommandExecutorServer server = new ShellCommandExecutorServer();

  private ShellMain() {}

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {

    boolean cleanExit = "1".equals(System.getenv("SM_EXIT"));

    String secretKey = server.start();

    // Append the arguments for the binder key in the second last position, before the
    // instrumentation itself.
    ArrayList<String> argList = new ArrayList<>(Arrays.asList(args).subList(0, args.length - 1));
    Collections.addAll(
        argList, new String[] {"-e", ShellExecSharedConstants.BINDER_KEY, secretKey});
    argList.add(args[args.length - 1]);

    ProcessBuilder pb = new ProcessBuilder(argList);
    final Process p = pb.start();

    FutureTask<Void> stdoutCopier =
        new FutureTask<>(
            new Runnable() {
              @Override
              public void run() {
                byte[] buf = new byte[ShellExecSharedConstants.BUFFER_SIZE];
                InputStream stdout = p.getInputStream();
                while (true) {
                  try {
                    int read = stdout.read(buf);
                    if (read == -1) {
                      System.out.close();
                      return;
                    }
                    System.out.write(buf, 0, read);
                  } catch (IOException e) {
                    Log.e(TAG, "IOException on stdout, terminating", e);
                    return;
                  }
                }
              }
            },
            null);

    FutureTask<Void> stderrCopier =
        new FutureTask<>(
            new Runnable() {
              @Override
              public void run() {
                byte[] buf = new byte[ShellExecSharedConstants.BUFFER_SIZE];
                InputStream stderr = p.getErrorStream();
                while (true) {
                  try {
                    int read = stderr.read(buf);
                    if (read == -1) {
                      System.err.close();
                      return;
                    }
                    System.err.write(buf, 0, read);
                  } catch (IOException e) {
                    Log.e(TAG, "IOException on stderr, terminating", e);
                    return;
                  }
                }
              }
            },
            null);

    Runnable stdinCopier =
        new Runnable() {
          @Override
          public void run() {
            byte[] buf = new byte[ShellExecSharedConstants.BUFFER_SIZE];
            OutputStream stdin = p.getOutputStream();
            while (true) {
              try {
                int read = System.in.read(buf);
                if (read == -1) {
                  stdin.close();
                  return;
                }
                stdin.write(buf, 0, read);
              } catch (IOException e) {
                Log.e(TAG, "IOException on stdin, terminating", e);
                return;
              }
            }
          }
        };

    Thread inputThread = new Thread(stdinCopier);
    inputThread.setDaemon(true);
    inputThread.start();
    Thread outputThread = new Thread(stdoutCopier);
    outputThread.setDaemon(true);
    outputThread.start();

    Thread errThread = new Thread(stderrCopier);
    errThread.setDaemon(true);
    errThread.start();
    int exit = p.waitFor();
    stdoutCopier.get();
    stderrCopier.get();

    if (cleanExit) {
      // Sometimes the atexit handler may block (seems to be around binder thread shutdown).
      // This makes us hang after system.exit and our process sticks around blocking everyone.
      // See b/73514868
      // Let the kernel be our 介錯人
      killProcess(myPid());
    }
    System.exit(exit);
  }
}
