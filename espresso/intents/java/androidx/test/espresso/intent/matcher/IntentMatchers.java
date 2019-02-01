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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasKey;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasMyPackageName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.espresso.intent.ResolvedIntent;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matchers for matching {@link Intent} objects. */
public final class IntentMatchers {

  private IntentMatchers() {}

  public static Matcher<Intent> anyIntent() {
    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("any intent");
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return true;
      }
    };
  }

  public static Matcher<Intent> hasAction(String action) {
    return hasAction(is(action));
  }

  public static Matcher<Intent> hasAction(final Matcher<String> actionMatcher) {
    checkNotNull(actionMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has action: ");
        description.appendDescriptionOf(actionMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return actionMatcher.matches(intent.getAction());
      }
    };
  }

  public static Matcher<Intent> hasCategories(Set<String> categories) {
    return hasCategories(equalTo((Iterable<String>) categories));
  }

  public static Matcher<Intent> hasCategories(
      final Matcher<? extends Iterable<? super String>> categoriesMatcher) {
    checkNotNull(categoriesMatcher);
    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has categories: ");
        description.appendDescriptionOf(categoriesMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return categoriesMatcher.matches(intent.getCategories());
      }
    };
  }

  /**
   * Returns a matcher that will only match intents targeted to a single class by using {@link
   * ComponentNameMatchers#hasClassName}. The input string must contain the package name + short
   * class name. For example hasComponent("com.google.provider.NotePad").
   *
   * @param className complete class path
   */
  public static Matcher<Intent> hasComponent(String className) {
    return hasComponent(hasClassName(className));
  }

  /**
   * Returns a matcher that will only match intents targeted to the componentName's class, {@see
   * ComponentName#getClassName}. For example: Intent intent = new Intent() .setComponentName(new
   * ComponentName("com.google.provider", "com.google.provider.Notepad")); will match all intents
   * targeted to Notepad.java.
   *
   * @param componentName a componentName that has the target class specified
   */
  public static Matcher<Intent> hasComponent(ComponentName componentName) {
    return hasComponent(hasClassName(componentName.getClassName()));
  }

  /**
   * Can match an intent by class name, package name or short class name.
   *
   * @param componentMatcher can be the value of {@link ComponentNameMatchers#hasClassName}, {@link
   *     ComponentNameMatchers#hasPackageName} or {@link ComponentNameMatchers#hasShortClassName}
   */
  public static Matcher<Intent> hasComponent(final Matcher<ComponentName> componentMatcher) {
    checkNotNull(componentMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has component: ");
        description.appendDescriptionOf(componentMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return componentMatcher.matches(intent.getComponent());
      }
    };
  }

  public static Matcher<Intent> hasData(String uri) {
    return hasData(is(Uri.parse(uri)));
  }

  public static Matcher<Intent> hasData(Uri uri) {
    return hasData(is(uri));
  }

  public static Matcher<Intent> hasData(final Matcher<Uri> uriMatcher) {
    checkNotNull(uriMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has data: ");
        description.appendDescriptionOf(uriMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return uriMatcher.matches(intent.getData());
      }
    };
  }

  public static Matcher<Intent> hasDataString(final Matcher<String> stringMatcher) {
    checkNotNull(stringMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has data string: ").appendDescriptionOf(stringMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return stringMatcher.matches(intent.getDataString());
      }
    };
  }

  public static Matcher<Intent> hasExtraWithKey(String key) {
    return hasExtraWithKey(is(key));
  }

  public static Matcher<Intent> hasExtraWithKey(Matcher<String> keyMatcher) {
    return hasExtras(hasKey(keyMatcher));
  }

  public static <T> Matcher<Intent> hasExtra(String key, T value) {
    return hasExtras(hasEntry(key, value));
  }

  public static Matcher<Intent> hasExtra(Matcher<String> keyMatcher, Matcher<?> valueMatcher) {
    return hasExtras(hasEntry(keyMatcher, valueMatcher));
  }

  public static Matcher<Intent> hasExtras(final Matcher<Bundle> bundleMatcher) {
    checkNotNull(bundleMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has extras: ");
        description.appendDescriptionOf(bundleMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return bundleMatcher.matches(intent.getExtras());
      }
    };
  }

  public static Matcher<Intent> hasType(String type) {
    return hasType(is(type));
  }

  public static Matcher<Intent> hasType(final Matcher<String> typeMatcher) {
    checkNotNull(typeMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has type: ");
        description.appendDescriptionOf(typeMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return typeMatcher.matches(intent.getType());
      }
    };
  }

  public static Matcher<Intent> hasPackage(final Matcher<String> packageMatcher) {
    checkNotNull(packageMatcher);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has pkg: ");
        description.appendDescriptionOf(packageMatcher);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return packageMatcher.matches(intent.getPackage());
      }
    };
  }

  public static Matcher<Intent> hasPackage(final String packageName) {
    checkNotNull(packageName);
    return hasPackage(equalTo(packageName));
  }

  /**
   * Matches an intent based on the package of activity which can handle the intent.
   *
   * @param packageName packages of activity that can handle the intent
   */
  public static Matcher<Intent> toPackage(final String packageName) {
    checkNotNull(packageName);

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("resolvesTo: " + packageName);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        // Ideally, this would be a Matcher<ResolvedIntent> and we would not need this runtime
        // check. However, this matcher would then not work in combination with other matchers of
        // type Matcher<Intent> (as in the case with allOf(toPackage(...), hasType(...)).
        if (!(intent instanceof ResolvedIntent)) {
          throw new RuntimeException(
              String.format(
                  "toPackage.matches was given an intent that is not of type %s. This"
                      + " should not happen as this method is only invoked internally by Intents.",
                  ResolvedIntent.class.getSimpleName()));
        }
        return ((ResolvedIntent) intent).canBeHandledBy(packageName);
      }
    };
  }

  public static Matcher<Intent> hasFlag(int flag) {
    return hasFlags(flag);
  }

  public static Matcher<Intent> hasFlags(int... flags) {
    int allFlags = 0;
    for (int i : flags) {
      allFlags |= i;
    }
    return hasFlags(allFlags);
  }

  public static Matcher<Intent> hasFlags(final int flags) {

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("flags: " + Integer.toHexString(flags));
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        int intentFlags = intent.getFlags();
        return ((intentFlags & flags) == flags);
      }
    };
  }

  /** Matches an intent if it {@link Intent#filterEquals(Intent)} the expected intent. */
  public static Matcher<Intent> filterEquals(Intent expectedIntent) {
    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("filterEquals: ").appendValue(expectedIntent);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return expectedIntent.filterEquals(intent);
      }
    };
  }

  /**
   * Matches an intent if its package is the same as the target package for the instrumentation
   * test.
   */
  public static Matcher<Intent> isInternal() {
    final Context targetContext = getApplicationContext();

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("target package: " + targetContext.getPackageName());
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        ComponentName component = intent.resolveActivity(targetContext.getPackageManager());
        if (component != null) {
          return hasMyPackageName().matches(component);
        }
        return false;
      }
    };
  }
}
