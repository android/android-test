package androidx.test.internal.runner.junit4.statement;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@UiThreadTest
public class UiThreadTestOnClass {

  @Test
  public void exampleTest1() {}

  @Test
  public void exampleTest2() {}
}
