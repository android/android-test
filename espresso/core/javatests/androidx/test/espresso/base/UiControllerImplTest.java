/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.base;

import static junit.framework.Assert.assertTrue;
import static kotlin.collections.CollectionsKt.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.test.espresso.IdlingResourceTimeoutException;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.tracing.Tracing;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link UiControllerImpl}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class UiControllerImplTest {

  private static final String TAG = UiControllerImplTest.class.getSimpleName();

  private final AtomicReference<UiControllerImpl> uiController = new AtomicReference<>();

  private LooperThread testThread;
  private ThreadPoolExecutor asyncPool;
  private IdlingResourceRegistry idlingResourceRegistry;

  private static class LooperThread extends Thread {
    private final CountDownLatch init = new CountDownLatch(1);
    private Handler handler;
    private Looper looper;

    @Override
    public void run() {
      Looper.prepare();
      handler = new Handler();
      looper = Looper.myLooper();
      init.countDown();
      Looper.loop();
    }

    public void quitLooper() {
      looper.quit();
    }

    public Looper getLooper() {
      try {
        init.await();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return looper;
    }

    public Handler getHandler() {
      try {
        init.await();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return handler;
    }
  }

  @Before
  public void setUp() throws Exception {
    testThread = new LooperThread();
    testThread.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, "Looper died: ", ex);
          }
        });
    testThread.start();
    idlingResourceRegistry =
        new IdlingResourceRegistry(testThread.getLooper(), Tracing.getInstance());
    asyncPool =
        new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    EventInjector injector = null;
    if (Build.VERSION.SDK_INT > 15) {
      InputManagerEventInjectionStrategy strat = new InputManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    } else {
      WindowManagerEventInjectionStrategy strat = new WindowManagerEventInjectionStrategy();
      strat.initialize();
      injector = new EventInjector(strat);
    }

    uiController.set(
        new UiControllerImpl(
            injector,
            new AsyncTaskPoolMonitor(asyncPool).asIdleNotifier(),
            new NoopRunnableIdleNotifier(),
            new Provider<IdleNotifier<IdleNotificationCallback>>() {
              @Override
              public IdleNotifier<IdleNotificationCallback> get() {
                return idlingResourceRegistry.asIdleNotifier();
              }
            },
            testThread.getLooper(),
            idlingResourceRegistry));
  }

  @After
  public void tearDown() throws Exception {
    testThread.quitLooper();
    asyncPool.shutdown();
  }

  @Test
  public void loopMainThreadTillIdle_sendsMessageToRightHandler() {
    final CountDownLatch latch = new CountDownLatch(3);
    testThread.getHandler(); // blocks till initialized;
    final Handler firstHandler =
        new Handler(
            testThread.looper,
            new Handler.Callback() {
              private boolean counted = false;

              @Override
              public boolean handleMessage(Message me) {
                if (counted) {
                  fail("Called 2x!!!!");
                }
                counted = true;
                latch.countDown();
                return true;
              }
            });

    final Handler secondHandler =
        new Handler(
            testThread.looper,
            new Handler.Callback() {
              private boolean counted = false;

              @Override
              public boolean handleMessage(Message me) {
                if (counted) {
                  fail("Called 2x!!!!");
                }
                counted = true;
                latch.countDown();
                return true;
              }
            });

    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    firstHandler.sendEmptyMessage(1);
                    secondHandler.sendEmptyMessage(1);
                    uiController.get().loopMainThreadUntilIdle();

                    latch.countDown();
                  }
                }));

    try {
      assertTrue(
          "Timed out waiting for looper to process all events", latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  @Test
  public void loopForAtLeast() throws Exception {
    final CountDownLatch latch = new CountDownLatch(2);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    testThread
                        .getHandler()
                        .post(
                            new Runnable() {
                              @Override
                              public void run() {
                                latch.countDown();
                              }
                            });
                    uiController.get().loopMainThreadForAtLeast(1000);
                    latch.countDown();
                  }
                }));
    assertTrue(
        "Never returned from UiControllerImpl.loopMainThreadForAtLeast();",
        latch.await(10, TimeUnit.SECONDS));
  }

  @Test
  public void loopMainThreadUntilIdle_fullQueue() {
    final CountDownLatch latch = new CountDownLatch(3);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "On main thread");
                    Handler handler = new Handler();
                    Log.i(TAG, "Equeueing test runnable 1");
                    handler.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            Log.i(TAG, "Running test runnable 1");
                            latch.countDown();
                          }
                        });
                    Log.i(TAG, "Equeueing test runnable 2");
                    handler.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            Log.i(TAG, "Running test runnable 2");
                            latch.countDown();
                          }
                        });
                    Log.i(TAG, "Hijacking thread and looping it.");
                    uiController.get().loopMainThreadUntilIdle();
                    latch.countDown();
                  }
                }));

    try {
      assertTrue(
          "Timed out waiting for looper to process all events", latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  @Test
  public void loopMainThreadUntilIdle_fullQueueAndAsyncTasks() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);
    final CountDownLatch asyncTaskStarted = new CountDownLatch(1);
    final CountDownLatch asyncTaskShouldComplete = new CountDownLatch(1);
    asyncPool.execute(
        new Runnable() {
          @Override
          public void run() {
            asyncTaskStarted.countDown();
            while (true) {
              try {
                asyncTaskShouldComplete.await();
                return;
              } catch (InterruptedException ie) {
                // cant interrupt me. ignore.
              }
            }
          }
        });
    assertTrue("async task is not starting!", asyncTaskStarted.await(2, TimeUnit.SECONDS));

    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "On main thread");
                    Handler handler = new Handler();
                    Log.i(TAG, "Equeueing test runnable 1");
                    handler.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            Log.i(TAG, "Running test runnable 1");
                            latch.countDown();
                          }
                        });
                    Log.i(TAG, "Equeueing test runnable 2");
                    handler.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            Log.i(TAG, "Running test runnable 2");
                            latch.countDown();
                          }
                        });
                    Log.i(TAG, "Hijacking thread and looping it.");
                    uiController.get().loopMainThreadUntilIdle();
                    latch.countDown();
                  }
                }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(2, TimeUnit.SECONDS));
    assertEquals("Not all main thread tasks have checked in", 1L, latch.getCount());
    asyncTaskShouldComplete.countDown();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  public void loopMainThreadUntilIdle_emptyQueue() {
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    uiController.get().loopMainThreadUntilIdle();
                    latch.countDown();
                  }
                }));
    try {
      assertTrue(
          "Never returned from UiControllerImpl.loopMainThreadUntilIdle();",
          latch.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      fail("Failed with exception " + e);
    }
  }

  @Test
  public void loopMainThreadUntilIdle_oneIdlingResource() throws InterruptedException {
    OnDemandIdlingResource fakeResource = new OnDemandIdlingResource("FakeResource");
    idlingResourceRegistry.registerResources(listOf(fakeResource));
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "Hijacking thread and looping it.");
                    uiController.get().loopMainThreadUntilIdle();
                    latch.countDown();
                  }
                }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(2, TimeUnit.SECONDS));
    fakeResource.forceIdleNow();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  public void loopMainThreadUntilIdle_multipleIdlingResources() throws InterruptedException {
    OnDemandIdlingResource fakeResource1 = new OnDemandIdlingResource("FakeResource1");
    OnDemandIdlingResource fakeResource2 = new OnDemandIdlingResource("FakeResource2");
    OnDemandIdlingResource fakeResource3 = new OnDemandIdlingResource("FakeResource3");
    // Register the first two right away and one later (once the wait for the first two begins).
    idlingResourceRegistry.registerResources(listOf(fakeResource1, fakeResource2));
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "Hijacking thread and looping it.");
                    uiController.get().loopMainThreadUntilIdle();
                    latch.countDown();
                  }
                }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource1.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    idlingResourceRegistry.registerResources(listOf(fakeResource3));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource2.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(1, TimeUnit.SECONDS));
    fakeResource3.forceIdleNow();
    assertTrue("App should be idle.", latch.await(5, TimeUnit.SECONDS));
  }

  @Test
  public void loopMainThreadUntilIdle_timeout() throws InterruptedException {
    OnDemandIdlingResource goodResource = new OnDemandIdlingResource("GoodResource");
    OnDemandIdlingResource kindaCrappyResource = new OnDemandIdlingResource("KindaCrappyResource");
    OnDemandIdlingResource badResource = new OnDemandIdlingResource("VeryBadResource");
    idlingResourceRegistry.registerResources(
        listOf(goodResource, kindaCrappyResource, badResource));
    final CountDownLatch latch = new CountDownLatch(1);
    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "Hijacking thread and looping it.");
                    try {
                      uiController.get().loopMainThreadUntilIdle();
                    } catch (IdlingResourceTimeoutException e) {
                      latch.countDown();
                    }
                  }
                }));
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(4, TimeUnit.SECONDS));
    goodResource.forceIdleNow();
    assertFalse(
        "Should not have stopped looping the main thread yet!", latch.await(12, TimeUnit.SECONDS));
    kindaCrappyResource.forceIdleNow();
    assertTrue(
        "Should have caught IdlingResourceTimeoutException", latch.await(11, TimeUnit.SECONDS));
  }

  @Test
  public void testEspressoInterruption() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);

    final int timeToWaitForIdle = 1000;

    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "Enqueueing test runnable 1");
                    testThread
                        .getHandler()
                        .post(
                            new Runnable() {
                              @Override
                              public void run() {
                                Log.i(TAG, "Running runnable 1");
                                latch.countDown();
                              }
                            });
                    try {
                      Log.i(TAG, "Looping main thread for: " + timeToWaitForIdle);
                      uiController.get().loopMainThreadForAtLeast(timeToWaitForIdle);
                      fail("Expected for loopMainThreadForAtLeast to be interrupted");
                    } catch (RuntimeException e) {
                      // expected
                      latch.countDown();
                    }
                  }
                }));

    assertTrue(
        testThread
            .getHandler()
            .post(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "Enqueueing test runnable 2");
                    testThread
                        .getHandler()
                        .post(
                            new Runnable() {
                              @Override
                              public void run() {
                                Log.i(TAG, "Running runnable 2");
                                latch.countDown();
                              }
                            });
                    Log.i(TAG, "Interrupting Espresso tasks running on main thread");
                    uiController.get().interruptEspressoTasks();
                    latch.countDown();
                  }
                }));

    assertTrue(
        "UiController tasks never interrupted",
        latch.await(timeToWaitForIdle - 100, TimeUnit.MILLISECONDS));
  }

  /** Verify uiController can be initialized on instrumentation thread. */
  @Test
  public void interruptInitialization() {
    uiController.get().interruptEspressoTasks();
  }
}
