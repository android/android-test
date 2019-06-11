package androidx.test.internal.platform.util;

import static androidx.test.internal.platform.util.InstrumentationParameterUtil.getTimeoutMillis;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

/** A unit test for {@link InstrumentationParameterUtil}. */
@RunWith(AndroidJUnit4.class)
public class InstrumentationParameterUtilTest {
  @Test
  public void timeout() {
    Bundle setArguments = new Bundle();
    setArguments.putString("timeout_A", "123");
    setArguments.putString("timeout_B", "456");
    setArguments.putString("timeout_C", "0");
    setArguments.putString("timeout_D", "-1");
    setArguments.putString("timeout_E", "-100");
    setArguments.putString("timeout_F", "invalid");

    InstrumentationRegistry.registerInstance(
        InstrumentationRegistry.getInstrumentation(), setArguments);

    final long defaultValue = 999;
    final long infinite = TimeUnit.DAYS.toMillis(1);
    assertThat(getTimeoutMillis("timeout_missing", defaultValue)).isEqualTo(defaultValue);
    assertThat(getTimeoutMillis("timeout_A", defaultValue)).isEqualTo(123);
    assertThat(getTimeoutMillis("timeout_B", defaultValue)).isEqualTo(456);
    assertThat(getTimeoutMillis("timeout_C", defaultValue)).isEqualTo(defaultValue);
    assertThat(getTimeoutMillis("timeout_D", defaultValue)).isEqualTo(infinite);
    assertThat(getTimeoutMillis("timeout_E", defaultValue)).isEqualTo(infinite);
    assertThrows(NumberFormatException.class, () -> getTimeoutMillis("timeout_F", defaultValue));

    // Negative default value means infinite timeout by default.
    assertThat(getTimeoutMillis("timeout_missing", -1)).isEqualTo(infinite);

    // You can't use zero as a default value.
    assertThrows(IllegalArgumentException.class, () -> getTimeoutMillis("timeout_A", 0));
    assertThrows(IllegalArgumentException.class, () -> getTimeoutMillis("timeout_missing", 0));
  }

  // TODO: Remove this method once junit 4.13 stable version is released.
  private static <T extends Throwable> void assertThrows(Class<T> expected, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable actual) {
      assertThat(actual).isInstanceOf(expected);
      return;
    }
    fail();
  }
}
