package androidx.test;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Placeholder fixture */
@RunWith(AndroidJUnit4.class)
public final class SimpleTest {

  @Test
  public void singleTest() {
    assertThat(2 + 2).isEqualTo(4);
  }

  @Test
  public void singleTes1t() {
    assertThat(2 + 2).isEqualTo(4);
  }

  @Test
  public void singleTest2() {
    assertThat(2 + 2).isEqualTo(4);
  }

  @Test
  public void singleTest3() {
    assertThat(2 + 2).isEqualTo(4);
  }
}
