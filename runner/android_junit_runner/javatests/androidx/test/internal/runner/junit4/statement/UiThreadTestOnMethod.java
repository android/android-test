package androidx.test.internal.runner.junit4.statement;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UiThreadTestOnMethod {

  @UiThreadTest
  @Test
  public void exampleUiTest() {}

  @Test
  public void examplePlainTest() {}
}
