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

import static androidx.test.espresso.matcher.ViewMatchers.hasLinks;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static kotlin.collections.CollectionsKt.mutableListOf;
import static org.hamcrest.Matchers.allOf;

import android.net.Uri;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matcher;

/**
 * Invokes onClick of a link within a TextView (made with Linkify or via another method). Why not
 * issue a real click event to the screen? Unfortunately, it does not seem to be possible (at least
 * using public APIs) to determine the location of the link on the screen.
 */
public final class OpenLinkAction implements ViewAction {
  private final Matcher<String> linkTextMatcher;
  private final Matcher<Uri> uriMatcher;

  public OpenLinkAction(Matcher<String> linkTextMatcher, Matcher<Uri> uriMatcher) {
    this.linkTextMatcher = checkNotNull(linkTextMatcher);
    this.uriMatcher = checkNotNull(uriMatcher);
  }

  @Override
  public Matcher<View> getConstraints() {
    return allOf(isDisplayed(), isAssignableFrom(TextView.class), hasLinks());
  }

  @Override
  public String getDescription() {
    return String.format(
        Locale.ROOT, "open link with text %s and uri %s", linkTextMatcher, uriMatcher);
  }

  @Override
  public void perform(UiController uiController, View view) {
    TextView textView = (TextView) view;
    String allText = textView.getText().toString();
    URLSpan[] urls = textView.getUrls();
    Spanned spanned = (Spanned) textView.getText();

    // TODO: what if we get more than one hit? For now, take the first one...
    // In the future, we may want to support a way to disambiguate (e.g using text around the link).
    List<String> allLinks = mutableListOf();
    for (URLSpan url : urls) {
      int start = spanned.getSpanStart(url);
      checkState(start != -1, "Unable to get start of text associated with url: " + url);
      int end = spanned.getSpanEnd(url);
      checkState(end != -1, "Unable to get end of text associated with url: " + url);
      String linkText = allText.substring(start, end);
      allLinks.add(linkText);
      if (linkTextMatcher.matches(linkText) && uriMatcher.matches(Uri.parse(url.getURL()))) {
        url.onClick(view);
        return;
      }
    }
    throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(
            new RuntimeException(
                String.format(
                    Locale.ROOT,
                    "Link with text '%s' and uri '%s' not found. List of links found in this view:"
                        + " %s\n"
                        + "List of uris: %s",
                    linkTextMatcher,
                    uriMatcher,
                    allLinks,
                    Arrays.asList(urls))))
        .build();
  }
}
