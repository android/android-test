package androidx.test.internal.platform.app;

import android.content.ComponentName;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;

/**
 * Temporary helper class to create an invoker intent.
 *
 * <p>This is a copy of the desugar generated class used in older versions of monitor artifact and is
 * present to maintain backwards compatibility. It can be removed once java8 bytecode is produced,
 * and thus desugaring is no longer necessary.
 */
public abstract class ActivityInvoker$$CC {

  public static Intent getIntentForActivity$$dflt$$(ActivityInvoker invoker, Class activityClass) {
    Intent intent =
        Intent.makeMainActivity(
            new ComponentName(
                InstrumentationRegistry.getInstrumentation().getTargetContext(), activityClass));
    return InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getPackageManager()
                .resolveActivity(intent, 0)
            != null
        ? intent
        : Intent.makeMainActivity(
            new ComponentName(
                InstrumentationRegistry.getInstrumentation().getContext(), activityClass));
  }
}
