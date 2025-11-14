package androidx.test.filters;

import android.os.Build.VERSION;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Objects;
import org.junit.runner.Description;

/**
 * A JUnit {@link Filter} that implements SdkSuppress annotation based filtering.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public final class SdkSuppressFilter extends AbstractFilter {

  @Override
  protected boolean evaluateTest(Description description) {
    final SdkSuppress sdkSuppress = getAnnotationForTest(description);
    if (sdkSuppress != null) {
      if ((VERSION.SDK_INT >= sdkSuppress.minSdkVersion()
              && VERSION.SDK_INT <= sdkSuppress.maxSdkVersion()
              && !isInExcludedSdks(sdkSuppress.excludedSdks()))
          || Objects.equals(sdkSuppress.codeName(), VERSION.CODENAME)) {
        return true; // run the test
      }
      return false; // don't run the test
    }
    return true; // no SdkSuppress, run the test
  }

  @Nullable
  private SdkSuppress getAnnotationForTest(Description description) {
    final SdkSuppress s = description.getAnnotation(SdkSuppress.class);
    if (s != null) {
      return s;
    }
    final Class<?> testClass = description.getTestClass();
    if (testClass != null) {
      return testClass.getAnnotation(SdkSuppress.class);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public String describe() {
    return "skip tests annotated with SdkSuppress if necessary";
  }

  private boolean isInExcludedSdks(int[] excludedSdks) {
    for (int sdk : excludedSdks) {
      if (sdk == VERSION.SDK_INT) {
        return true;
      }
    }
    return false;
  }
}
