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

package androidx.test.espresso.matcher;

import static org.hamcrest.Matchers.is;

import android.content.res.Resources;
import android.preference.Preference;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matchers that match {@link Preference}s. */
public final class PreferenceMatchers {

  private PreferenceMatchers() {}

  public static Matcher<Preference> withSummary(final int resourceId) {
    return new TypeSafeMatcher<Preference>() {
      private String resourceName = null;
      private String expectedText = null;

      @Override
      public void describeTo(Description description) {
        description.appendText(" with summary string from resource id: ");
        description.appendValue(resourceId);
        if (null != resourceName) {
          description.appendText("[");
          description.appendText(resourceName);
          description.appendText("]");
        }
        if (null != expectedText) {
          description.appendText(" value: ");
          description.appendText(expectedText);
        }
      }

      @Override
      public boolean matchesSafely(Preference preference) {
        if (null == expectedText) {
          try {
            expectedText = preference.getContext().getResources().getString(resourceId);
            resourceName = preference.getContext().getResources().getResourceEntryName(resourceId);
          } catch (Resources.NotFoundException ignored) {
            /* view could be from a context unaware of the resource id. */
          }
        }
        if (null != expectedText) {
          return expectedText.equals(preference.getSummary().toString());
        } else {
          return false;
        }
      }
    };
  }

  public static Matcher<Preference> withSummaryText(String summary) {
    return withSummaryText(is(summary));
  }

  public static Matcher<Preference> withSummaryText(final Matcher<String> summaryMatcher) {
    return new TypeSafeMatcher<Preference>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(" a preference with summary matching: ");
        summaryMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(Preference pref) {
        String summary = pref.getSummary().toString();
        return summaryMatcher.matches(summary);
      }
    };
  }

  public static Matcher<Preference> withTitle(final int resourceId) {
    return new TypeSafeMatcher<Preference>() {
      private String resourceName = null;
      private String expectedText = null;

      @Override
      public void describeTo(Description description) {
        description.appendText(" with title string from resource id: ");
        description.appendValue(resourceId);
        if (null != resourceName) {
          description.appendText("[");
          description.appendText(resourceName);
          description.appendText("]");
        }
        if (null != expectedText) {
          description.appendText(" value: ");
          description.appendText(expectedText);
        }
      }

      @Override
      public boolean matchesSafely(Preference preference) {
        if (null == expectedText) {
          try {
            expectedText = preference.getContext().getResources().getString(resourceId);
            resourceName = preference.getContext().getResources().getResourceEntryName(resourceId);
          } catch (Resources.NotFoundException ignored) {
            /* view could be from a context unaware of the resource id. */
          }
        }
        if (null != expectedText && preference.getTitle() != null) {
          return expectedText.equals(preference.getTitle().toString());
        } else {
          return false;
        }
      }
    };
  }

  public static Matcher<Preference> withTitleText(String title) {
    return withTitleText(is(title));
  }

  public static Matcher<Preference> withTitleText(final Matcher<String> titleMatcher) {
    return new TypeSafeMatcher<Preference>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(" a preference with title matching: ");
        titleMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(Preference pref) {
        if (pref.getTitle() == null) {
          return false;
        }
        String title = pref.getTitle().toString();
        return titleMatcher.matches(title);
      }
    };
  }

  public static Matcher<Preference> isEnabled() {
    return new TypeSafeMatcher<Preference>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(" is an enabled preference");
      }

      @Override
      public boolean matchesSafely(Preference pref) {
        return pref.isEnabled();
      }
    };
  }

  public static Matcher<Preference> withKey(String key) {
    return withKey(is(key));
  }

  public static Matcher<Preference> withKey(final Matcher<String> keyMatcher) {
    return new TypeSafeMatcher<Preference>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(" preference with key matching: ");
        keyMatcher.describeTo(description);
      }

      @Override
      public boolean matchesSafely(Preference pref) {
        return keyMatcher.matches(pref.getKey());
      }
    };
  }
}
