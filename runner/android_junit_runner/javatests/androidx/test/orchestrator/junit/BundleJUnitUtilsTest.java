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
package androidx.test.orchestrator.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.orchestrator.SampleJUnitTest;
import androidx.test.services.events.internal.StackTrimmer;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/** Unit tests for {@link BundleJUnitUtils}. */
@RunWith(AndroidJUnit4.class)
public class BundleJUnitUtilsTest {

  @Test
  public void fromDescription() {
    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    Description jUnitDescription = Description.createTestDescription(testClass, "sampleTest");

    ParcelableDescription parcelableDescription =
        BundleJUnitUtils.getDescription(
            parcelBundle(BundleJUnitUtils.getBundleFromDescription(jUnitDescription)));

    compareDescription(parcelableDescription, jUnitDescription);
  }

  @Test
  public void fromFailure() {
    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    Description jUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    Throwable throwable = new RuntimeException("Your test is bad and you should feel bad.");
    Failure jUnitFailure = new Failure(jUnitDescription, throwable);

    ParcelableFailure parcelableFailure =
        BundleJUnitUtils.getFailure(
            parcelBundle(BundleJUnitUtils.getBundleFromFailure(jUnitFailure)));

    assertThat(parcelableFailure.getTrace(), is(StackTrimmer.getTrimmedStackTrace(jUnitFailure)));
    compareDescription(parcelableFailure.getDescription(), jUnitFailure.getDescription());
  }

  @Test
  public void fromThrowable() {
    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    Description jUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    Throwable throwable = new RuntimeException("Your test is bad and you should feel bad.");

    ParcelableFailure parcelableFailure =
        BundleJUnitUtils.getFailure(
            parcelBundle(BundleJUnitUtils.getBundleFromThrowable(jUnitDescription, throwable)));

    assertThat(parcelableFailure.getTrace(), is(throwable.getMessage() + '\n'));
    compareDescription(parcelableFailure.getDescription(), jUnitDescription);
  }

  @Test
  public void fromResult_success() throws Exception {
    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    Description jUnitDescription = Description.createTestDescription(testClass, "sampleTest");

    Result jUnitResult = new Result();
    RunListener jUnitListener = jUnitResult.createListener();
    jUnitListener.testRunStarted(jUnitDescription);
    jUnitListener.testStarted(jUnitDescription);
    jUnitListener.testFinished(jUnitDescription);

    ParcelableResult parcelableResult =
        BundleJUnitUtils.getResult(parcelBundle(BundleJUnitUtils.getBundleFromResult(jUnitResult)));

    assertThat(parcelableResult.wasSuccessful(), is(jUnitResult.wasSuccessful()));
  }

  @Test
  public void fromResult_failure() throws Exception {
    Class<SampleJUnitTest> testClass = SampleJUnitTest.class;
    Description jUnitDescription = Description.createTestDescription(testClass, "sampleTest");
    Throwable throwable = new RuntimeException("Your test is bad and you should feel bad.");
    Failure jUnitFailure = new Failure(jUnitDescription, throwable);

    Result jUnitResult = new Result();
    RunListener jUnitListener = jUnitResult.createListener();
    jUnitListener.testRunStarted(jUnitDescription);
    jUnitListener.testStarted(jUnitDescription);
    jUnitListener.testFailure(jUnitFailure);
    jUnitListener.testFinished(jUnitDescription);

    ParcelableResult parcelableResult =
        BundleJUnitUtils.getResult(parcelBundle(BundleJUnitUtils.getBundleFromResult(jUnitResult)));

    assertThat(parcelableResult.wasSuccessful(), is(jUnitResult.wasSuccessful()));
    assertThat(parcelableResult.getFailureCount(), is(jUnitResult.getFailureCount()));
    compareFailure(parcelableResult.getFailures().get(0), jUnitResult.getFailures().get(0));
  }

  private static void compareDescription(
      ParcelableDescription parcelableDescription, Description jUnitDescription) {
    assertThat(parcelableDescription.getDisplayName(), is(jUnitDescription.getDisplayName()));
    assertThat(parcelableDescription.getClassName(), is(jUnitDescription.getClassName()));
    assertThat(parcelableDescription.getMethodName(), is(jUnitDescription.getMethodName()));
  }

  private static void compareFailure(ParcelableFailure parcelableFailure, Failure jUnitFailure) {
    assertThat(parcelableFailure.getTrace(), is(StackTrimmer.getTrimmedStackTrace(jUnitFailure)));
    compareDescription(parcelableFailure.getDescription(), jUnitFailure.getDescription());
  }

  // We want to both parcel and unparcel the bundle to ensure the contents are themselves
  // are parcelled and unparcelled.
  private Bundle parcelBundle(Bundle in) {
    Parcel parcel = Parcel.obtain();
    in.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    Bundle out = Bundle.CREATOR.createFromParcel(parcel);

    out.setClassLoader(this.getClass().getClassLoader());
    // Sanity check that the robolectric shadows haven't done something tricky like give us
    // the same object back instead of a reconstructed bundle.
    assertThat(in, is(not(out)));
    return out;
  }
}
