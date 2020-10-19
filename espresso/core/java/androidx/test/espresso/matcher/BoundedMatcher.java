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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Some matcher sugar that lets you create a matcher for a given type but only process items of a
 * specific subtype of that matcher.
 *
 * @param <T> The desired type of the Matcher.
 * @param <S> the subtype of T that your matcher applies safely to.
 * @deprecated Use {@link BoundedDiagnosingMatcher} instead for better diagnostic messages.
 */
@Deprecated
public abstract class BoundedMatcher<T, S extends T> extends BaseMatcher<T> {

  private final Class<?> expectedType;
  private final Class<?>[] interfaceTypes;

  public BoundedMatcher(Class<? extends S> expectedType) {
    this.expectedType = checkNotNull(expectedType);
    this.interfaceTypes = new Class<?>[0];
  }

  public BoundedMatcher(
      Class<?> expectedType, Class<?> interfaceType1, Class<?>... otherInterfaces) {
    this.expectedType = checkNotNull(expectedType);
    checkNotNull(otherInterfaces);
    int interfaceCount = otherInterfaces.length + 1;
    this.interfaceTypes = new Class<?>[interfaceCount];

    interfaceTypes[0] = checkNotNull(interfaceType1);
    checkArgument(interfaceType1.isInterface());
    int interfaceTypeIdx = 1;
    for (Class<?> intfType : otherInterfaces) {
      interfaceTypes[interfaceTypeIdx] = checkNotNull(intfType);
      checkArgument(intfType.isInterface());
      interfaceTypeIdx++;
    }
  }

  protected abstract boolean matchesSafely(S item);

  @Override
  @SuppressWarnings({"unchecked"})
  public final boolean matches(Object item) {
    if (item == null) {
      return false;
    }

    if (expectedType.isInstance(item)) {
      for (Class<?> intfType : interfaceTypes) {
        if (!intfType.isInstance(item)) {
          return false;
        }
      }
      return matchesSafely((S) item);
    }
    return false;
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public void describeMismatch(Object item, Description description) {
    if (item == null) {
      description.appendText("item was null");
      return;
    }
    if (!expectedType.isInstance(item)) {
      description.appendText("item does not extend ").appendText(expectedType.getName());
      return;
    }
    for (Class<?> intfType : interfaceTypes) {
      if (!intfType.isInstance(item)) {
        description.appendText("item does not implement ").appendText(intfType.getName());
        return;
      }
    }
  }
}
