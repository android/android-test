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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.internal.data.model.ScreenData;
import androidx.test.espresso.internal.data.model.TestArtifact;
import androidx.test.espresso.internal.data.model.TestFlow;
import androidx.test.internal.platform.util.TestOutputEmitter;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A class for visualizing test data. For every action, records screen data to output as a test
 * artifact.
 *
 * <p>Run by setting the custom test argument "enable_testflow_gallery" to true.
 *
 * <p>This is an EXPERIMENTAL FEATURE to assist in Espresso test debuggability.
 */
public class TestFlowVisualizer {
  private static TestFlowVisualizer testFlowVisualizer;
  private static final String TEST_FLOW_ARG = "enable_testflow_gallery";
  private final TestFlow testFlow = new TestFlow();
  private final PlatformTestStorage platformTestStorage;
  private boolean firstWrite;

  @VisibleForTesting
  TestFlowVisualizer(PlatformTestStorage testStorage) {
    this.platformTestStorage = checkNotNull(testStorage);
    firstWrite = true;
  }

  /** Gets an instance of {@link TestFlowVisualizer}. Ensures singleton behavior. */
  public static TestFlowVisualizer getInstance(PlatformTestStorage platformTestStorage) {
    if (testFlowVisualizer != null) {
      if (testFlowVisualizer.platformTestStorage != platformTestStorage) {
        throw new IllegalStateException(
            "getInstance called with different instance of PlatformTestStorage.");
      }
    } else {
      testFlowVisualizer = new TestFlowVisualizer(platformTestStorage);
    }
    return testFlowVisualizer;
  }

  /**
   * Returns whether this feature is enabled.
   *
   * <p>To enable, pass in the --enable_testflow_gallery flag.
   */
  public boolean isEnabled() {
    return platformTestStorage.getInputArgs().containsKey(TEST_FLOW_ARG)
        && Boolean.parseBoolean(platformTestStorage.getInputArg(TEST_FLOW_ARG));
  }

  /** Appends a {@link ScreenData} node to the {@link TestFlow}. */
  public void generateScreenData() {
    ScreenData screen = new ScreenData();
    generateScreenshotTestArtifact(screen);
    testFlow.addScreen(screen);
  }

  /** Dumps a screenshot to be parsed and displayed in an output artifact. */
  public void generateScreenshotTestArtifact(ScreenData parentNode) {
    String pathname = "screenshot-" + testFlow.getSize() + ".png";
    TestOutputEmitter.takeScreenshot(pathname);
    parentNode.addArtifact(new TestArtifact(pathname, ".png"));
    writeHtmlForScreenshot(pathname);
  }

  /**
   * Parses the contents of nodes containing {@link TestArtifact} screenshot data.
   *
   * @param pathname the pathname of the dumped screenshot.
   */
  public void writeHtmlForScreenshot(String pathname) {
    try (PrintStream writer =
        new PrintStream(platformTestStorage.openOutputFile("output_gallery.html", !firstWrite))) {
      writer.append("<img src=\"./").append(pathname).append("\" />\n");
      firstWrite = false;
    } catch (IOException e) {
      Log.e(
          "TestFlowVisualizer",
          "Exception thrown while trying to append screenshot to output artifact",
          e);
    }
  }
}
