package androidx.test.internal.platform.app;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Handles default implementation for ActivityInvoker#getIntentForActivity
 *
 * <p>Previous releases of androidx.test:monitor shipped with java 7 bytecode, and used desugaring
 * to convert Java8+ features like default interface method implementations to java7.
 *
 * <p>This meant previous releases of androidx.test:monitor had an effective inter-library
 * dependency on a generated ActivityInvoker$$CC class. Thus retain this class so monitor is
 * compatible with older androidx.test core versions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class ActivityInvoker$$CC {

  private ActivityInvoker$$CC() {}

  public static Intent getIntentForActivity$$dflt$$(ActivityInvoker invoker,
            Class<? extends Activity> activityClass) {
    Intent intent =
        Intent.makeMainActivity(
            new ComponentName(getInstrumentation().getTargetContext(), activityClass));
    if (getInstrumentation().getTargetContext().getPackageManager().resolveActivity(intent, 0)
        != null) {
      return intent;
    }
    return Intent.makeMainActivity(
        new ComponentName(getInstrumentation().getContext(), activityClass));
  }
}
