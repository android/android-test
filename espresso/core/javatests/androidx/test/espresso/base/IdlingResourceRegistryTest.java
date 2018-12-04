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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link IdlingResourceRegistry}. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class IdlingResourceRegistryTest {

  private IdlingResourceRegistry registry;
  private Handler handler;

  @Before
  public void setUp() throws Exception {
    Looper looper = Looper.getMainLooper();
    handler = new Handler(looper);
    registry = new IdlingResourceRegistry(looper);
  }

  @Test
  public void verifyRegisterUnRegister() throws Exception {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    r1.forceIdleNow();
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    r2.forceIdleNow();
    registry.registerResources(Lists.newArrayList(r1));
    registry.registerResources(Lists.newArrayList(r2));
    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);

    assertTrue(resourcesIdle.get());
    r1.reset();
    r2.reset();
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());

    registry.unregisterResources(Lists.newArrayList(r1));
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());

    r2.forceIdleNow();
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void registerDuplicates() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r1dup = new OnDemandIdlingResource("r1");
    assertTrue(registry.registerResources(Lists.newArrayList(r1)));
    assertFalse(registry.registerResources(Lists.newArrayList(r1)));
    assertFalse(registry.registerResources(Lists.newArrayList(r1dup)));
    assertEquals(1, registry.getResources().size());
  }

  @Test
  public void unregisterNeverRegistered() throws Exception {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r2 = new OnDemandIdlingResource("r2");

    assertTrue(registry.registerResources(Lists.newArrayList(r1)));
    assertFalse(registry.unregisterResources(Lists.newArrayList(r2)));

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);

    // r1 should still be registered
    assertFalse(resourcesIdle.get());
  }

  @Test
  public void unregisteredResourceIsDisconnected() throws Exception {
    // This repros a bug where a resource which has been registered and then
    // unregistered could still send an onTransitionToIdle() message back to
    // the IRR and corrupt it's internal data structures.

    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    OnDemandIdlingResource r3 = new OnDemandIdlingResource("r3");

    assertTrue(registry.registerResources(Lists.newArrayList(r1, r2, r3)));

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());
    assertTrue(registry.unregisterResources(Lists.newArrayList(r3)));

    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);

    // r1 and r2 are still busy.
    assertFalse(resourcesIdle.get());

    // this message should be ignored by the IRR
    r3.forceIdleNow();

    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());

    r2.forceIdleNow();
    r1.forceIdleNow();
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);

    // if this is false, r3's message broke the IRR
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void registerAndUnregisterIdling() throws Exception {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    r1.forceIdleNow();

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);

    assertTrue(resourcesIdle.get());

    r1.reset();

    assertFalse(registry.unregisterResources(Lists.newArrayList(r1)));

    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void registerAndUnregisterNeverIdling() throws Exception {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    registry.registerResources(Lists.newArrayList(r1));

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());

    registry.unregisterResources(Lists.newArrayList(r1));

    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void registerAndUnregisterReturnValue() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r2 = new OnDemandIdlingResource("r2");

    assertTrue(registry.registerResources(Lists.newArrayList(r1)));
    assertFalse(registry.registerResources(Lists.newArrayList(r1)));
    assertTrue(registry.registerResources(Lists.newArrayList(r2)));

    IdlingResource r3 = new OnDemandIdlingResource("r3");
    assertFalse(registry.registerResources(Lists.newArrayList(r3, r3)));

    IdlingResource r4 = new OnDemandIdlingResource("r4");
    assertFalse(registry.unregisterResources(Lists.newArrayList(r4)));

    assertTrue(registry.unregisterResources(Lists.newArrayList(r1)));
    assertFalse(registry.unregisterResources(Lists.newArrayList(r1)));
    assertTrue(registry.unregisterResources(Lists.newArrayList(r2)));

    assertFalse(registry.unregisterResources(Lists.newArrayList(r3, r3)));
  }

  @Test
  public void getResources() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r2 = new OnDemandIdlingResource("r2");

    assertEquals(0, registry.getResources().size());

    registry.registerResources(Lists.newArrayList(r1, r2));
    assertEquals(2, registry.getResources().size());

    registry.unregisterResources(Lists.newArrayList(r1, r2));
    assertEquals(0, registry.getResources().size());
  }

  @Test
  public void allResourcesAreIdle() throws Exception {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    IdlingResource r3 = new OnDemandIdlingResource("r3");
    r1.forceIdleNow();
    r2.forceIdleNow();
    registry.registerResources(Lists.newArrayList(r1, r2));
    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());

    registry.registerResources(Lists.newArrayList(r3));

    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());
  }

  @Test
  public void allResourcesAreIdle_RepeatingToIdleTransitions() throws Exception {
    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    registry.registerResources(Lists.newArrayList(r1));
    for (int i = 1; i <= 3; i++) {
      FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
      handler.post(resourcesIdle);
      assertFalse("Busy test " + i, resourcesIdle.get());

      r1.forceIdleNow();

      resourcesIdle = createIdleCheckTask(registry);
      handler.post(resourcesIdle);
      assertTrue("Idle transition test " + i, resourcesIdle.get());

      r1.reset();
    }
  }

  @Test
  public void notifyWhenAllResourcesAreIdle_success() throws InterruptedException {
    final CountDownLatch busyWarningLatch = new CountDownLatch(4);
    final CountDownLatch timeoutLatch = new CountDownLatch(1);
    final CountDownLatch allResourcesIdleLatch = new CountDownLatch(1);
    final AtomicReference<List<String>> busysFromWarning = new AtomicReference<List<String>>();

    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    OnDemandIdlingResource r3 = new OnDemandIdlingResource("r3");
    registry.registerResources(Lists.newArrayList(r1, r2, r3));

    handler.post(
        new Runnable() {

          @Override
          public void run() {
            registry.notifyWhenAllResourcesAreIdle(
                new IdleNotificationCallback() {
                  private static final String TAG = "IdleNotifCallback";

                  @Override
                  public void resourcesStillBusyWarning(List<String> busyResourceNames) {
                    Log.w(TAG, "Timeout warning: " + busyResourceNames);
                    busysFromWarning.set(busyResourceNames);
                    busyWarningLatch.countDown();
                  }

                  @Override
                  public void resourcesHaveTimedOut(List<String> busyResourceNames) {
                    Log.w(TAG, "Timeout error: " + busyResourceNames);
                    timeoutLatch.countDown();
                  }

                  @Override
                  public void allResourcesIdle() {
                    allResourcesIdleLatch.countDown();
                  }
                });
          }
        });

    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(3, busysFromWarning.get().size());

    r3.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(2, busysFromWarning.get().size());

    r2.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());

    r1.forceIdleNow();
    assertTrue(allResourcesIdleLatch.await(200, TimeUnit.MILLISECONDS));
    assertEquals(1, busyWarningLatch.getCount());
    assertEquals(1, timeoutLatch.getCount());
  }

  @Test
  public void notifyWhenAllResourcesAreIdle_timeout() throws InterruptedException {
    final CountDownLatch busyWarningLatch = new CountDownLatch(5);
    final CountDownLatch timeoutLatch = new CountDownLatch(1);
    final CountDownLatch allResourcesIdleLatch = new CountDownLatch(1);
    final AtomicReference<List<String>> busysFromWarning = new AtomicReference<List<String>>();

    OnDemandIdlingResource r1 = new OnDemandIdlingResource("r1");
    OnDemandIdlingResource r2 = new OnDemandIdlingResource("r2");
    OnDemandIdlingResource r3 = new OnDemandIdlingResource("r3");
    registry.registerResources(Lists.newArrayList(r1, r2, r3));

    handler.post(
        new Runnable() {
          @Override
          public void run() {
            registry.notifyWhenAllResourcesAreIdle(
                new IdleNotificationCallback() {
                  private static final String TAG = "IdleNotifCallback";

                  @Override
                  public void resourcesStillBusyWarning(List<String> busyResourceNames) {
                    Log.w(TAG, "Timeout warning: " + busyResourceNames);
                    busysFromWarning.set(busyResourceNames);
                    busyWarningLatch.countDown();
                  }

                  @Override
                  public void resourcesHaveTimedOut(List<String> busyResourceNames) {
                    Log.w(TAG, "Timeout error: " + busyResourceNames);
                    timeoutLatch.countDown();
                  }

                  @Override
                  public void allResourcesIdle() {
                    allResourcesIdleLatch.countDown();
                  }
                });
          }
        });

    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(3, busysFromWarning.get().size());

    r1.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(2, busysFromWarning.get().size());

    r2.forceIdleNow();
    assertFalse("Expected to timeout", busyWarningLatch.await(6, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());

    assertTrue("Expected to finish count down", busyWarningLatch.await(8, TimeUnit.SECONDS));
    assertTrue("Should have timed out", timeoutLatch.await(2, TimeUnit.SECONDS));
    assertEquals(1, busysFromWarning.get().size());
    assertEquals(1, allResourcesIdleLatch.getCount());
  }

  @Test
  public void testSecondaryLooper_sleepingTask() throws Exception {
    HandlerThread ht = new HandlerThread("sleeping");
    ht.start();
    Handler secondaryHandler = new Handler(ht.getLooper());
    registry.registerLooper(ht.getLooper(), false);
    Thread.sleep(50);

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());

    final Semaphore started = new Semaphore(0);
    final Semaphore woke = new Semaphore(0);
    secondaryHandler.post(
        new FutureTask<Void>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                started.release();
                try {
                  Thread.sleep(100000);
                } catch (InterruptedException expected) {
                  // the test will interupt us.
                }
                woke.release();
                return null;
              }
            }));

    started.acquire();
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());
    ht.getLooper().getThread().interrupt();
    woke.acquire();

    Thread.sleep(50);
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void testSecondaryLooper_blockingTask() throws Exception {
    HandlerThread ht = new HandlerThread("secondary");
    ht.start();
    Handler secondaryHandler = new Handler(ht.getLooper());
    registry.registerLooper(ht.getLooper(), false);
    Thread.sleep(50);

    FutureTask<Boolean> resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());

    final Semaphore started = new Semaphore(0);
    final Semaphore blocking = new Semaphore(0);
    secondaryHandler.post(
        new FutureTask<Void>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                started.release();
                blocking.acquire();
                return null;
              }
            }));

    started.acquire();
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertFalse(resourcesIdle.get());

    blocking.release();
    Thread.sleep(50);
    resourcesIdle = createIdleCheckTask(registry);
    handler.post(resourcesIdle);
    assertTrue(resourcesIdle.get());
  }

  @Test
  public void testSync_ofIdlingResource() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    registry.sync(Sets.newHashSet(r1), Sets.<Looper>newHashSet());
    assertEquals(1, registry.getResources().size());
    assertTrue(registry.getResources().contains(r1));
  }

  @Test
  public void testSync_ofLooperAsIdlingResource() {
    IdlingResource r1 = LooperIdlingResourceInterrogationHandler.forLooper(handler.getLooper());
    registry.sync(Sets.newHashSet(r1), Sets.newHashSet(handler.getLooper()));
    assertEquals(1, registry.getResources().size());
    String expectedLooperResourceName =
        LooperIdlingResourceInterrogationHandler.forLooper(handler.getLooper()).getName();
    assertEquals(expectedLooperResourceName, registry.getResources().get(0).getName());
  }

  @Test
  public void testSync_withMultiRegisterAndUnregister() {
    IdlingResource r1 = new OnDemandIdlingResource("r1");
    IdlingResource r2 = new OnDemandIdlingResource("r2");

    // Register r1 directly
    assertTrue(registry.registerResources(Lists.newArrayList(r1)));
    // Sync with second r2 and a looper
    registry.sync(Sets.newHashSet(r2), Sets.newHashSet(Looper.getMainLooper()));
    // Verify r2 and looper registered
    // Verify r1 is unregister
    assertEquals(2, registry.getResources().size());
    assertTrue(
        registry
            .getResources()
            .contains(LooperIdlingResourceInterrogationHandler.forLooper(handler.getLooper())));
    assertTrue(registry.getResources().contains(r2));
  }

  @Test
  public void testSync_withDuplicateRegisterAndUnregister() {
    IdlingResource toUnReg = new OnDemandIdlingResource("toUnReg");
    IdlingResource alreadyReg = new OnDemandIdlingResource("alreadyReg");
    IdlingResource newReg = new OnDemandIdlingResource("newReg");

    // Register "toUnReg" and "alreadyReg"
    assertTrue(registry.registerResources(Lists.newArrayList(toUnReg)));
    assertTrue(registry.registerResources(Lists.newArrayList(alreadyReg)));

    // Sync with second "alreadyReg" and "newReg"
    registry.sync(Sets.newHashSet(alreadyReg, newReg), Sets.<Looper>newHashSet());
    // Verify toUnReg is unregistered
    // Verify alreadyReg stayed registered
    // verify newReg registered
    assertEquals(2, registry.getResources().size());
    assertTrue(registry.getResources().contains(alreadyReg));
    assertTrue(registry.getResources().contains(newReg));
  }

  private FutureTask<Boolean> createIdleCheckTask(final IdlingResourceRegistry registry) {
    Callable<Boolean> isIdle =
        new Callable<Boolean>() {
          @Override
          public Boolean call() {

            return registry.allResourcesAreIdle();
          }
        };

    return new FutureTask<Boolean>(isIdle);
  }
}
