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

  private OrchestratedInstrumentationListener mListener;
  private Description mJUnitDescription;
  private Failure mJUnitFailure;
  private Result mJUnitResult;

  @Override
  public void onOrchestratorConnect() {
    // Do nothing
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    mListener = new OrchestratedInstrumentationListener(this);
    mListener.odoCallback = mockCallback;

    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    mJUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    mJUnitFailure = new Failure(mJUnitDescription, new Throwable("error"));
    mJUnitResult = new Result();
    RunListener jUnitListener = mJUnitResult.createListener();
    jUnitListener.testRunStarted(mJUnitDescription);
    jUnitListener.testStarted(mJUnitDescription);
    jUnitListener.testFinished(mJUnitDescription);
  }

  @Test
  public void nullCallbackThrowsException() {
    try {
      mListener.odoCallback = null;
      mListener.testRunStarted(mJUnitDescription);
      fail("Listener should throw an error if the callback is null");
    } catch (IllegalStateException e) {
      // as expected
    }
  }

  @Test
  public void testRunStarted() throws RemoteException {
    mListener.testRunStarted(mJUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, mJUnitDescription);
  }

  @Test
  public void testRunFinished() throws RemoteException {
    mListener.testRunFinished(mJUnitResult);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableResult result = BundleJUnitUtils.getResult(argument.getValue());
    assertThat(result.wasSuccessful(), is(mJUnitResult.wasSuccessful()));
  }

  @Test
  public void testStarted() throws RemoteException {
    mListener.testStarted(mJUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, mJUnitDescription);
  }

  @Test
  public void testFinished() throws RemoteException {
    mListener.testFinished(mJUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, mJUnitDescription);
  }

  @Test
  public void testFailure() throws RemoteException {
    mListener.testFailure(mJUnitFailure);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableFailure failure = BundleJUnitUtils.getFailure(argument.getValue());
    compareFailure(failure, mJUnitFailure);
  }

  @Test
  public void testAssumptionFailure() throws RemoteException {
    mListener.testAssumptionFailure(mJUnitFailure);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableFailure failure = BundleJUnitUtils.getFailure(argument.getValue());
    compareFailure(failure, mJUnitFailure);
  }

  @Test
  public void testIgnored() throws RemoteException {
    mListener.testIgnored(mJUnitDescription);
    ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
    verify(mockCallback).sendTestNotification(argument.capture());

    ParcelableDescription description = BundleJUnitUtils.getDescription(argument.getValue());
    compareDescription(description, mJUnitDescription);
  }

  @Test
  public void addTest() throws RemoteException {
    mListener.addTest("exampleTest");
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
