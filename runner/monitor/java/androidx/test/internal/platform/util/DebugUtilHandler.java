package androidx.test.internal.platform.util;

/**
 * An extension definition for outputting debugging information to the test execution environment.
 *
 * <p>Implementations should be provided via {@link ServiceLoader}
 */
public interface DebugUtilHandler {

  /** Output the stack traces of all running threads back to the execution environment. */
  void dumpThreadStates(String outputName);

  /** Take a screenshot and store it in test outputs */
  boolean takeScreenshot(String outputName);
}
