/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.runner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.TestExecutor;
import androidx.test.internal.runner.TestRequestBuilder;
import androidx.test.internal.runner.listener.ActivityFinisherRunListener;
import androidx.test.internal.runner.listener.CoverageListener;
import androidx.test.internal.runner.listener.DelayInjector;
import androidx.test.internal.runner.listener.InstrumentationResultPrinter;
import androidx.test.internal.runner.listener.LogRunListener;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link AndroidJUnitRunner}. */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AndroidJUnitRunnerTest {
  public static final int SLEEP_TIME = 300;

  private AndroidJUnitRunner androidJUnitRunner;

  @Captor private ArgumentCaptor<Iterable<String>> pathsCaptor;
  @Mock private Context mockContext;
  @Mock private InstrumentationResultPrinter instrumentationResultPrinter;
  @Mock private TestRequestBuilder testRequestBuilder;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    doReturn("/apps/foo.apk").when(mockContext).getPackageCodePath();
    androidJUnitRunner =
        new AndroidJUnitRunner() {

          @Override
          public Context getContext() {
            return mockContext;
          }

          @Override
          InstrumentationResultPrinter getInstrumentationResultPrinter() {
            return instrumentationResultPrinter;
          }

          @Override
          TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
            return testRequestBuilder;
          }
        };
  }

  /** Ensures that the main looper is not blocked and can process messages during test execution. */
  @Test
  public void testMainLooperIsAlive() throws InterruptedException {
    final boolean[] called = new boolean[1];
    Handler handler =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(Message msg) {
            called[0] = true;
          }
        };
    handler.sendEmptyMessage(0);
    Thread.sleep(SLEEP_TIME);
    Assert.assertTrue(called[0]);
  }

  /**
   * Ensures that the thread the test runs on has not been prepared as a looper. It doesn't make
   * sense for it to be a looper because it will be blocked for the entire duration of test
   * execution. Tests should instead post messages to the main looper or a new handler thread of
   * their own as appropriate while running.
   */
  @Test
  public void testTestThreadIsNotALooper() {
    Assert.assertNull(Looper.myLooper());
  }

  /**
   * Ensure the correct exception is passed to {@link
   * InstrumentationResultPrinter#reportProcessCrash(Throwable)}
   */
  @Test
  public void testInstrResultPrinter_reportProcessCrash() {
    Throwable e = new RuntimeException();
    androidJUnitRunner.getInstrumentationResultPrinter();
    androidJUnitRunner.onException(this, e);
    Mockito.verify(instrumentationResultPrinter).reportProcessCrash(e);
  }

  /**
   * Ensure the order of the {@link RunListener} added to the builder is following the legacy order.
   */
  @Test
  public void testLegacyOrderRunListeners() {
    Bundle b = new Bundle();
    b.putString("newRunListenerMode", "false");
    b.putString("coverage", "true");
    b.putString("delay_msec", "15");
    b.putString(
        "listener",
        "androidx.test.internal.runner.listener.LogRunListener,"
            + "androidx.test.internal.runner.listener.InstrumentationResultPrinter");
    RunnerArgs args =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), b)
            .build();
    TestExecutor.Builder executorBuilder = Mockito.mock(TestExecutor.Builder.class);
    androidJUnitRunner.addListeners(args, executorBuilder);

    InOrder order = Mockito.inOrder(executorBuilder);
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(LogRunListener.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(InstrumentationResultPrinter.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(ActivityFinisherRunListener.class));
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(DelayInjector.class));
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(CoverageListener.class));
    // Two extra user added listeners
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(LogRunListener.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(InstrumentationResultPrinter.class));
  }

  /**
   * Ensure the order of the {@link RunListener} added to the builder is following the new order
   * when the option is set.
   */
  @Test
  public void testNewOrderRunListeners() {
    Bundle b = new Bundle();
    b.putString("newRunListenerMode", "true");
    b.putString("coverage", "true");
    b.putString("delay_msec", "15");
    b.putString(
        "listener",
        "androidx.test.internal.runner.listener.LogRunListener,"
            + "androidx.test.internal.runner.listener.InstrumentationResultPrinter");
    RunnerArgs args =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), b)
            .build();
    TestExecutor.Builder executorBuilder = Mockito.mock(TestExecutor.Builder.class);
    androidJUnitRunner.addListeners(args, executorBuilder);

    InOrder order = Mockito.inOrder(executorBuilder);
    // Two extra user added listeners go first
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(LogRunListener.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(InstrumentationResultPrinter.class));
    // Default listeners added in AndroidJUnitRunner
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(LogRunListener.class));
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(DelayInjector.class));
    order.verify(executorBuilder).addRunListener(ArgumentMatchers.isA(CoverageListener.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(InstrumentationResultPrinter.class));
    order
        .verify(executorBuilder)
        .addRunListener(ArgumentMatchers.isA(ActivityFinisherRunListener.class));
  }

  /** Ensure classpathToScan paths are added to the runner. */
  @Test
  public void testClasspathToScanIsAdded() {
    Bundle b = new Bundle();
    b.putString("classpathToScan", "/foo/bar.dex:/foo/baz.dex");

    RunnerArgs runnerArgs =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), b)
            .build();
    androidJUnitRunner.buildRequest(runnerArgs, new Bundle());
    verify(testRequestBuilder, times(1)).addPathsToScan(pathsCaptor.capture());

    Set<String> pathsToScan = new HashSet<>();
    for (Object p : pathsCaptor.getValue()) {
      pathsToScan.add((String) p);
    }

    Assert.assertEquals(2, pathsToScan.size());
    Assert.assertTrue(pathsToScan.contains("/foo/bar.dex"));
    Assert.assertTrue(pathsToScan.contains("/foo/baz.dex"));
  }

  /** Ensure everything works when classpathToScan is not explicitly provided. */
  @Test
  public void testDefaultClasspathIsAdded() {
    Bundle b = new Bundle();
    RunnerArgs runnerArgs =
        new RunnerArgs.Builder()
            .fromBundle(InstrumentationRegistry.getInstrumentation(), b)
            .build();
    androidJUnitRunner.buildRequest(runnerArgs, new Bundle());
    verify(testRequestBuilder, times(1)).addPathToScan("/apps/foo.apk");
  }
}
