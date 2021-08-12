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

package androidx.test.espresso.action;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import androidx.test.espresso.internal.data.TestFlowVisualizer;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.io.PlatformTestStorageRegistry;
import androidx.test.services.storage.TestStorage;
import androidx.test.services.storage.internal.TestStorageUtil;
import androidx.test.ui.app.LargeViewActivity;
import androidx.test.ui.app.R;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Instrumentation tests for {@link TestFlowVisualizer} on a large view. */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoActionsWithTestFlowTest {

  @Rule
  public ActivityScenarioRule<LargeViewActivity> rule =
      new ActivityScenarioRule<>(LargeViewActivity.class);

  /**
   * Shows how to use TestFlowVisualizer in an instrumentation test. Enable {@link
   * TestFlowVisualizer} with '-test_arg=--test_args=enable_testflow_gallery=true'.
   */
  @Test
  public void testFlowVisualizerTemplate() {
    onView(withId(R.id.large_view)).check(matches(withText("large view")));
    onView(withId(R.id.large_view)).perform(click());
    onView(withId(R.id.large_view)).check(matches(withText("Ouch!!!")));

    TestFlowVisualizer testFlowVisualizer =
        TestFlowVisualizer.getInstance(PlatformTestStorageRegistry.getInstance());
    if (testFlowVisualizer.isEnabled()) {
      testFlowVisualizer.visualize();
    }
  }

  /**
   * Copy of {@link ClickActionIntegrationTest} test with additional {@link TestFlowVisualizer}
   * functionality.
   *
   * <p>TODO(b/196265716): Include comprehensive assertions on the View box html elements to assure
   * correctness.
   */
  @Test
  public void clickActionTestingOfTestFlowOutput() throws IOException {
    onView(withId(R.id.large_view)).check(matches(withText("large view")));
    onView(withId(R.id.large_view)).perform(click());
    onView(withId(R.id.large_view)).check(matches(withText("Ouch!!!")));
    onView(withId(R.id.large_view)).perform(click());
    onView(withId(R.id.large_view)).check(matches(withText("Ouch!!!")));
    TestFlowVisualizer testFlowVisualizer =
        TestFlowVisualizer.getInstance(PlatformTestStorageRegistry.getInstance());
    if (testFlowVisualizer.isEnabled()) {
      testFlowVisualizer.visualize();
      assertThat(testFlowVisualizer.getLastActionIndexAndIncrement()).isEqualTo(2);
      InputStream outputGalleryFile =
          TestStorageUtil.getInputStream(
              TestStorage.getOutputFileUri("output_gallery.html"),
              InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver());
      int size = outputGalleryFile.available();
      byte[] directFileContents = new byte[size];
      outputGalleryFile.read(directFileContents);
      String stringFileContents = new String(directFileContents);
      assertThat(stringFileContents).contains("<img src=\"./screenshot-before-0.png\" />");
      assertThat(stringFileContents).contains("<img src=\"./screenshot-after-0.png\" />");
      assertThat(stringFileContents).contains("<img src=\"./screenshot-before-1.png\" />");
      assertThat(stringFileContents).contains("<img src=\"./screenshot-after-1.png\" />");
      assertThat(stringFileContents).contains("View: Rect(0, 120 - 479, 800)");
      assertThat(stringFileContents).contains("<p>Visible portion: Rect(0, 120 - 480, 800)</p>");
      assertThat(stringFileContents).contains("<p>Classname: GeneralClickAction</p>");
      assertThat(stringFileContents).contains("<p>Description: single click</p>");
      String constraints =
          "Constraints: (view has effective visibility (VISIBLE) and view.getGlobalVisibleRect()"
              + " covers at least (90) percent of the view's area)";
      assertThat(stringFileContents).contains(constraints);
    }
  }

  /**
   * Ensure that if the feature is disabled from the instrumentation test, no output gallery will be
   * generated from a {@link TestFlowVisualizer#visualize()} invocation.
   */
  @Test
  public void testFlowVisualizerIfFeatureIsOffShowBreakage() {
    onView(withId(R.id.large_view)).check(matches(withText("large view")));
    onView(withId(R.id.large_view)).perform(click());
    onView(withId(R.id.large_view)).check(matches(withText("Ouch!!!")));
    TestFlowVisualizer testFlowVisualizer =
        TestFlowVisualizer.getInstance(PlatformTestStorageRegistry.getInstance());
    // Simulates the test argument not being present in test invocation.
    if (!testFlowVisualizer.isEnabled()) {
      testFlowVisualizer.visualize();
    }
    if (testFlowVisualizer.isEnabled()) {
      assertThrows(
          "No entry for content://androidx.test.services.storage.outputfiles/output_gallery.html",
          FileNotFoundException.class,
          () ->
              TestStorageUtil.getInputStream(
                  TestStorage.getOutputFileUri("output_gallery.html"),
                  InstrumentationRegistry.getInstrumentation()
                      .getTargetContext()
                      .getContentResolver()));
    }
  }
}
