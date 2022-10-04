/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import androidx.test.internal.runner.listener.TraceRunListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowTrace;

/** Robolectric unit test for {@link TraceRunListener}. */
@RunWith(RobolectricTestRunner.class)
public final class TraceRunListenerTest {
  @Before
  public void setUp() throws Exception {
    ShadowTrace.setEnabled(true);
  }

  @After
  public void tearDown() throws Exception {
    ShadowTrace.reset();
  }

  @Test
  public void createsSection_withShortName() throws Exception {
    TraceRunListener listener = new TraceRunListener();
    Description description =
        Description.createTestDescription(ClassUnderTest.class, "theMethodName");

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();

    listener.testStarted(description);
    listener.testFinished(description);

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).containsExactly("ClassUnderTest#theMethodName");
  }

  @Test
  public void createsSection_withLongName() throws Exception {
    TraceRunListener listener = new TraceRunListener();
    Description description =
        Description.createTestDescription(
            ClassUnderTest.class,
            "anExcessivelyLongDescriptionStringWithLengthAboveThe127CharLimitWillBeTrimmedToAvoidThrowingAnExceptionFromBeginSpan");

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();

    listener.testStarted(description);
    listener.testFinished(description);

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections())
        .containsExactly(
            "ClassUnderTest#anExcessivelyLongDescriptionStringWithLengthAboveThe127CharLimitWillBeTrimmedToAvoidThrowingAnExceptionFromBegin");
  }

  private static final class ClassUnderTest {}
}
