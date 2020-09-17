package androidx.test.espresso.util;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.ViewHierarchyRenderer;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Locale;

import static androidx.test.espresso.util.TreeIterables.depthFirstViewTraversalWithDistance;

public class DefaultViewHierarchyRenderer implements ViewHierarchyRenderer {
  @Override public String render(@NonNull View rootView, @Nullable ProblemViews problemViews) {
    Iterable<String> lines = Iterables.transform(
        depthFirstViewTraversalWithDistance(rootView),
        viewAndDistance -> {
          String formatString = "+%s%s ";
          if (problemViews != null && problemViews.views.contains(
              viewAndDistance.getView())) {
            formatString += problemViews.suffix;
          }
          formatString += "\n|";

          return String.format(
              Locale.ROOT,
              formatString,
              Strings.padStart(">", viewAndDistance.getDistanceFromRoot() + 1, '-'),
              HumanReadables.describe(viewAndDistance.getView()));
        });
    return Joiner.on("\n").join(lines);
  }
}
