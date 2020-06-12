package androidx.test.internal.runner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.ClassPathScanner.InclusivePackageNamesFilter;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class InclusivePackageNamesFilterTest {

  @Test
  public void filter_includesPackageName() throws Exception {
    InclusivePackageNamesFilter filter =
        new InclusivePackageNamesFilter(Arrays.asList("com.exclude"));
    assertTrue(filter.accept("com.exclude.Excluded"));
  }

  @Test
  public void filter_excludesOtherPackageName() throws Exception {
    InclusivePackageNamesFilter filter =
        new InclusivePackageNamesFilter(Arrays.asList("com.exclude"));
    assertFalse(filter.accept("com.example.MyName"));
  }
}
