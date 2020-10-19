package androidx.test.espresso.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/** Utilities used for matcher tests. */
/* package */ final class MatcherTestUtils {

  // Not instantiable.
  private MatcherTestUtils() {}

  /**
   * Shorthand function to calling {@link Matcher#describeTo(Description)} and getting the String
   * representation of that description.
   *
   * @param matcher The matcher which we want the description for.
   * @return The string set by {@link Matcher#describeTo(Description)}.
   */
  public static String getDescription(Matcher<?> matcher) {
    Description description = new StringDescription();
    matcher.describeTo(description);
    return description.toString();
  }

  /**
   * Shorthand function to calling {@link Matcher#describeMismatch(Object, Description)} and getting
   * the String representation of that description.
   *
   * @param matcher The matcher which we want the mismatch description for.
   * @param actual The actual value that triggered the mismatch.
   * @return The string set by {@link Matcher#describeMismatch(Object, Description)}.
   */
  public static String getMismatchDescription(Matcher<?> matcher, Object actual) {
    Description description = new StringDescription();
    matcher.describeMismatch(actual, description);
    return description.toString();
  }
}
