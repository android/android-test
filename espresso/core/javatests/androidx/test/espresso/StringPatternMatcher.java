package androidx.test.espresso;

import org.hamcrest.core.SubstringMatcher;

/** Simulates the MatchesPattern available in Hamcrest 2. */
class StringPatternMatcher extends SubstringMatcher {

  public StringPatternMatcher(String substringRegexPattern) {
    super(/* relationship= */ "matching", /* ignoringCase= */ false, substringRegexPattern);
  }

  @Override
  protected boolean evalSubstringOf(String s) {
    return s.matches(substring);
  }
}
