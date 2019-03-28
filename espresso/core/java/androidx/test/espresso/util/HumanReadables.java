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

package androidx.test.espresso.util;

import static androidx.test.espresso.util.TreeIterables.depthFirstViewTraversalWithDistance;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.util.Printer;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Checkable;
import android.widget.TextView;
import androidx.test.espresso.util.TreeIterables.ViewAndDistance;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Locale;

/** Text converters for various Android objects. */
public final class HumanReadables {

  private HumanReadables() {}

  /**
   * Prints out an error message feature the view hierarchy starting at the rootView.
   *
   * @param rootView the root of the hierarchy tree to print out.
   * @param problemViews list of the views that you would like to point out are causing the error
   *     message or null, if you want to skip this feature.
   * @param errorHeader the header of the error message (should contain the description of why the
   *     error is happening).
   * @param problemViewSuffix the message to append to the view description in the tree printout.
   *     Required if problemViews is supplied. Otherwise, null is acceptable.
   * @return a string for human consumption.
   */
  public static String getViewHierarchyErrorMessage(
      View rootView,
      final List<View> problemViews,
      String errorHeader,
      final String problemViewSuffix) {
    Preconditions.checkArgument(problemViews == null || problemViewSuffix != null);
    StringBuilder errorMessage = new StringBuilder(errorHeader);
    if (problemViewSuffix != null) {
      errorMessage.append(
          String.format(
              Locale.ROOT, "\nProblem views are marked with '%s' below.", problemViewSuffix));
    }

    errorMessage.append("\n\nView Hierarchy:\n");

    Joiner.on("\n")
        .appendTo(
            errorMessage,
            Iterables.transform(
                depthFirstViewTraversalWithDistance(rootView),
                new Function<ViewAndDistance, String>() {
                  @Override
                  public String apply(ViewAndDistance viewAndDistance) {
                    String formatString = "+%s%s ";
                    if (problemViews != null && problemViews.contains(viewAndDistance.getView())) {
                      formatString += problemViewSuffix;
                    }
                    formatString += "\n|";

                    return String.format(
                        Locale.ROOT,
                        formatString,
                        Strings.padStart(">", viewAndDistance.getDistanceFromRoot() + 1, '-'),
                        HumanReadables.describe(viewAndDistance.getView()));
                  }
                }));

    return errorMessage.toString();
  }

  public static String describe(Cursor c) {
    if (c.isBeforeFirst()) {
      return "Cursor positioned before first element.";
    } else if (c.isAfterLast()) {
      return "Cursor positioned after last element.";
    }
    StringBuilder result = new StringBuilder("Row ").append(c.getPosition()).append(": {");
    String[] columns = c.getColumnNames();
    for (int i = 0; i < columns.length; i++) {
      result.append(columns[i]).append(":");
      int type = Cursor.FIELD_TYPE_STRING;
      if (Build.VERSION.SDK_INT > 10) {
        type = c.getType(i);
      }
      switch (type) {
        case Cursor.FIELD_TYPE_STRING:
          result.append("\"").append(c.getString(i)).append("\"");
          break;
        case Cursor.FIELD_TYPE_INTEGER:
          result.append(c.getLong(i));
          break;
        case Cursor.FIELD_TYPE_FLOAT:
          result.append(c.getDouble(i));
          result.append("f");
          break;
        case Cursor.FIELD_TYPE_NULL:
          result.append("null");
          break;
        case Cursor.FIELD_TYPE_BLOB:
          byte[] val = c.getBlob(i);
          result.append("[");
          for (int j = 0; j < 5 && j < val.length; j++) {
            result.append(val[j]);
            result.append(",");
          }
          if (5 < val.length) {
            result.append("... (").append(val.length - 5).append(" more elements)");
          }
          result.append("]");
          break;
        default:
          result.append("\"").append(c.getString(i)).append("\"");
          break;
      }
      result.append(", ");
    }
    result.append("}");
    return result.toString();
  }

  /**
   * Transforms an arbitrary view into a string with (hopefully) enough debug info.
   *
   * @param v nullable view
   * @return a string for human consumption.
   */
  public static String describe(View v) {
    if (null == v) {
      return "null";
    }
    ToStringHelper helper = MoreObjects.toStringHelper(v).add("id", v.getId());
    if (v.getId() != -1 && v.getResources() != null && !isViewIdGenerated(v.getId())) {
      try {
        helper.add("res-name", v.getResources().getResourceEntryName(v.getId()));
      } catch (Resources.NotFoundException ignore) {
        // Do nothing.
      }
    }
    if (null != v.getContentDescription()) {
      helper.add("desc", v.getContentDescription());
    }

    switch (v.getVisibility()) {
      case View.GONE:
        helper.add("visibility", "GONE");
        break;
      case View.INVISIBLE:
        helper.add("visibility", "INVISIBLE");
        break;
      case View.VISIBLE:
        helper.add("visibility", "VISIBLE");
        break;
      default:
        helper.add("visibility", v.getVisibility());
    }

    helper
        .add("width", v.getWidth())
        .add("height", v.getHeight())
        .add("has-focus", v.hasFocus())
        .add("has-focusable", v.hasFocusable())
        .add("has-window-focus", v.hasWindowFocus())
        .add("is-clickable", v.isClickable())
        .add("is-enabled", v.isEnabled())
        .add("is-focused", v.isFocused())
        .add("is-focusable", v.isFocusable())
        .add("is-layout-requested", v.isLayoutRequested())
        .add("is-selected", v.isSelected())
        .add("layout-params", v.getLayoutParams())
        .add("tag", v.getTag());

    if (null != v.getRootView()) {
      // pretty much only true in unit-tests.
      helper.add("root-is-layout-requested", v.getRootView().isLayoutRequested());
    }

    EditorInfo ei = new EditorInfo();
    InputConnection ic = v.onCreateInputConnection(ei);
    boolean hasInputConnection = ic != null;
    helper.add("has-input-connection", hasInputConnection);
    if (hasInputConnection) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      Printer p = new StringBuilderPrinter(sb);
      ei.dump(p, "");
      sb.append("]");
      helper.add("editor-info", sb.toString().replace("\n", " "));
    }

    if (Build.VERSION.SDK_INT > 10) {
      helper.add("x", v.getX()).add("y", v.getY());
    }

    if (v instanceof TextView) {
      innerDescribe((TextView) v, helper);
    }
    if (v instanceof Checkable) {
      innerDescribe((Checkable) v, helper);
    }
    if (v instanceof ViewGroup) {
      innerDescribe((ViewGroup) v, helper);
    }
    return helper.toString();
  }

  private static void innerDescribe(TextView textBox, ToStringHelper helper) {
    if (null != textBox.getText()) {
      helper.add("text", textBox.getText());
    }

    if (null != textBox.getError()) {
      helper.add("error-text", textBox.getError());
    }

    if (null != textBox.getHint()) {
      helper.add("hint", textBox.getHint());
    }

    helper.add("input-type", textBox.getInputType());
    helper.add("ime-target", textBox.isInputMethodTarget());
    helper.add("has-links", textBox.getUrls().length > 0);
  }

  private static void innerDescribe(Checkable checkable, ToStringHelper helper) {
    helper.add("is-checked", checkable.isChecked());
  }

  private static void innerDescribe(ViewGroup viewGroup, ToStringHelper helper) {
    helper.add("child-count", viewGroup.getChildCount());
  }
  /**
   * IDs generated by {@link View#generateViewId} will fail if used as a resource ID in attempted
   * resources lookups. This now logs an error in API 28, causing test failures. This method is
   * taken from {@link View#isViewIdGenerated} to prevent resource lookup to check if a view id was
   * generated.
   */
  private static boolean isViewIdGenerated(int id) {
    return (id & 0xFF000000) == 0 && (id & 0x00FFFFFF) != 0;
  }
}
