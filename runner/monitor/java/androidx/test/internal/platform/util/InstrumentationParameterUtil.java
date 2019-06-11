package androidx.test.internal.platform.util;

import androidx.test.internal.util.Checks;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.TimeUnit;

/** Provides utility functions to retrieve and parse data from instrumentation parameter. */
public class InstrumentationParameterUtil {

  /**
   * Gets a timeout value with a given key to the instrumentation argument bundle.
   *
   * @param key a key for the timeout to be retrieved from the instrumentation argument.
   * @param defaultValue a default timeout to be returned if the timeout is not specified or zero in
   *     the instrumentation argument. You can pass negative value as a defaultValue and it will be
   *     interpreted as infinite timeout. You cannot use zero as a defaultValue.
   * @return the timeout in milliseconds for the given key. The value is always greater than zero.
   */
  public static long getTimeoutMillis(String key, long defaultValue) {
    Checks.checkArgument(defaultValue != 0, "default timeout value cannot be zero");

    long value = Long.parseLong(InstrumentationRegistry.getArguments().getString(key, "0"));
    if (value == 0) {
      value = defaultValue;
    }

    if (value < 0) {
      return TimeUnit.DAYS.toMillis(1);
    } else {
      return value;
    }
  }
}
