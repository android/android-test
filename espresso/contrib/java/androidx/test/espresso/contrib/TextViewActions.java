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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.Checks.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import androidx.annotation.StringRes;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import java.util.Locale;
import javax.annotation.Nullable;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/** {@link ViewAction}s for clicking on {@link ClickableSpan}s inside {@link TextView}s. */
public final class TextViewActions {
  private TextViewActions() {}

  public static ViewAction clickSpannedText(@StringRes int spannedTextResourceId) {
    return new ClickSpannedTextAction(spannedTextResourceId, null);
  }

  public static ViewAction clickSpannedText(String spannedText) {
    return new ClickSpannedTextAction(null, spannedText);
  }

  public static <T extends ClickableSpan> ViewAction clickSpannedText(
      Matcher<T> spanMatcher, Class<T> spanClass) {
    return new ClickMatchedSpannedAction<T>(spanMatcher, spanClass);
  }

  private static final String SPAN_NOT_FOUND_MESSAGE =
      "Clicking on the span was attempted, but a matching span was not found";

  private static final class ClickSpannedTextAction implements ViewAction {

    @Nullable private final Integer spannedTextResourceId;

    @Nullable private String spannedText;

    private ClickSpannedTextAction(
        @Nullable Integer spannedTextResourceId, @Nullable String spannedText) {
      checkArgument(
          (spannedTextResourceId == null) != (spannedText == null),
          "either spannedTextResourceId or spannedText must be specified, but not both");
      this.spannedTextResourceId = spannedTextResourceId;
      this.spannedText = spannedText;
    }

    @Override
    public Matcher<View> getConstraints() {
      return allOf(click().getConstraints(), instanceOf(TextView.class));
    }

    @Override
    public String getDescription() {
      if (spannedTextResourceId == null) {
        return String.format(Locale.ROOT, "click span with text '%s'", spannedText);
      } else if (spannedText != null) {
        return String.format(
            Locale.ROOT,
            "click span with text res-ID '%s' and text '%s'",
            spannedTextResourceId,
            spannedText);
      } else {
        return String.format(
            Locale.ROOT, "click span with text res-ID '%s'", spannedTextResourceId);
      }
    }

    @Override
    public void perform(UiController uiController, View view) {
      if (spannedTextResourceId != null) {
        if (!isViewIdGenerated(spannedTextResourceId)) {
          spannedText = view.getContext().getString(spannedTextResourceId);
        } else {
          throw new PerformException.Builder()
              .withActionDescription(getDescription())
              .withViewDescription(HumanReadables.describe(view))
              .build();
        }
      }
      if (view instanceof TextView) {
        TextView textView = (TextView) view;
        SpannableString fullSpannable = SpannableString.valueOf(textView.getText());
        ClickableSpan[] spans =
            fullSpannable.getSpans(0, fullSpannable.length(), ClickableSpan.class);
        for (ClickableSpan span : spans) {
          int spanStart = fullSpannable.getSpanStart(span);
          int spanEnd = fullSpannable.getSpanEnd(span);
          String spanText = fullSpannable.subSequence(spanStart, spanEnd).toString();
          if (spanText.equals(spannedText)) {
            span.onClick(view);
            return;
          }
        }
        throw new PerformException.Builder()
            .withActionDescription(getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new RuntimeException(SPAN_NOT_FOUND_MESSAGE))
            .build();
      }
    }
  }

  private static final class ClickMatchedSpannedAction<T extends ClickableSpan>
      implements ViewAction {
    private final Matcher<T> spanMatcher;
    private final Class<T> spanClass;

    private ClickMatchedSpannedAction(Matcher<T> spanMatcher, Class<T> spanClass) {
      this.spanMatcher = spanMatcher;
      this.spanClass = spanClass;
    }

    @Override
    public Matcher<View> getConstraints() {
      return allOf(click().getConstraints(), instanceOf(TextView.class));
    }

    @Override
    public String getDescription() {
      return new StringDescription()
          .appendText("click span with clickable span matcher ")
          .appendDescriptionOf(spanMatcher)
          .toString();
    }

    @Override
    public void perform(UiController uiController, View view) {
      if (view instanceof TextView) {
        TextView textView = (TextView) view;
        SpannableString fullSpannable = SpannableString.valueOf(textView.getText());
        T[] spans = fullSpannable.getSpans(0, fullSpannable.length(), spanClass);
        for (T span : spans) {
          if (spanMatcher.matches(span)) {
            span.onClick(view);
            return;
          }
        }
        throw new PerformException.Builder()
            .withActionDescription(getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new RuntimeException(SPAN_NOT_FOUND_MESSAGE))
            .build();
      }
    }
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
