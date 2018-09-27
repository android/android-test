/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.runner.JUnitCore.runClasses;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ServiceTestRuleTest {

  public static class TestService extends Service {

    private final IBinder mBinder = new Binder();

    @Override
    public IBinder onBind(Intent intent) {
      return mBinder;
    }
  }

  public static class ServiceThatCantBeBoundTo extends Service {
    @Override
    public IBinder onBind(Intent intent) {
      // returns null so clients can not bind to the service
      return null;
    }
  }

  public static class ServiceThatIsNotDefinedInManifest extends Service {
    // This service is not declared in the manifest on purpose
    @Override
    public IBinder onBind(Intent intent) {
      // returns null so clients can not bind to the service
      return null;
    }
  }

  public static class TimeoutService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
      SystemClock.sleep(100);
      return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      SystemClock.sleep(100);
      return super.onStartCommand(intent, flags, startId);
    }
  }

  public static class StartedServiceLifecycleTest {
    private static StringBuilder log = new StringBuilder();

    @Rule
    public final ServiceTestRule mServiceRule =
        new ServiceTestRule() {

          @Override
          public void beforeService() {
            log.append("beforeService ");
          }

          @Override
          public void startService(@NonNull Intent intent) throws TimeoutException {
            log.append("startService ");
          }

          @Override
          public IBinder bindService(@NonNull Intent intent) throws TimeoutException {
            log.append("bindService ");
            return null;
          }

          @Override
          public void afterService() {
            log.append("afterService ");
          }

          @Override
          void shutdownService() throws TimeoutException {
            log.append("shutdownService ");
          }
        };

    @Before
    public void before() {
      log.append("before ");
    }

    @After
    public void after() {
      log.append("after ");
    }

    @Test
    public void dummyTestToLaunchService() throws TimeoutException {
      log.append("test ");
      mServiceRule.startService(new Intent());
      fail("This is a dummy test to start a service");
    }
  }

  @Test
  public void checkLifecycleOfStartedService() {
    Result result = runClasses(StartedServiceLifecycleTest.class);
    assertEquals(1, result.getFailureCount());
    assertThat(
        result.getFailures().get(0).getMessage(), is("This is a dummy test to start a service"));
    assertThat(
        StartedServiceLifecycleTest.log.toString(),
        is("beforeService before test startService after shutdownService afterService "));
  }

  public static class BoundServiceLifecycleTest {
    private static StringBuilder log = new StringBuilder();

    @Rule
    public final ServiceTestRule mServiceRule =
        new ServiceTestRule() {

          @Override
          public void beforeService() {
            log.append("beforeService ");
          }

          @Override
          public void startService(@NonNull Intent intent) throws TimeoutException {
            log.append("startService ");
          }

          @Override
          public IBinder bindService(@NonNull Intent intent) throws TimeoutException {
            log.append("bindService ");
            return null;
          }

          @Override
          public void afterService() {
            log.append("afterService ");
          }

          @Override
          public void shutdownService() {
            log.append("shutdownService ");
          }
        };

    @Before
    public void before() {
      log.append("before ");
    }

    @After
    public void after() {
      log.append("after ");
    }

    @Test
    public void dummyTestToLaunchService() throws TimeoutException {
      log.append("test ");
      mServiceRule.bindService(new Intent());
      fail("This is a dummy test to bind to a service");
    }
  }

  @Test
  public void checkLifecycleOfBoundService() {
    Result result = runClasses(BoundServiceLifecycleTest.class);
    assertEquals(1, result.getFailureCount());
    assertThat(
        result.getFailures().get(0).getMessage(), is("This is a dummy test to bind to a service"));
    assertThat(
        BoundServiceLifecycleTest.log.toString(),
        is("beforeService before test bindService after shutdownService afterService "));
  }

  public static class TimedOutServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule =
        ServiceTestRule.withTimeout(50, TimeUnit.MILLISECONDS);

    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyStartServiceTimeout() throws TimeoutException {
      thrown.expect(TimeoutException.class);
      thrown.expectMessage("Waited for 50 MILLISECONDS, but service was never connected");
      // TimeoutService takes >= 100 milliseconds to start.
      mServiceRule.startService(
          new Intent(InstrumentationRegistry.getTargetContext(), TimeoutService.class));
    }

    @Test
    public void verifyBindServiceTimeout() throws TimeoutException {
      thrown.expect(TimeoutException.class);
      thrown.expectMessage("Waited for 50 MILLISECONDS, but service was never connected");
      // TimeoutService takes >= 100 milliseconds to bind.
      mServiceRule.bindService(
          new Intent(InstrumentationRegistry.getTargetContext(), TimeoutService.class));
    }
  }

  @Test
  public void checkServiceTimeoutLogic() {
    Result result = runClasses(TimedOutServiceTest.class);
    // since we're catching exception inside the test, nothing should report failure
    assertEquals(0, result.getFailureCount());
  }

  @Rule public final ServiceTestRule mServiceRule = new ServiceTestRule();

  @Test
  public void verifySuccessfulServiceStart() throws TimeoutException {
    mServiceRule.startService(
        new Intent(InstrumentationRegistry.getTargetContext(), TestService.class));
    assertTrue("The service was not started", mServiceRule.mServiceStarted);
    assertTrue("The service was not bound", mServiceRule.mServiceBound);
  }

  @Test
  public void verifySuccessfulServiceBind() throws TimeoutException {
    mServiceRule.bindService(
        new Intent(InstrumentationRegistry.getTargetContext(), TestService.class));
    assertTrue("The service was not bound", mServiceRule.mServiceBound);
    assertFalse("The service started instead of bound", mServiceRule.mServiceStarted);
  }

  @Test
  public void serviceCanBeBoundTwice() throws TimeoutException {
    Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), TestService.class);

    IBinder firstBinder = mServiceRule.bindService(intent);
    assertNotNull("Service failed to bind 1/2", firstBinder);

    IBinder secondBinder = mServiceRule.bindService(intent);
    assertNotNull("Service failed to bind 2/2", secondBinder);
  }

  @Test
  public void serviceCanBindAfterUnbind() throws TimeoutException {
    Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), TestService.class);

    IBinder firstBinder = mServiceRule.bindService(intent);
    assertNotNull("Service failed to bind 1/2", firstBinder);
    mServiceRule.unbindService();

    IBinder secondBinder = mServiceRule.bindService(intent);
    assertNotNull("Service failed to bind 2/2", secondBinder);
  }

  @Test
  public void serviceThatCantBeBoundTo() {
    Intent intent =
        new Intent(InstrumentationRegistry.getTargetContext(), ServiceThatCantBeBoundTo.class);
    try {
      mServiceRule.startService(intent);
      fail("TimeoutException was not thrown");
    } catch (TimeoutException e) {
      // expected
    }
  }

  @Test
  public void serviceThatIsNotDefinedInManifest() throws TimeoutException {
    Intent intent =
        new Intent(
            InstrumentationRegistry.getTargetContext(), ServiceThatIsNotDefinedInManifest.class);
    assertFalse(mServiceRule.bindServiceAndWait(intent, null, 123));
  }
}
