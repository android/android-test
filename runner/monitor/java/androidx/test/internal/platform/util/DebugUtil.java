package androidx.test.internal.platform.util;

import androidx.test.internal.platform.ServiceLoaderWrapper;

/** Debugging utilities */
public class DebugUtil {

  // create a handler for DebugUtil calls. By default DebugUtil calls are no-ops
  private static final DebugUtilHandler debugHandler =
      ServiceLoaderWrapper.loadSingleService(
          DebugUtilHandler.class,
          () ->
              new DebugUtilHandler() {
                @Override
                public void dumpThreadStates(String outputName) {}

                @Override
                public boolean takeScreenshot(String outputName) {
                  return false;
                }
              });

  private DebugUtil() {}

  /** Output the stack traces of all running threads back to the execution environment. */
  public static void dumpThreadStates(String outputName) {
    debugHandler.dumpThreadStates(outputName);
  }

  /** Output a screenshot back to the execution environment. */
  public static boolean takeScreenshot(String outputName) {
    return debugHandler.takeScreenshot(outputName);
  }
}
