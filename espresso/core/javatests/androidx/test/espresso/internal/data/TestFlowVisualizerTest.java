/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.espresso.internal.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;

/** Small tests for {@link TestFlowVisualizer}. */
@RunWith(AndroidJUnit4.class)
public class TestFlowVisualizerTest {
  @Mock private PlatformTestStorage testStorage1;
  @Mock PlatformTestStorage testStorage2;
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setup() throws IOException {
    when(testStorage1.openOutputFile(eq("output_gallery.html"), anyBoolean()))
        .thenReturn(outputStream);
  }

  @Test
  public void checkTestFlowCreationWithTestStorage() {
    assertThat(TestFlowVisualizer.getInstance(testStorage1))
        .isSameInstanceAs(TestFlowVisualizer.getInstance(testStorage1));
    assertThrows(
        "getInstance called with different arguments.",
        IllegalStateException.class,
        () -> TestFlowVisualizer.getInstance(testStorage2));
  }

  @Test
  public void checkThereAreNoScreenshotsWithoutInvocation() {
    assertThat(outputStream.toString()).doesNotContain("<img src=\"./screenshot-0.png\" />");
  }

  @Test
  public void checkOutputGalleryHasOneScreenshotWhenInvokedOnce() throws IOException {
    TestFlowVisualizer testFlowVisualizer = new TestFlowVisualizer(testStorage1);
    testFlowVisualizer.generateScreenData();
    assertThat(outputStream.toString()).contains("<img src=\"./screenshot-0.png\" />");
    assertThat(outputStream.toString()).doesNotContain("<img src=\"./screenshot-1.png\" />");
  }

  @Test
  public void checkOutputGalleryHasCorrectNumOfScreenshots() throws IOException {
    TestFlowVisualizer testFlowVisualizer = new TestFlowVisualizer(testStorage1);
    testFlowVisualizer.generateScreenData();
    testFlowVisualizer.generateScreenData();
    assertThat(outputStream.toString()).contains("<img src=\"./screenshot-0.png\" />");
    assertThat(outputStream.toString()).contains("<img src=\"./screenshot-1.png\" />");
    assertThat(outputStream.toString()).doesNotContain("<img src=\"./screenshot-2.png\" />");
  }
}
