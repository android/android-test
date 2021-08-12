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
import static androidx.test.internal.util.Checks.checkState;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

import android.graphics.Rect;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.internal.data.model.ActionData;
import androidx.test.espresso.internal.data.model.ScreenData;
import androidx.test.espresso.internal.data.model.TestArtifact;
import androidx.test.espresso.internal.data.model.TestFlow;
import androidx.test.espresso.internal.data.model.ViewData;
import androidx.test.internal.platform.util.TestOutputEmitter;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
  private final TestFlow testFlow;
  private final PlatformTestStorage platformTestStorage;
  private static final String LOG_TAG = "TestFlowVisualizer";
  private int actionIndex = 0;
  private Boolean enabled;

  TestFlowVisualizer(PlatformTestStorage testStorage) {
    this(testStorage, new TestFlow());
  }

  @VisibleForTesting
  TestFlowVisualizer(PlatformTestStorage testStorage, TestFlow testFlow) {
    this.platformTestStorage = checkNotNull(testStorage);
    this.testFlow = checkNotNull(testFlow);
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
    if (enabled == null) {
      enabled =
          platformTestStorage.getInputArgs().containsKey(TEST_FLOW_ARG)
              && Boolean.parseBoolean(platformTestStorage.getInputArg(TEST_FLOW_ARG));
    }
    return enabled;
  }

  public int getLastActionIndexAndIncrement() {
    int index = actionIndex;
    actionIndex++;
    return index;
  }

  public int getLastActionIndex() {
    return this.actionIndex;
  }

  /**
   * Appends a {@link ScreenData} node to the {@link TestFlow}.
   *
   * <p>Must be called before an action occurs, with afterActionRecordData after the action.
   *
   * <p>Must be called on main thread.
   *
   * @param actionData Data pertaining to a ViewAction to be performed.
   * @param view The view an action is performed on.
   */
  public void beforeActionRecordData(ActionData actionData, View view) {
    // TODO(b/196263898): Fix currently-required sequential calling of data recording functions
    // TODO(b/196264377): Allow for appending data to ActionData upon test completion.
    checkState(
        Thread.currentThread().equals(Looper.getMainLooper().getThread()),
        "Method cannot be called off the main application thread (on: %s)",
        Thread.currentThread().getName());
    checkNotNull(actionData, "Requires actionData to store in graph.");
    checkNotNull(view, "Requires View to analyze.");
    if (actionData.getIndex() == null) {
      throw new IllegalStateException("ActionData must have a distinguishing index.");
    }
    if (testFlow.getEdge(actionData.getIndex()) != null) {
      throw new IllegalStateException(
          "Currently appending to existing ActionData objects is not supported.");
    }
    Rect visibleParts = new Rect();
    view.getGlobalVisibleRect(visibleParts);
    ScreenData screen = new ScreenData();
    screen.addViewData(new ViewData(view.toString(), adjustViewCoords(view), visibleParts));
    testFlow.addScreen(screen);
  }

  /**
   * Appends a {@link ScreenData} node to the {@link TestFlow}. Sets {@link ActionData} members and
   * displays them.
   *
   * <p>Must be called after an action occurs, with beforeActionRecordData before the action.
   *
   * <p>Must be called on main thread.
   *
   * @param actionData The viewAction being performed.
   */
  public void afterActionRecordData(ActionData actionData) {
    checkState(
        Thread.currentThread().equals(Looper.getMainLooper().getThread()),
        "Method cannot be called off the main application thread (on: %s)",
        Thread.currentThread().getName());
    checkNotNull(actionData, "Requires ActionData to store in graph.");
    ScreenData currScreen = testFlow.getTail();
    ScreenData nextScreen = new ScreenData();
    actionData.source = currScreen;
    actionData.dest = nextScreen;
    testFlow.addScreen(nextScreen, actionData);
  }

  public void beforeActionGenerateTestArtifact(int actionIndex) {
    TestOutputEmitter.takeScreenshot("screenshot-before-" + actionIndex + ".png");
  }

  public void afterActionGenerateTestArtifact(int actionIndex) {
    TestOutputEmitter.takeScreenshot("screenshot-after-" + actionIndex + ".png");
  }

  /**
   * Restricts the sometimes-unset lower coordinates of the view box.
   *
   * @param view The Espresso test's view.
   * @return The new array of coordinates.
   */
  private Rect adjustViewCoords(View view) {
    float[] tl = GeneralLocation.TOP_LEFT.calculateCoordinates(view);
    float[] br = GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
    // TODO(b/196263288): Replace with programmatically retrieved screen size
    br[1] = min(br[1], 800);
    return new Rect((int) tl[0], (int) tl[1], (int) br[0], (int) br[1]);
  }

  /**
   * Traverses the TestFlow graph and parses data to html.
   *
   * <p>TODO(b/196264719): Move this to a TestRule.
   */
  public void visualize() {
    try (PrintStream writer =
        new PrintStream(platformTestStorage.openOutputFile("output_gallery.html"))) {
      ScreenData curr = testFlow.getHead();
      if (curr == null) {
        Log.d(LOG_TAG, "Exiting process 'visualize()', TestFlow graph is empty.");
        return;
      }
      testFlow.resetTraversal();
      setStyling(writer);
      int actionCounter = 0;
      while (!curr.getActions().isEmpty() && curr.getActionIndex() < curr.getActions().size()) {
        // before action occurs
        beginActionOutput(writer);
        String pathname = "screenshot-before-" + actionCounter + ".png";
        curr.addArtifact(new TestArtifact(pathname, ".png"));
        displayScreenshot(pathname, writer);
        // action
        if (curr.getActions().isEmpty()) {
          return;
        }
        ActionData action = curr.getActions().get(curr.getActionIndex());
        List<ViewData> views = curr.getViews();
        if (action.getDesc() != null) {
          // View data not reliable for scroll actions.
          if (!action.getDesc().contains("scroll") && !curr.getViews().isEmpty()) {
            for (ViewData element : views) {
              displayViewData(element, writer);
            }
          } else {
            writer.append("<div class=\"action-item\">");
          }
          displayActionData(action, writer);
        } else if (!views.isEmpty()) {
          for (ViewData element : views) {
            displayViewData(element, writer);
          }
        }
        ScreenData temp = action.getDest();
        curr.setActionIndex(curr.getActionIndex() + 1);
        // after action occurs
        pathname = "screenshot-after-" + actionCounter + ".png";
        curr.addArtifact(new TestArtifact(pathname, ".png"));
        displayScreenshot(pathname, writer);
        if (!temp.getActions().isEmpty() && temp.getActions().get(temp.getActionIndex()) != null) {
          curr = temp.getActions().get(temp.getActionIndex()).getDest();
          temp.setActionIndex(temp.getActionIndex() + 1);
        }
        endActionOutput(writer);
        actionCounter++;
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Exception thrown while trying to display TestFlow.", e);
    }
  }

  /** Displays the {@link ViewData}. */
  private void displayViewData(ViewData viewData, PrintStream writer) {
    Rect viewBox = viewData.getViewBox();
    Rect visible = viewData.getVisibleViewBox();
    int x0 = viewBox.left;
    int x1 = viewBox.right;
    int y0 = viewBox.top;
    int y1 = viewBox.bottom;
    writer.append(
        format(
            Locale.ENGLISH,
            "<div style=\"border:3px solid rgba(255, 0, 0, .5); width:%d; height:%d",
            visible.right - visible.left,
            visible.bottom - (visible.top + 3)));
    writer.append(
        format(
            Locale.ENGLISH,
            "px; position:absolute; top:%dpx; left: %dpx; z-index:10;\"></div>",
            visible.top - 3,
            visible.left - 3));
    writer.append(
        format(
            Locale.ENGLISH,
            "<div style=\"border:3px solid rgba(0, 0, 255, .5); width:%s; height:%s",
            x1 - x0,
            y1 - (y0 + 3)));
    writer.append(
        String.format(
            Locale.ENGLISH,
            "; position:absolute; top:%spx; left: %spx; z-index:9;\"></div>",
            y0 - 3,
            x0 - 3));
    writer.append("<div class=\"action-item\">");
    writer.append("<div style=\"border:3px solid rgba(255, 0, 0, .5);\">Visible View</div>");
    writer.append("<div style=\"border:3px solid rgba(0, 0, 255, .5);\">Actual View</div>");
    writer.append(format(Locale.ENGLISH, "<p>%s</p>", viewData.getDesc()));
    writer.append(String.format("View: %s<br />", viewBox));
    writer.append(
        format(Locale.ENGLISH, "<p>Visible portion: %s</p>", Objects.requireNonNull(visible)));
    float percentVisible =
        max(
            min(((float) visible.bottom - (float) visible.top) / (y1 - y0), 1)
                * min(((float) visible.right - (float) visible.left) / (x1 - x0), 1)
                * 100,
            0);
    writer.append(String.format(Locale.ENGLISH, "This view is %s%% visible.", percentVisible));
  }

  /**
   * Displays the {@link ActionData} members.
   *
   * @param action a {@link ActionData} object.
   */
  private void displayActionData(ActionData action, PrintStream writer) {
    if (action.getName() != null) {
      writer.append(format(Locale.getDefault(), "<p>Classname: %s</p>", action.getName()));
    }
    if (action.getDesc() != null) {
      writer.append(format(Locale.getDefault(), "<p>Description: %s</p>", action.getDesc()));
    }
    if (action.getConstraints() != null) {
      writer.append(
          format(
              Locale.getDefault(),
              "<p>Constraints: %s</p>",
              action.getConstraints().replace('<', '(').replace('>', ')')));
    }
    writer.append("</div>");
  }

  /** Appends opening wrappers for action data to be displayed. */
  private void beginActionOutput(PrintStream writer) {
    writer.append("<div class=\"action\"><div style=\"position:relative; display:inline-block;\">");
  }

  /** Appends closing wrappers of action data to be displayed. */
  private void endActionOutput(PrintStream writer) {
    writer.append("</div></div>");
  }

  /**
   * Appends html stylings to document.
   *
   * @param writer writes html stylings.
   */
  private void setStyling(PrintStream writer) {
    writer.append("<style>\n.action-item {\ndisplay:inline-block;\nwidth:450px;\n");
    writer.append("margin-left:10px;\nmargin-right:10px;\n}\n</style>");
  }

  /**
   * Parses the contents of nodes containing {@link TestArtifact} screenshot data.
   *
   * @param pathname the pathname of the dumped screenshot.
   */
  private void displayScreenshot(String pathname, PrintStream writer) {
    // TODO(b/196263288): Replace with programmatically retrieved screen size.
    writer.append("<div style=\"width:480px; display: inline-block\">");
    writer.append(format(Locale.ENGLISH, "<img src=\"./%s\" />\n", pathname));
    writer.append("</div>");
  }
}
