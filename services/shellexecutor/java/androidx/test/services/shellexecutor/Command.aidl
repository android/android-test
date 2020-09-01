package androidx.test.services.shellexecutor;

import android.os.ParcelFileDescriptor;

interface Command {
  void execute(String command, in List<String> parameters, in Map shellEnv, boolean executeThroughShell, in ParcelFileDescriptor pfd);

  void executeWithTimeout(String command, in List<String> parameters, in Map shellEnv, boolean executeThroughShell, in ParcelFileDescriptor pfd, long timeoutMs);
}
