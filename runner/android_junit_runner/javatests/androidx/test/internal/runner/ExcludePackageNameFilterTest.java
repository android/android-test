package androidx.test.internal.runner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.ClassPathScanner.ExcludePackageNameFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ExcludePackageNameFilterTest {

  @Test
  public void filter_excludesPackageName() throws Exception {
    ExcludePackageNameFilter filter = new ExcludePackageNameFilter("com.exclude");
    assertFalse(filter.accept("com.exclude.Excluded"));
  }

  @Test
  public void filter_includesOtherPackageNames() throws Exception {
    ExcludePackageNameFilter filter = new ExcludePackageNameFilter("com.exclude");
    assertTrue(filter.accept("com.example.MyName"));
  }
}
