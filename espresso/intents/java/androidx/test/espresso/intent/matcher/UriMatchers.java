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

import static androidx.test.espresso.intent.Checks.checkArgument;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A collection of matchers for {@link Uri}s, which can match Uris on their properties (host, path,
 * ...).
 */
public final class UriMatchers {

  /** Container class for paramName and pramValues. */
  static class QueryParamEntry {

    String paramName;
    Iterable<String> paramVals;

    public QueryParamEntry(String paramName, Iterable<String> paramVals) {
      this.paramName = paramName;
      this.paramVals = paramVals;
    }
  }

  /** Container class for schemeName and schemeValues. */
  static class SchemeParamValue {

    String schemeName;
    String schemeVals;

    public SchemeParamValue(String schemeName, String schemeVals) {
      this.schemeName = schemeName;
      this.schemeVals = schemeVals;
    }
  }

  /*
   * Private constructor to make sure no one instantiate this class.
   */
  private UriMatchers() {}

  /**
   * Returns a set of the unique names of all query parameters. Iterating over the set will return
   * the names in order of their first occurrence.
   *
   * <p>This method was added to Uri class in android api 11. So, for working with API level less
   * then that we had to re-implement it.
   *
   * @throws UnsupportedOperationException if this isn't a hierarchical URI
   * @return a set of decoded names
   */
  // VisibleForTesting
  static Set<String> getQueryParameterNames(Uri uri) {
    checkArgument(!uri.isOpaque(), "This isn't a hierarchical URI.");
    String query = uri.getEncodedQuery();
    if (query == null) {
      return Collections.emptySet();
    }

    Set<String> names = new LinkedHashSet<String>();
    int start = 0;
    do {
      int next = query.indexOf('&', start);
      int end = (next == -1) ? query.length() : next;

      int separator = query.indexOf('=', start);
      if (separator > end || separator == -1) {
        separator = end;
      }

      String name = query.substring(start, separator);
      names.add(Uri.decode(name));

      // Move start to end of name.
      start = end + 1;
    } while (start < query.length());

    return Collections.unmodifiableSet(names);
  }

  public static Matcher<Uri> hasHost(String host) {
    return hasHost(is(host));
  }

  public static Matcher<Uri> hasHost(final Matcher<String> hostMatcher) {
    checkNotNull(hostMatcher);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        return hostMatcher.matches(uri.getHost());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has host: ");
        description.appendDescriptionOf(hostMatcher);
      }
    };
  }

  public static Matcher<Uri> hasParamWithName(String paramName) {
    return hasParamWithName(is(paramName));
  }

  public static Matcher<Uri> hasParamWithName(final Matcher<String> paramName) {
    checkNotNull(paramName);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        return hasItem(paramName).matches(getQueryParameterNames(uri));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has param with name: ");
        description.appendDescriptionOf(paramName);
      }
    };
  }

  public static Matcher<Uri> hasPath(String pathName) {
    return hasPath(is(pathName));
  }

  public static Matcher<Uri> hasPath(final Matcher<String> pathName) {
    checkNotNull(pathName);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        return pathName.matches(uri.getPath());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has path: ");
        description.appendDescriptionOf(pathName);
      }
    };
  }

  public static Matcher<Uri> hasParamWithValue(String paramName, String paramVal) {
    return hasParamWithValue(is(paramName), is(paramVal));
  }

  public static Matcher<Uri> hasParamWithValue(
      final Matcher<String> paramName, final Matcher<String> paramVal) {
    checkNotNull(paramName);
    checkNotNull(paramVal);
    final Matcher<QueryParamEntry> qpe = queryParamEntry(paramName, paramVal);
    final Matcher<Iterable<? super QueryParamEntry>> matcherImpl = hasItem(qpe);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        List<QueryParamEntry> qpes = new ArrayList<>();
        for (String name : getQueryParameterNames(uri)) {
          qpes.add(new QueryParamEntry(name, uri.getQueryParameters(name)));
        }
        return matcherImpl.matches(qpes);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has param with: name: ");
        description.appendDescriptionOf(paramName);
        description.appendText(" value: ");
        description.appendDescriptionOf(paramVal);
      }
    };
  }

  private static Matcher<QueryParamEntry> queryParamEntry(
      final Matcher<String> paramName, final Matcher<String> paramVal) {
    final Matcher<Iterable<? super String>> valMatcher = hasItem(paramVal);

    return new TypeSafeMatcher<QueryParamEntry>(QueryParamEntry.class) {

      @Override
      public boolean matchesSafely(QueryParamEntry qpe) {
        return paramName.matches(qpe.paramName) && valMatcher.matches(qpe.paramVals);
      }

      @Override
      public void describeTo(Description description) {
        description.appendDescriptionOf(paramName);
        description.appendDescriptionOf(paramVal);
      }
    };
  }

  public static Matcher<Uri> hasScheme(String scheme) {
    return hasScheme(is(scheme));
  }

  public static Matcher<Uri> hasScheme(final Matcher<String> schemeMatcher) {
    checkNotNull(schemeMatcher);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        return schemeMatcher.matches(uri.getScheme());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has scheme: ");
        description.appendDescriptionOf(schemeMatcher);
      }
    };
  }

  public static Matcher<Uri> hasSchemeSpecificPart(String scheme, String schemeSpecificPart) {
    return hasSchemeSpecificPart(is(scheme), is(schemeSpecificPart));
  }

  public static Matcher<Uri> hasSchemeSpecificPart(
      final Matcher<String> schemeMatcher, final Matcher<String> schemeSpecificPartMatcher) {
    checkNotNull(schemeMatcher);
    checkNotNull(schemeSpecificPartMatcher);

    return new TypeSafeMatcher<Uri>() {

      @Override
      public boolean matchesSafely(Uri uri) {
        return schemeMatcher.matches(uri.getScheme())
            && schemeSpecificPartMatcher.matches(uri.getSchemeSpecificPart());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has scheme specific part: scheme: ");
        description.appendDescriptionOf(schemeMatcher);
        description.appendText(" scheme specific part: ");
        description.appendDescriptionOf(schemeSpecificPartMatcher);
      }
    };
  }
}
