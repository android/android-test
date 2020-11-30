package androidx.test.internal.runner.junit4.statement;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UiThreadTestOnImplementedInterface implements UiThreadTestInterface {

  @Test
  public void exampleTest1() {}

  @Test
  public void exampleTest2() {}
}
