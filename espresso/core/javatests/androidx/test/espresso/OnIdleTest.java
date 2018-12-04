/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.espresso;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public final class OnIdleTest {

  @Before
  public void setMasterPolicyTimeout() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS);
  }

  @After
  public void unsetMasterPolicyTimeout() throws Exception {
    IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
  }

  @Test
  public void waitForIdle() throws Exception {
    DummyIdlingResource resource = new DummyIdlingResource("testResource", true /*isIdle*/);
    assertThat(Espresso.registerIdlingResources(resource), is(true));
    CountDownLatch countDownLatch = new CountDownLatch(1);
    try {
      Espresso.onIdle(
          new Callable<Void>() {
            @Override
            public Void call() {
              countDownLatch.countDown();
              return null;
            }
          });
      assertThat(countDownLatch.await(10, TimeUnit.SECONDS), is(true));
    } finally {
      assertThat(Espresso.unregisterIdlingResources(resource), is(true));
    }
  }

  @Test
  public void neverIdleResourceThrowsAppNotIdleException() throws Exception {
    DummyIdlingResource resource = new DummyIdlingResource("testResource", false /*isIdle*/);
    assertThat(Espresso.registerIdlingResources(resource), is(true));
    CountDownLatch countDownLatch = new CountDownLatch(1);
    try {
      Espresso.onIdle(
          new Callable<Void>() {
            @Override
            public Void call() {
              countDownLatch.countDown();
              return null;
            }
          });
      assertThat(countDownLatch.await(10, TimeUnit.SECONDS), is(false));
    } catch (AppNotIdleException expected) {
      assertThat(expected, instanceOf(AppNotIdleException.class));
    } finally {
      assertThat(Espresso.unregisterIdlingResources(resource), is(true));
    }
  }

  @Test
  public void neverIdleResourceThrowsAppNotIdleException_withIdlingRegistry() throws Exception {
    DummyIdlingResource resource = new DummyIdlingResource("testResource", false /*isIdle*/);
    assertThat(IdlingRegistry.getInstance().register(resource), is(true));
    CountDownLatch countDownLatch = new CountDownLatch(1);
    try {
      Espresso.onIdle(
          new Callable<Void>() {
            @Override
            public Void call() {
              countDownLatch.countDown();
              return null;
            }
          });
      assertThat(countDownLatch.await(10, TimeUnit.SECONDS), is(false));
    } catch (AppNotIdleException expected) {
      assertThat(expected, instanceOf(AppNotIdleException.class));
    } finally {
      assertThat(IdlingRegistry.getInstance().unregister(resource), is(true));
    }
  }

  @Test
  public void onIdle_neverIdleResourceThrowsAppNotIdleException_withIdlingRegistry()
      throws Exception {
    DummyIdlingResource resource = new DummyIdlingResource("testResource", false /*isIdle*/);
    assertThat(IdlingRegistry.getInstance().register(resource), is(true));
    try {
      Espresso.onIdle();
      fail("Expected AppNotIdleException to be thrown");
    } catch (AppNotIdleException expected) {
      assertThat(expected, instanceOf(AppNotIdleException.class));
    } finally {
      assertThat(IdlingRegistry.getInstance().unregister(resource), is(true));
    }
  }

  @Test
  public void resourcePostsTaskToUiLooper() throws Exception {
    DummyIdlingResource resource = new DummyIdlingResource("testResource", false /*isIdle*/);
    assertThat(Espresso.registerIdlingResources(resource), is(true));
    MainThreadTask mainThreadTask = resource.setIdle();
    assertThat(mainThreadTask, notNullValue());
    assertThat(Espresso.unregisterIdlingResources(resource), is(true));
    CountDownLatch countDownLatch = new CountDownLatch(1);
    mainThreadTask.stopBeingBusy();
    Espresso.onIdle(
        new Callable<Void>() {
          @Override
          public Void call() {
            countDownLatch.countDown();
            return null;
          }
        });
    assertThat(countDownLatch.await(10, TimeUnit.SECONDS), is(true));
  }

  private static class MainThreadTask implements Runnable {
    private final AtomicBoolean continueBeingBusy = new AtomicBoolean(true);
    private final Handler handler;

    MainThreadTask(Handler handler) {
      this.handler = handler;
    }

    void stopBeingBusy() {
      continueBeingBusy.set(false);
    }

    @Override
    public void run() {
      if (!continueBeingBusy.get()) {
        return;
      } else {
        handler.post(this);
      }
    }
  }

  private static class DummyIdlingResource implements IdlingResource {
    private final String name;
    private ResourceCallback callback;
    private boolean isIdle = false;

    DummyIdlingResource(String name, boolean isIdle) {
      this.name = name;
      this.isIdle = isIdle;
    }

    /**
     * Returns the MainThreadTask that was posted to the main Looper before this resource
     * transitioned to idle, or null if the resource was already idle.
     */
    MainThreadTask setIdle() throws Exception {
      MainThreadTask mainThreadTask = null;
      if (!isIdle) {
        Handler handler = new Handler(Looper.getMainLooper());
        mainThreadTask = new MainThreadTask(handler);
        FutureTask<Void> task = new FutureTask<>(mainThreadTask, null);
        handler.post(task);
        task.get();

        callback.onTransitionToIdle();
      }
      isIdle = true;
      return mainThreadTask;
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      this.callback = callback;
    }
  }
}
