package androidx.test.internal.runner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.ClassPathScanner.ExternalClassNameFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ExternalClassNameFilterTest {

  @Test
  public void filter_includesExternalClassName() throws Exception {
    ExternalClassNameFilter filter = new ExternalClassNameFilter();
    assertTrue(filter.accept("com.example.MyName"));
  }

  @Test
  public void filter_excludesInnerClassName() throws Exception {
    ExternalClassNameFilter filter = new ExternalClassNameFilter();
    assertFalse(filter.accept("com.example.MyName$Inner"));
  }
}
