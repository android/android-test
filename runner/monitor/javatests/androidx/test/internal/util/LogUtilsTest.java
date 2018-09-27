package androidx.test.internal.util;

import static com.google.common.truth.Truth.assertThat;

import android.util.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

/** Tests for {@link LogUtil} */
@RunWith(RobolectricTestRunner.class)
public class LogUtilsTest {

  @Test
  public void logDebug_isLoggableEnabled() {
    ShadowLog.setLoggable(LogUtilsTest.class.getSimpleName(), Log.DEBUG);
    String message = "Order me a %s";
    LogUtil.logDebug(LogUtilsTest.class.getSimpleName(), message, "latte");
    assertThat(ShadowLog.getLogsForTag(LogUtilsTest.class.getSimpleName()).get(0).msg)
        .contains("Order me a latte");
  }

  @Test
  public void logDebug_isLoggableDisabled() {
    ShadowLog.setLoggable(LogUtilsTest.class.getSimpleName(), Log.INFO);
    String message = "Order me a %s";
    LogUtil.logDebug(LogUtilsTest.class.getSimpleName(), message, "latte");
    assertThat(ShadowLog.getLogsForTag(LogUtilsTest.class.getSimpleName())).isEmpty();
  }

}
