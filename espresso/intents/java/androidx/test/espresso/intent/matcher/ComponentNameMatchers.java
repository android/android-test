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
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;

import android.content.ComponentName;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** A collection of hamcrest matchers to match {@link ComponentName} objects. */
public final class ComponentNameMatchers {
  private ComponentNameMatchers() {}

  private static class ComponentMatcher extends TypeSafeMatcher<ComponentName> {
    private Matcher<String> classNameMatcher;
    private Matcher<String> packageNameMatcher;
    private Matcher<String> shortClassNameMatcher;

    private ComponentMatcher(
        Matcher<String> classNameMatcher,
        Matcher<String> packageNameMatcher,
        Matcher<String> shortClassNameMatcher) {
      super(ComponentName.class);
      this.classNameMatcher = checkNotNull(classNameMatcher);
      this.packageNameMatcher = checkNotNull(packageNameMatcher);
      this.shortClassNameMatcher = checkNotNull(shortClassNameMatcher);
    }

    @Override
    public boolean matchesSafely(ComponentName componentName) {
      return classNameMatcher.matches(componentName.getClassName())
          && packageNameMatcher.matches(componentName.getPackageName())
          && shortClassNameMatcher.matches(componentName.getShortClassName());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has component with: class name: ");
      description.appendDescriptionOf(classNameMatcher);
      description.appendText(" package name: ");
      description.appendDescriptionOf(packageNameMatcher);
      description.appendText(" short class name: ");
      description.appendDescriptionOf(shortClassNameMatcher);
    }
  }

  public static Matcher<ComponentName> hasClassName(String className) {
    return hasClassName(is(className));
  }

  public static Matcher<ComponentName> hasClassName(Matcher<String> classNameMatcher) {
    return new ComponentMatcher(classNameMatcher, any(String.class), any(String.class));
  }

  public static Matcher<ComponentName> hasPackageName(String packageName) {
    return hasPackageName(is(packageName));
  }

  public static Matcher<ComponentName> hasPackageName(Matcher<String> packageNameMatcher) {
    return new ComponentMatcher(any(String.class), packageNameMatcher, any(String.class));
  }

  public static Matcher<ComponentName> hasShortClassName(String shortClassName) {
    return hasShortClassName(is(shortClassName));
  }

  public static Matcher<ComponentName> hasShortClassName(Matcher<String> shortClassNameMatcher) {
    return new ComponentMatcher(any(String.class), any(String.class), shortClassNameMatcher);
  }

  /**
   * Matches a component based on the target package name found through the Instrumentation Registry
   * for the test.
   */
  public static Matcher<ComponentName> hasMyPackageName() {
    return hasPackageName(is(getApplicationContext().getPackageName()));
  }
}
