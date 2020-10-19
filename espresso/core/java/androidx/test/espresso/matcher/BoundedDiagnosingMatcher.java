package androidx.test.espresso.matcher;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;

/**
 * A matcher that allows for a quick creation of a matcher that applies to a given type but only
 * processes items of a specific subtype of that matcher. Additional interfaces can be applied. This
 * class is syntactic sugar for {@link Matchers#instanceOf(Class)} where the first argument is the
 * base class and the remaining optional arguments are interfaces.
 *
 * @param <S> The desired type of the Matcher (T or a subclass of T).
 * @param <T> The actual type that the matcher applies safely to.
 */
public abstract class BoundedDiagnosingMatcher<S, T extends S> extends BaseMatcher<S> {

  private final Matcher<Class<?>> matcher;

  public BoundedDiagnosingMatcher(Class<? extends S> expectedType) {
    matcher = Matchers.instanceOf(checkNotNull(expectedType));
  }

  public BoundedDiagnosingMatcher(
      Class<? extends S> expectedType, Class<?> interfaceType1, Class<?>... otherInterfaces) {
    int interfaceCount = otherInterfaces.length + 2;
    List<Matcher<? super Class<?>>> instanceMatchers = new ArrayList<>(interfaceCount);
    instanceMatchers.add(Matchers.instanceOf(checkNotNull(expectedType)));
    checkNotNull(otherInterfaces);

    instanceMatchers.add(Matchers.instanceOf(checkNotNull(interfaceType1)));
    checkArgument(interfaceType1.isInterface());
    for (Class<?> intfType : otherInterfaces) {
      instanceMatchers.add(Matchers.instanceOf(checkNotNull(intfType)));
      checkArgument(intfType.isInterface());
    }

    matcher = Matchers.allOf(instanceMatchers);
  }

  /**
   * Subclasses should implement this. The item will already have been checked for the specific
   * type, interfaces, and will never be null.
   *
   * @param item The pre-checked item.
   * @param mismatchDescription A {@link Description} to write to for mismatches.
   * @return {@code true} if the item matches the expectations for this {@link Matcher}.
   */
  protected abstract boolean matchesSafely(T item, Description mismatchDescription);

  /**
   * Subclasses should implement this. The fine details of the matcher should be added to the
   * description. Type checking information will have already been added.
   *
   * @param description The {@link Description} object to write to.
   */
  protected abstract void describeMoreTo(Description description);

  @Override
  public final void describeTo(Description description) {
    matcher.describeTo(description);
    Description implDescription = new StringDescription();
    describeMoreTo(implDescription);
    String implDescriptionString = implDescription.toString();
    if (!implDescriptionString.isEmpty()) {
      description.appendText(" and ").appendText(implDescriptionString);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(Object item) {
    return item != null && matcher.matches(item) && matchesSafely((T) item, Description.NONE);
  }

  /**
   * This method provides a default implementation for {@code null} check as well as a super type
   * and interface checks provided by the constructor. Failing either check provides a default
   * mismatch description. Passing both will call into {@link #matchesSafely(Object, Description)}
   * which will allow the sub-class to check for a mismatch and describe what went wrong (if
   * anything at all).
   *
   * @param item The item which is assumed to have mismatched and should be described.
   * @param mismatchDescription The description builder for the mismatch.
   * @see org.hamcrest.TypeSafeDiagnosingMatcher for similar implementation pattern.
   */
  @Override
  @SuppressWarnings("unchecked")
  public final void describeMismatch(Object item, Description mismatchDescription) {
    if (item == null) {
      mismatchDescription.appendText("was null");
    } else if (!matcher.matches(item)) {
      matcher.describeMismatch(item, mismatchDescription);
    } else {
      matchesSafely((T) item, mismatchDescription);
    }
  }
}
