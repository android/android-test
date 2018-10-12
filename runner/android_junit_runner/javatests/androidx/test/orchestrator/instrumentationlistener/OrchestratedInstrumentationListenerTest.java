/*
 * Copyright (C) 2017 The Android Open Source Project
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
package androidx.test.orchestrator.instrumentationlistener;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.os.RemoteException;
import androidx.test.orchestrator.SampleJUnitTest;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link OrchestrationListenerManager}. */
@RunWith(RobolectricTestRunner.class)
public class OrchestratedInstrumentationListenerTest
    implements OrchestratedInstrumentationListener.OnConnectListener {

  @Mock OrchestratorCallback mockCallback;

  private OrchestratedInstrumentationListener listener;
  private Description jUnitDescription;
  private Failure jUnitFailure;
  private Result jUnitResult;

  @Override
  public void onOrchestratorConnect() {
    // Do nothing
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    listener = new OrchestratedInstrumentationListener(this);
    listener.odoCallback = mockCallback;

    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    jUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    jUnitFailure = new Failure(jUnitDescription, new Throwable("error"));
    jUnitResult = new Result();
    RunListener jUnitListener = jUnitResult.createListener();
    jUnitListener.testRunStarted(jUnitDescription);
    jUnitListener.testStarted(jUnitDescription);
    jUnitListener.testFinished(jUnitDescription);
  }

  @Test
  public void nullCallbackThrowsException() {
    try {
      listener.odoCallback = null;
      listener.testRunStarted(jUnitDescription);
      fail("Listener should throw an error if the callback is null");
    } catch (IllegalStateException e) {
      // as expected
    }
  }

  @Test
  public void testRunStarted() throws RemoteException {
    listener.testRunStarted(jUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, jUnitDescription);
  }

  @Test
  public void testRunFinished() throws RemoteException {
    listener.testRunFinished(jUnitResult);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableResult result = BundleJUnitUtils.getResult(argument.getValue());
    assertThat(result.wasSuccessful(), is(jUnitResult.wasSuccessful()));
  }

  @Test
  public void testStarted() throws RemoteException {
    listener.testStarted(jUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, jUnitDescription);
  }

  @Test
  public void testFinished() throws RemoteException {
    listener.testFinished(jUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, jUnitDescription);
  }

  @Test
  public void testFailure() throws RemoteException {
    listener.testFailure(jUnitFailure);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableFailure failure = BundleJUnitUtils.getFailure(argument.getValue());
    compareFailure(failure, jUnitFailure);
  }

  @Test
  public void testAssumptionFailure() throws RemoteException {
    listener.testAssumptionFailure(jUnitFailure);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableFailure failure = BundleJUnitUtils.getFailure(argument.getValue());
    compareFailure(failure, jUnitFailure);
  }

  @Test
  public void testIgnored() throws RemoteException {
    listener.testIgnored(jUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, jUnitDescription);
  }

  @Test
  public void addTest() throws RemoteException {
    listener.addTest("exampleTest");
    verify(mockCallback).addTest("exampleTest");
  }

  private static void compareDescription(
      ParcelableDescription parcelableDescription, Description jUnitDescription) {
    assertThat(parcelableDescription.getDisplayName(), is(jUnitDescription.getDisplayName()));
    assertThat(parcelableDescription.getClassName(), is(jUnitDescription.getClassName()));
    assertThat(parcelableDescription.getMethodName(), is(jUnitDescription.getMethodName()));
  }

  private static void compareFailure(ParcelableFailure parcelableFailure, Failure jUnitFailure) {
    assertThat(parcelableFailure.getTrace(), is(jUnitFailure.getTrace()));
    compareDescription(parcelableFailure.getDescription(), jUnitFailure.getDescription());
  }
}
