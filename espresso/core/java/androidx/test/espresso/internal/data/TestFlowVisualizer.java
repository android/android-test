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
      if (firstWrite) {
        addHtmlStyling(writer);
        firstWrite = false;
      }
      writer
          .append("<img id=\"")
          .append(Integer.toString(testFlow.getSize()))
          .append("\"src=\"./")
          .append(pathname)
          .append("\" />\n");
    } catch (IOException e) {
      Log.e(
          "TestFlowVisualizer",
          "Exception thrown while trying to append screenshot to output artifact",
          e);
    }
  }

  /** Saves styling tags to the displayed artifact. */
  public void addHtmlStyling(PrintStream writer) {
    writer.append("<body onload = \"setup()\">\n");
    writer.append("<style>\n");
    writer.append("  img { display:none; }\n");
    writer.append("  .nav-button {\n");
    writer.append("    height:25px;\n");
    writer.append("    width:100px;\n");
    writer.append("    border:2px solid black;\n");
    writer.append("    display:inline-block;\n");
    writer.append("    text-align:center;\n");
    writer.append("    margin:2px;\n");
    writer.append("  }\n");
    writer.append("</style>\n");
    writer.append("<script>\n");
    writer.append("  var slider = 0;\n");
    writer.append("  function setup() { \n");
    writer.append("    var chooser = document.getElementById(\"chooser\");\n");
    writer.append("    document.getElementById(\"0\").style.display=\"block\"; \n");
    writer.append("    chooser.value = 0;\n");
    writer.append("    chooser.addEventListener(\"change\", change);\n");
    writer.append("    document.addEventListener(\"keydown\", keypress => {\n");
    writer.append("      let keycode = keypress.keyCode\n");
    writer.append("      if (keycode === 74 || keycode === 39) {increment();}\n");
    writer.append("      else if (keycode === 75 || keycode === 37) { decrement(); }\n");
    writer.append("    });\n");
    writer.append("  }\n");
    writer.append("  function decrement() {\n");
    writer.append("    if (slider <= 0) { return; }\n");
    writer.append("    document.getElementById(slider.toString()).style.display=\"none\";\n");
    writer.append("    document.getElementById((slider-1).toString()).style.display=\"block\";\n");
    writer.append("    slider--;\n");
    writer.append("    document.getElementById(\"chooser\").value = slider;\n");
    writer.append("  }\n");
    writer.append("  function increment(){\n");
    writer.append("    let next = document.getElementById((slider+1).toString());\n");
    writer.append("    if (next != null) {\n");
    writer.append("      document.getElementById(slider.toString()).style.display=\"none\";\n");
    writer.append("      next.style.display=\"block\";\n");
    writer.append("      slider++;\n");
    writer.append("      document.getElementById(\"chooser\").value = slider; \n");
    writer.append("    }\n");
    writer.append("  }\n");
    writer.append("  function change() {\n");
    writer.append("    var chooser = document.getElementById(\"chooser\");\n");
    writer.append("    var slide = document.getElementById((chooser.value).toString());\n");
    writer.append("    if (slide == null) {\n");
    writer.append("      chooser.value = slider;\n");
    writer.append("    } else {\n");
    writer.append("      document.getElementById(slider.toString()).style.display=\"none\";\n");
    writer.append("      slide.style.display=\"block\";\n");
    writer.append("      slider = chooser.value;\n");
    writer.append("    }\n");
    writer.append("  }\n");
    writer.append("</script>\n");
    writer.append("<div>\n");
    writer.append("<div id=\"previous\" class=\"nav-button\" onclick=\"decrement()\"><</div>\n");
    writer.append("<div style=\"display:inline-block\"><input id=\"chooser\" type=\"number\">");
    writer.append("</div>\n");
    writer.append("<div id=\"next\" class=\"nav-button\" onclick=\"increment()\">></div>\n");
  }
}
