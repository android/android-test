package androidx.test.espresso;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/**
 * Renders a view hierarchy into a human readable string.
 */
public interface ViewHierarchyRenderer {

  String render(@NonNull View rootView, @Nullable ProblemViews problemViews);

  class ProblemViews {
    @NonNull public final String suffix;
    @NonNull public final List<View> views;

    public ProblemViews(@NonNull String suffix, @NonNull List<View> views) {
      this.suffix = suffix;
      this.views = views;
    }
  }
}
