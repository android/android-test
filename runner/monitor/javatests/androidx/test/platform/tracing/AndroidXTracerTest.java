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

package androidx.test.platform.tracing;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.platform.tracing.Tracer.Span;
import androidx.tracing.Trace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowTrace;

/** Unit test for {@link AndroidXTracer}. */
@RunWith(RobolectricTestRunner.class)
public class AndroidXTracerTest {

  @Before
  public void setUp() throws Exception {
    ShadowTrace.setEnabled(true);
  }

  @After
  public void tearDown() throws Exception {
    ShadowTrace.reset();
  }

  // Validates the current Robolectric test is configured with an SDK level that
  // actually captures AndroidX tracing sessions. JB MR2 (API 18) or + is required.
  @Test
  public void validateSdkUsesAndroidXTracingSession() {
    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();

    Trace.beginSection("RootSection");

    assertThat(ShadowTrace.getCurrentSections()).containsExactly("RootSection");
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();

    // Sections can be nested. Sections are recorded by the shadow when they are closed,
    // which essentially reverses the order in the assertion check below.
    Trace.beginSection("NestedSection");
    Trace.endSection();
    Trace.endSection();

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections())
        .containsExactly("NestedSection", "RootSection")
        .inOrder();
  }

  @Test
  public void beginSpan() {
    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();

    AndroidXTracer tracer = new AndroidXTracer();
    try (Span span1 = tracer.beginSpan("span1")) {
      assertThat(span1).isNotNull();

      try (Span span11 = span1.beginChildSpan("span11")) {
        assertThat(span11).isNotNull();
      }
      try (Span span12 = span1.beginChildSpan("span12")) {
        assertThat(span12).isNotNull();
      }
    }

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections())
        .containsExactly("span11", "span12", "span1")
        .inOrder();
  }
}
