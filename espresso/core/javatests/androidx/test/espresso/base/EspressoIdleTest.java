package androidx.test.espresso.base;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;

import android.os.Handler;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

/** A simple integration test for Espresso's main looper integration. */
@RunWith(AndroidJUnit4.class)
public class EspressoIdleTest {

  @Test
  public void onIdle_whenIdle() {
    Espresso.onIdle();
  }

  @Test
  public void onIdle_afterPost() {
    AtomicBoolean wasRun = new AtomicBoolean(false);
    new Handler(getMainLooper()).post(() -> wasRun.set(true));
    Espresso.onIdle();
    assertThat(wasRun.get()).isTrue();
  }

  @Test
  public void onIdle_afterPostShortDelay() {
    AtomicBoolean wasRun = new AtomicBoolean(false);
    new Handler(getMainLooper())
        .postDelayed(() -> wasRun.set(true), Interrogator.LOOKAHEAD_MILLIS - 1);
    Espresso.onIdle();
    assertThat(wasRun.get()).isTrue();
  }

  @Test
  public void onIdle_afterPostLongDelay() {
    AtomicBoolean wasRun = new AtomicBoolean(false);
    new Handler(getMainLooper()).postDelayed(() -> wasRun.set(true), 10000);
    Espresso.onIdle();
    assertThat(wasRun.get()).isFalse();
  }
}
