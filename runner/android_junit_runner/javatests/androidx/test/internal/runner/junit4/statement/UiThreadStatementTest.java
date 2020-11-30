package androidx.test.internal.runner.junit4.statement;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;

@RunWith(AndroidJUnit4.class)
public final class UiThreadStatementTest {

  @Test
  public void shouldRunOnUiThread_method() throws Exception {
    Method m =
        Class.forName("androidx.test.internal.runner.junit4.statement.UiThreadTestOnMethod")
            .getMethod("exampleUiTest");
    assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isTrue();
  }

  @Test
  public void shouldRunOnUiThread_methodNegativeCase() throws Exception {
    Method m =
        Class.forName("androidx.test.internal.runner.junit4.statement.UiThreadTestOnMethod")
            .getMethod("examplePlainTest");
    assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isFalse();
  }

  @Test
  public void shouldRunOnUiThread_class() throws Exception {
    for (Method m :
        Class.forName("androidx.test.internal.runner.junit4.statement.UiThreadTestOnClass")
            .getDeclaredMethods()) {
      assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isTrue();
    }
  }

  @Test
  public void shouldRunOnUiThread_classNegativeCase() throws Exception {
    for (Method m : getClass().getDeclaredMethods()) {
      assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isFalse();
    }
  }

  @Test
  public void shouldRunOnUiThread_superclass() throws Exception {
    for (Method m :
        Class.forName("androidx.test.internal.runner.junit4.statement.UiThreadTestOnSuperclass")
            .getDeclaredMethods()) {
      assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isTrue();
    }
  }

  @Test
  public void shouldRunOnUiThread_interface() throws Exception {
    for (Method m :
        Class.forName(
                "androidx.test.internal.runner.junit4.statement.UiThreadTestOnImplementedInterface")
            .getDeclaredMethods()) {
      assertThat(UiThreadStatement.shouldRunOnUiThread(new FrameworkMethod(m))).isTrue();
    }
  }
}
