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

package androidx.test.ui.app;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import androidx.test.espresso.matcher.BoundedMatcher;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Static utility methods to create <a
 * href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
 * </code></a> instances that can be applied to the data objects created by {@link
 * androidx.test.ui.app.LongListActivity}.
 *
 * <p>These matchers are used by the {@link androidx.test.espresso.Espresso#onData(Matcher)}
 * API and are applied against the data exposed by @{link android.widget.ListView#getAdapter()}.
 *
 * <p>In LongListActivity's case - each row is a Map containing 2 key value pairs. The key "STR" is
 * mapped to a String which will be rendered into a TextView with the R.id.item_content. The other
 * key "LEN" is an Integer which is the length of the string "STR" refers to. This length is
 * rendered into a TextView with the id R.id.item_size.
 */
public final class LongListMatchers {

  private LongListMatchers() { }


  /**
   * Creates a matcher against the text stored in R.id.item_content. This text is roughly
   * "item: $row_number".
   */
  public static Matcher<Object> withItemContent(String expectedText) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(expectedText);
    return withItemContent(equalTo(expectedText));
  }

  /**
   * Creates a matcher against the text stored in R.id.item_content. This text is roughly
   * "item: $row_number".
   */
  @SuppressWarnings("rawtypes")
  public static Matcher<Object> withItemContent(final Matcher<String> itemTextMatcher) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(itemTextMatcher);
    return new BoundedMatcher<Object, Map>(Map.class) {
      @Override
      public boolean matchesSafely(Map map) {
        return hasEntry(equalTo("STR"), itemTextMatcher).matches(map);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with item content: ");
        itemTextMatcher.describeTo(description);
      }
    };
  }

  /**
   * Creates a matcher against the text stored in R.id.item_size. This text is the size of the text
   * printed in R.id.item_content.
   */
  public static Matcher<Object> withItemSize(int itemSize) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkArgument(itemSize > -1);
    return withItemSize(equalTo(itemSize));
  }

  /**
   * Creates a matcher against the text stored in R.id.item_size. This text is the size of the text
   * printed in R.id.item_content.
   */
  @SuppressWarnings("rawtypes")
  public static Matcher<Object> withItemSize(final Matcher<Integer> itemSizeMatcher) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(itemSizeMatcher);
    return new BoundedMatcher<Object, Map>(Map.class) {
      @Override
      public boolean matchesSafely(Map map) {
        return hasEntry(equalTo("LEN"), itemSizeMatcher).matches(map);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with item size: ");
        itemSizeMatcher.describeTo(description);
      }
    };
  }

  /**
   * Creates a matcher against the footer of this list view.
   */
  @SuppressWarnings("unchecked")
  public static Matcher<? extends Object> isFooter() {
    // This depends on LongListActivity.FOOTER being passed as data in the addFooterView method.
    return is(LongListActivity.FOOTER);
  }

}
