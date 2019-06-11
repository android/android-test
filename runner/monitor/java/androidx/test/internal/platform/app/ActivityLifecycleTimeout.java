package androidx.test.internal.platform.app;

import androidx.test.internal.platform.util.InstrumentationParameterUtil;

/** Timeout for Activity's lifecycle state transitions. */
public class ActivityLifecycleTimeout {

  /** The instrumentation argument key for the activity lifecycle change timeout. */
  private static final String ACTIVITY_LIFECYCLE_CHANGE_TIMEOUT_MILLIS_KEY =
      "activityLifecycleChangeTimeoutMillis";

  /**
   * Default timeout length is 45 seconds. This value is used traditionally and has been chosen
   * heuristically.
   */
  private static final int DEFAULT_ACTIVITY_LIFECYCLE_CHANGE_TIMEOUT_MILLIS = 45000;

  /**
   * Returns the timeout in millisecond which should be used for waiting for Activity's lifecycle
   * state transitions to be completed.
   */
  public static long getMillis() {
    return InstrumentationParameterUtil.getTimeoutMillis(
        ACTIVITY_LIFECYCLE_CHANGE_TIMEOUT_MILLIS_KEY,
        DEFAULT_ACTIVITY_LIFECYCLE_CHANGE_TIMEOUT_MILLIS);
  }
}
