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

import androidx.test.espresso.internal.data.model.ActionData;
import androidx.test.espresso.internal.data.model.ScreenData;
import androidx.test.espresso.internal.data.model.TestFlow;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.platform.io.PlatformTestStorageRegistry;
import androidx.test.services.storage.TestStorage;
import androidx.test.services.storage.internal.TestStorageUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
  @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testFlowVisualizerGetInstanceThrowsIllegalStateExceptionIfTestStorageMismatch() {
    assertThat(TestFlowVisualizer.getInstance(testStorage1))
        .isSameInstanceAs(TestFlowVisualizer.getInstance(testStorage1));
    assertThrows(
        "getInstance called with different arguments.",
        IllegalStateException.class,
        () -> TestFlowVisualizer.getInstance(testStorage2));
  }

  @Test
  public void testFlowVisualizerEnsureCorrectOutputForSingleDummyAction() throws IOException {
    TestFlow testFlow = new TestFlow();
    PlatformTestStorage platformTestStorage = PlatformTestStorageRegistry.getInstance();
    TestFlowVisualizer testFlowVisualizer = new TestFlowVisualizer(platformTestStorage, testFlow);
    testFlow.addScreen(new ScreenData());
    ScreenData screenData2 = new ScreenData();
    ActionData action = new ActionData(0, "DummyClassName", "DummyDescription", "DummyConstraints");
    action.source = testFlow.getHead();
    action.dest = screenData2;
    testFlow.addScreen(screenData2, action);
    assertThat(testFlow.getSize()).isEqualTo(2);
    testFlowVisualizer.visualize();
    InputStream outputGalleryFile =
        TestStorageUtil.getInputStream(
            TestStorage.getOutputFileUri("output_gallery.html"),
            InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver());
    int size = outputGalleryFile.available();
    byte[] directFileContents = new byte[size];
    outputGalleryFile.read(directFileContents);
    String stringFileContents = new String(directFileContents, Charset.defaultCharset());
    assertThat(stringFileContents).contains("<img src=\"./screenshot-before-0.png\" />");
    assertThat(stringFileContents).contains("<img src=\"./screenshot-after-0.png\" />");
    assertThat(stringFileContents).contains("Classname: DummyClassName");
    assertThat(stringFileContents).contains("Description: DummyDescription");
    assertThat(stringFileContents).contains("Constraints: DummyConstraints");
  }
}
