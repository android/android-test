package androidx.test.eventto;

import android.view.View;
import androidx.annotation.CheckResult;
import java.time.Duration;
import javax.annotation.CheckReturnValue;
import org.hamcrest.Matcher;

public class Eventto {
  private Eventto() {}

  @CheckReturnValue
  @CheckResult
  public static EventtoViewInteraction onView(Matcher<View> matcher) {
    return new EventtoViewInteraction(matcher);
  }

  public static void setDefaultTimeout(Duration duration) {
    EventtoViewInteraction.setDefaultTimeout(duration);
  }
}
