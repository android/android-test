package androidx.test.ext.truth.content;

import android.content.Intent;
import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.truth.Correspondence;

/**
 * Collection of {@link com.google.common.truth.Correspondence} helpers for asserting lists of
 * {@link Intent}s.
 *
 * @see {@link com.google.common.truth.IterableSubject#comparingElementsUsing(Correspondence)}
 */
public final class IntentCorrespondences {

  private IntentCorrespondences() {}

  public static Correspondence<Intent, Intent> action() {
    return Correspondence.from(
        (actual, expected) -> Objects.equal(actual.getAction(), expected.getAction()),
        "has getAction() equal to");
  }

  public static Correspondence<Intent, Intent> data() {
    return Correspondence.from(
        (actual, expected) -> Objects.equal(actual.getData(), expected.getData()),
        "has getData() equal to");
  }

  @Beta
  public static Correspondence<Intent, Intent> all(
      final Correspondence<Intent, Intent>... correspondences) {
    StringBuilder combinedString = new StringBuilder();
    for (int i = 0; i < correspondences.length; i++) {
      combinedString.append(correspondences[i]);
      if ((i + 1) < correspondences.length) {
        combinedString.append(" and ");
      }
    }
    return Correspondence.from(
        (actual, expected) -> {
          for (Correspondence<Intent, Intent> innerCorrespondence : correspondences) {
            if (!innerCorrespondence.compare(actual, expected)) {
              return false;
            }
          }
          return true;
        },
        combinedString.toString());
  }
}
