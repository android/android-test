/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.intent.matcher;

import static androidx.test.espresso.intent.Checks.checkNotNull;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.errorprone.annotations.CheckReturnValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matchers to match {@link Bundle} objects. */
@CheckReturnValue
public final class BundleMatchers {
  private BundleMatchers() {}

  private static class BundleMatcher extends TypeSafeMatcher<Bundle> {
    private final Matcher<String> keyMatcher;
    private final Matcher<?> valueMatcher;
    /**
     * Whether the matcher should check for presence or absence of the key and value.
     *
     * <p>If true, the matcher will match bundles that have a matching key-value pair. If false, the
     * matcher will match bundles that do not have a matching key-value pair (including empty
     * bundles).
     */
    private final boolean shouldBePresent;

    BundleMatcher(Matcher<String> keyMatcher, Matcher<?> valueMatcher, boolean shouldBePresent) {
      super(Bundle.class);
      this.keyMatcher = checkNotNull(keyMatcher);
      this.valueMatcher = checkNotNull(valueMatcher);
      this.shouldBePresent = shouldBePresent;
    }

    @Override
    public boolean matchesSafely(Bundle bundle) {
      for (String key : bundle.keySet()) {
        if (keyMatcher.matches(key) && valueMatcher.matches(bundle.get(key))) {
          return shouldBePresent;
        }
      }
      return !shouldBePresent;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has bundle ");
      description.appendText(shouldBePresent ? "with" : "without");
      description.appendText(": key: ");
      description.appendDescriptionOf(keyMatcher);
      description.appendText(" value: ");
      description.appendDescriptionOf(valueMatcher);
    }
  }

  private static final class EmptyBundleMatcher extends TypeSafeMatcher<Bundle> {

    @Override
    protected boolean matchesSafely(Bundle bundle) {
      return bundle.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is empty bundle");
    }
  }

  public static Matcher<Bundle> isEmpty() {
    return new EmptyBundleMatcher();
  }

  public static Matcher<Bundle> isEmptyOrNull() {
    return anyOf(nullValue(), isEmpty());
  }

  public static <T> Matcher<Bundle> hasEntry(String key, T value) {
    return hasEntry(is(key), is(value));
  }

  public static Matcher<Bundle> hasEntry(String key, Matcher<?> valueMatcher) {
    return hasEntry(is(key), valueMatcher);
  }

  public static Matcher<Bundle> hasEntry(Matcher<String> keyMatcher, Matcher<?> valueMatcher) {
    return new BundleMatcher(keyMatcher, valueMatcher, /* shouldBePresent= */ true);
  }

  public static Matcher<Bundle> hasKey(String key) {
    return hasKey(is(key));
  }

  public static Matcher<Bundle> hasKey(Matcher<String> keyMatcher) {
    return new BundleMatcher(keyMatcher, anything(), /* shouldBePresent= */ true);
  }

  @NonNull
  public static Matcher<Bundle> doesNotHaveKey(String key) {
    return doesNotHaveKey(is(key));
  }

  @NonNull
  public static Matcher<Bundle> doesNotHaveKey(Matcher<String> keyMatcher) {
    return new BundleMatcher(keyMatcher, anything(), /* shouldBePresent= */ false);
  }

  public static <T> Matcher<Bundle> hasValue(T value) {
    return hasValue(is(value));
  }

  public static Matcher<Bundle> hasValue(Matcher<?> valueMatcher) {
    return new BundleMatcher(any(String.class), valueMatcher, /* shouldBePresent= */ true);
  }
}
