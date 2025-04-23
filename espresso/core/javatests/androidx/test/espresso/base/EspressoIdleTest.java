package androidx.test.espresso.base;

import static android.os.Looper.getMainLooper;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.TestLooperManager;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
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

  @Test
  public void onIdle_afterPost_backgroundLooper() {
    HandlerThread ht = new HandlerThread("onIdle_afterPost_backgroundLooper");
    ht.start();
    Looper looper = ht.getLooper();

    try {
      IdlingRegistry.getInstance().registerLooperAsIdlingResource(looper);
      AtomicBoolean wasRun = new AtomicBoolean(false);
      new Handler(looper).post(() -> wasRun.set(true));
      Espresso.onIdle();
      assertThat(wasRun.get()).isTrue();
    } finally {
      IdlingRegistry.getInstance().unregisterLooperAsIdlingResource(looper);
      ht.quit();
    }
  }

  /** Verify TestLooperManager can be used after Espresso idle is run. */
  @Test
  public void onIdle_with_TestLooperManager() {
    assumeTrue(VERSION.SDK_INT >= 36);

    Espresso.onIdle();

    TestLooperManager manager = getInstrumentation().acquireLooperManager(getMainLooper());
    assertThat(manager).isNotNull();
    manager.release();

    Espresso.onIdle();
  }

  @Test
  public void onIdle_backgroundLooper_with_TestLooperManager() throws InterruptedException {
    assumeTrue(VERSION.SDK_INT >= 36);

    HandlerThread ht = new HandlerThread("onIdle_backgroundLooper_with_TestLooperManager");
    ht.start();
    Looper looper = ht.getLooper();

    IdlingRegistry.getInstance().registerLooperAsIdlingResource(looper);
    Espresso.onIdle();
    AtomicBoolean wasRun = new AtomicBoolean(false);
    new Handler(looper).post(() -> wasRun.set(true));
    Espresso.onIdle();
    assertThat(wasRun.get()).isTrue();

    IdlingRegistry.getInstance().unregisterLooperAsIdlingResource(looper);
    Espresso.onIdle();

    // The Looper IdlingResource releases its TestLooperManager asynchronously on
    // the Looper thread. Post and wait for the Looper thread to clear
    CountDownLatch latch = new CountDownLatch(1);
    new Handler(looper).post(() -> latch.countDown());
    latch.await();

    TestLooperManager manager = getInstrumentation().acquireLooperManager(looper);
    assertThat(manager).isNotNull();
    manager.release();
    ht.quit();
  }
}
