package androidx.test.espresso;

import org.hamcrest.core.SubstringMatcher;

/** Simulates the MatchesPattern available in Hamcrest 2. */
class StringPatternMatcher extends SubstringMatcher {

  public StringPatternMatcher(String substringRegexPattern) {
    super(substringRegexPattern);
  }

  @Override
  protected boolean evalSubstringOf(String s) {
    return s.matches(substring);
  }

  @Override
  protected String relationship() {
    return "matching";
  }
}
