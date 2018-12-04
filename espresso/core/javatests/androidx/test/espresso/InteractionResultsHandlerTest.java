/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.espresso.remote.NoRemoteEspressoInstanceException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link InteractionResultsHandler}. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class InteractionResultsHandlerTest {
  private static final ListeningExecutorService TEST_EXECUTOR_1 =
      MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  private static final ListeningExecutorService TEST_EXECUTOR_2 =
      MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  private static final ListeningExecutorService TEST_EXECUTOR_3 =
      MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

  List<ListenableFuture<Void>> interactionsList;

  @Before
  public void setup() {
    interactionsList = new ArrayList<>();
  }

  @After
  public void tearDown() {
    interactionsList.clear();
  }

  @Test
  public void verifyNullInteractionsThrows() {
    try {
      InteractionResultsHandler.gatherAnyResult(null);
      fail("Didn't throw NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void verifyEmptyInteractionsThrows() {
    try {
      InteractionResultsHandler.gatherAnyResult(interactionsList);
      fail("Didn't throw IllegalStateException");
    } catch (IllegalStateException npe) {
      // expected
    }
  }

  @Test
  public void firstSuccessfulInteractionShouldCancelAllOthers() throws Throwable {
    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);
    CountDownLatch latch3 = new CountDownLatch(1);
    interactionsList.add(TEST_EXECUTOR_1.submit(getBockingCallable(100, latch1, null)));
    interactionsList.add(TEST_EXECUTOR_1.submit(getBockingCallable(500, latch2, null)));
    interactionsList.add(TEST_EXECUTOR_2.submit(getBockingCallable(500, latch3, null)));

    InteractionResultsHandler.gatherAnyResult(interactionsList);

    // Successful interaction
    assertTrue(
        "Shortest interaction didn't finish successfully",
        latch1.await(200, TimeUnit.MILLISECONDS));
    // Canceled/interrupted interactions
    assertFalse("Longer interaction didn't interrupt", latch2.await(600, TimeUnit.MILLISECONDS));
    assertFalse("Longer interaction didn't interrupt", latch3.await(600, TimeUnit.MILLISECONDS));
  }

  @Test
  public void verifyAllInteractionsFinishWhenNoSuccessfulInteractionExists() throws Throwable {

    NoActivityResumedException expectedException =
        new NoActivityResumedException("Runnable Exception");
    Runnable throwsRunnable = getRunnableThatThrows(expectedException);

    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);
    interactionsList.add(TEST_EXECUTOR_1.submit(getBockingCallable(100, latch1, throwsRunnable)));
    interactionsList.add(TEST_EXECUTOR_2.submit(getBockingCallable(500, latch2, throwsRunnable)));

    try {
      InteractionResultsHandler.gatherAnyResult(interactionsList);
      fail("Expected to throw an exception");
    } catch (NoActivityResumedException e) {
      // When both failing interaction finish should throw the exception.
      assertThat(e, is(expectedException));
      // Both should complete successfully
      assertTrue("First interaction interrupted", latch1.await(200, TimeUnit.MILLISECONDS));
      assertTrue("Second interaction interrupted", latch2.await(600, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  public void verifyInteractionExceptionTakesPrecedenceOverNoRemoteInstanceExcepting()
      throws Throwable {
    NoActivityResumedException noActivityException =
        new NoActivityResumedException("No Activity in Resumed state");
    Runnable noActivityExceptionRunnable = getRunnableThatThrows(noActivityException);

    NoRemoteEspressoInstanceException noRemoteException =
        new NoRemoteEspressoInstanceException("No Remote Espresso instance at this time");
    Runnable noRemoteExceptionRunnable = getRunnableThatThrows(noRemoteException);

    CountDownLatch noActivityLatch = new CountDownLatch(1);
    CountDownLatch noRemoteLatch = new CountDownLatch(1);
    interactionsList.add(
        TEST_EXECUTOR_1.submit(
            getBockingCallable(500, noActivityLatch, noActivityExceptionRunnable)));
    interactionsList.add(
        TEST_EXECUTOR_2.submit(getBockingCallable(100, noRemoteLatch, noRemoteExceptionRunnable)));

    try {
      InteractionResultsHandler.gatherAnyResult(interactionsList);
      fail("Expected to NoActivityResumedException an exception");
    } catch (NoRemoteEspressoInstanceException e) {
      fail("Expected to NoActivityResumedException instead of NoRemoteEspressoInstanceException");
    } catch (NoActivityResumedException e) {
      // Should always throw NoActivityResumedException exception over NoRemoteEspressoInstance
      assertThat(e, is(noActivityException));
      // Both should complete successfully
      assertTrue(
          "noActivityCallable interrupted", noActivityLatch.await(200, TimeUnit.MILLISECONDS));
      assertTrue("noRemoteCallable interrupted", noRemoteLatch.await(600, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  public void verifyAssertionErrorsTakePrecedenceOverAllOther() throws Throwable {
    NoActivityResumedException noActivityException =
        new NoActivityResumedException("No Activity in Resumed state");
    Runnable noActivityExceptionRunnable = getRunnableThatThrows(noActivityException);
    NoRemoteEspressoInstanceException noRemoteException =
        new NoRemoteEspressoInstanceException("No Remote Espresso instance at this time");
    Runnable noRemoteExceptionRunnable = getRunnableThatThrows(noRemoteException);
    final AssertionFailedError assertionFailedError =
        new AssertionFailedError("Assertions on some UI failed");
    Runnable assertionErrorRunnable =
        new Runnable() {
          @Override
          public void run() {
            throw assertionFailedError;
          }
        };

    CountDownLatch noActivityLatch = new CountDownLatch(1);
    CountDownLatch noRemoteLatch = new CountDownLatch(1);
    CountDownLatch assertLatch = new CountDownLatch(1);
    interactionsList.add(
        TEST_EXECUTOR_1.submit(
            getBockingCallable(500, noActivityLatch, noActivityExceptionRunnable)));
    interactionsList.add(
        TEST_EXECUTOR_2.submit(getBockingCallable(400, noRemoteLatch, noRemoteExceptionRunnable)));
    interactionsList.add(
        TEST_EXECUTOR_3.submit(getBockingCallable(100, assertLatch, assertionErrorRunnable)));

    try {
      InteractionResultsHandler.gatherAnyResult(interactionsList);
      fail("Expected to throw AssertionFailedError");
    } catch (NoRemoteEspressoInstanceException e) {
      fail("Expected to throw AssertionFailedError instead of NoRemoteEspressoInstanceException");
    } catch (NoActivityResumedException e) {
      fail("Expected to AssertionFailedError instead of NoActivityResumedException");
    } catch (AssertionFailedError expected) {
      // Should always throw AssertionFailedError exception over everything else
      assertThat(expected, is(assertionFailedError));
      // Should complete successfully
      assertTrue("assertErrorCallable interrupted", assertLatch.await(200, TimeUnit.MILLISECONDS));
      // All other should be interrupted
      assertFalse(
          "noActivityCallable wasn't interrupted",
          noActivityLatch.await(200, TimeUnit.MILLISECONDS));
      assertFalse(
          "noRemoteCallable wasn't interrupted", noRemoteLatch.await(200, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  public void verifyMultipleConcurrentInteractionsOnDifferentThreadsReportSuccess()
      throws Throwable {
    NoRemoteEspressoInstanceException noRemoteException =
        new NoRemoteEspressoInstanceException("No Remote Espresso instance at this time");
    Runnable noRemoteExceptionRunnable = getRunnableThatThrows(noRemoteException);

    for (int i = 0; i < 1000; i++) {
      interactionsList = new ArrayList<>();
      // Repeat many times to ensure stability
      final CountDownLatch successfulLatch = new CountDownLatch(1);
      final CountDownLatch noRemoteLatch = new CountDownLatch(1);

      final ListenableFuture<Void> noRemoteFuture =
          TEST_EXECUTOR_2.submit(getBockingCallable(0, noRemoteLatch, noRemoteExceptionRunnable));
      final ListenableFuture<Void> successFutures =
          TEST_EXECUTOR_1.submit(getBockingCallable(0, successfulLatch, null));

      interactionsList.add(noRemoteFuture);
      interactionsList.add(successFutures);

      InteractionResultsHandler.gatherAnyResult(
          interactionsList,
          new Executor() {
            @Override
            public void execute(Runnable r) {
              new Thread(r).start();
            }
          });
    }
  }

  private Callable<Void> getBockingCallable(
      final long millis, final CountDownLatch latch, final Runnable runnable) {
    return new Callable<Void>() {
      @Override
      public Void call() {
        try {
          Thread.sleep(millis);
          latch.countDown();
          if (runnable != null) {
            runnable.run();
          }
        } catch (InterruptedException e) {
          // don't count down the latch
        }
        return null;
      }
    };
  }

  private Runnable getRunnableThatThrows(final RuntimeException exception) {
    return new Runnable() {
      @Override
      public void run() {
        throw exception;
      }
    };
  }
}
