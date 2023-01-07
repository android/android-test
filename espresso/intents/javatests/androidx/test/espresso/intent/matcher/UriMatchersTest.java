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

import static androidx.test.espresso.intent.matcher.UriMatchers.getQueryParameterNames;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasHost;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasParamWithName;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasParamWithValue;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasPath;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasScheme;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasSchemeSpecificPart;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.Set;
import kotlin.collections.SetsKt;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link UriMatchers}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class UriMatchersTest {

  private final Uri uri =
      Uri.parse("https://www.google.com/search?q=Matcher&aq=f&oq=Matcher&sourceid=chrome&ie=UTF-8");

  @Test
  public void hasHostTesting() {
    String host = "www.google.com";
    assertTrue("HasHost didn't match", hasHost(host).matches(uri));
    assertTrue("HasHost didn't match", hasHost(equalTo(host)).matches(uri));
  }

  @Test
  public void hasHostDoesNotMatch() {
    String host = "www.android.com";
    assertFalse("HasHost Matched, But it shouldn't have", hasHost(host).matches(uri));
    assertFalse("HasHost Matched, But it shouldn't have", hasHost(equalTo(host)).matches(uri));
  }

  @Test
  public void hasPathTesting() {
    String pathName = "/search";
    assertTrue("HasPath didn't match", hasPath(pathName).matches(uri));
    assertTrue("HasPath didn't match", hasPath(equalTo(pathName)).matches(uri));
  }

  @Test
  public void hasPathDoesNotMatch() {
    String pathName = "/query";
    assertFalse("HasPath Matched, But it shouldn't have", hasPath(pathName).matches(uri));
    assertFalse("HasPath Matched, But it shouldn't have", hasPath(equalTo(pathName)).matches(uri));
  }

  @Test
  public void hasSchemeTesting() {
    Uri schemeUri = Uri.parse("market://details?id=com.google.android.apps.plus");
    assertTrue("HasPath didn't match", hasScheme("market").matches(schemeUri));
    assertTrue("HasPath didn't match", hasScheme(equalTo("market")).matches(schemeUri));
  }

  @Test
  public void hasSchemeDoesNotMatch() {
    Uri schemeUri = Uri.parse("market://details?id=com.google.android.apps.plus");
    assertFalse("HasPath Matched, But it shouldn't have", hasScheme("details").matches(schemeUri));
    assertFalse(
        "HasPath Matched, But it shouldn't have", hasScheme(equalTo("details")).matches(schemeUri));
  }

  @Test
  public void hasSchemeSpecificPartTesting() {
    Uri schemeUri = Uri.parse("tel:123456789");
    Matcher<Uri> hasSchemeMatcher = hasSchemeSpecificPart(equalTo("tel"), equalTo("123456789"));
    assertTrue(
        "HasScheme didn't match", hasSchemeSpecificPart("tel", "123456789").matches(schemeUri));
    assertTrue("HasScheme didn't match", hasSchemeMatcher.matches(schemeUri));
  }

  @Test
  public void hasSchemeSpecificPartDoesNotMatch() {
    Uri schemeUri = Uri.parse("tel:123456789");
    Matcher<Uri> hasSchemeMatcher = hasSchemeSpecificPart(equalTo("tel"), equalTo("987654321"));
    assertFalse(
        "HasScheme Matched, But it shouldn't have",
        hasSchemeSpecificPart("tel", "987654321").matches(schemeUri));
    assertFalse("HasScheme Matched, But it shouldn't have", hasSchemeMatcher.matches(schemeUri));
  }

  @Test
  public void hasParamName() {
    String paramName = "sourceid";
    assertTrue("HasParam didn't match", hasParamWithName(paramName).matches(uri));
    assertTrue("HasParam didn't match", hasParamWithName(equalTo(paramName)).matches(uri));
  }

  @Test
  public void hasParamNameDoesNotMatch() {
    String paramName = "param";
    assertFalse(
        "HasParamName Matched, But it shouldn't have", hasParamWithName(paramName).matches(uri));
    assertFalse(
        "HasParamName Matched, But it shouldn't have",
        hasParamWithName(equalTo(paramName)).matches(uri));
  }

  @Test
  public void hasParamWithValueTesting() {
    Matcher<Uri> hasParamWithValue = hasParamWithValue(equalTo("sourceid"), equalTo("chrome"));
    assertTrue(
        "HasParamWithValue didn't match", hasParamWithValue("sourceid", "chrome").matches(uri));
    assertTrue("HasParamWithValue didn't match", hasParamWithValue.matches(uri));
  }

  @Test
  public void hasParamWithValueMatcherDoesNotMatch() {
    Matcher<Uri> hasParamWithValue = hasParamWithValue(equalTo("sourceid"), equalTo("google"));
    assertFalse(
        "HasParamWithValue Matched, But it shouldn't have",
        hasParamWithValue("sourceid", "google").matches(uri));
    assertFalse("HasParamWithValue Matched, But it shouldn't have", hasParamWithValue.matches(uri));
  }

  @Test
  public void allOfMatcher() {
    @SuppressWarnings("unchecked")
    Matcher<Uri> allOfMatcher =
        allOf(
            hasHost(equalTo("www.google.com")),
            hasParamWithName("sourceid"),
            hasParamWithName("aq"),
            hasPath("/search"),
            hasParamWithValue("q", "Matcher"),
            hasParamWithValue("aq", "f"),
            hasParamWithValue("oq", "Matcher"),
            hasParamWithValue("sourceid", "chrome"),
            hasParamWithValue("ie", "UTF-8"));

    assertTrue("AllOf matcher didn't match", allOfMatcher.matches(uri));
  }

  @Test
  public void allOfMatcherForMailToScheme() {
    @SuppressWarnings("unchecked")
    Uri schemeUri = Uri.parse("mailto:nobody@google.com");
    Matcher<Uri> allOfMatcher =
        allOf(
            hasScheme(equalTo("mailto")),
            hasSchemeSpecificPart(equalTo("mailto"), equalTo("nobody@google.com")));

    assertTrue("AllOf matcher for MailTo Scheme didn't match", allOfMatcher.matches(schemeUri));
  }

  @Test
  public void allOfWithWrongMatcher() {
    @SuppressWarnings("unchecked")
    Matcher<Uri> allOfMatcher =
        allOf(
            hasHost("www.google.com"), hasParamWithName("sourceid"),
            hasParamWithName("param"), hasPath("/search"));
    assertFalse("Matcher shouldn't have matched.", allOfMatcher.matches(uri));
  }

  @Test
  public void anyOfMatcherWithOneMatch() {
    @SuppressWarnings("unchecked")
    Matcher<Uri> anyOfMatcher =
        anyOf(
            hasHost(equalTo("www.google.com")),
            hasParamWithName(equalTo("param1")),
            hasParamWithName(equalTo("param2")));

    assertTrue("AnyOf matcher didn't match", anyOfMatcher.matches(uri));
  }

  @Test
  public void anyOfMatcherWithMoreThanOneMatch() {
    @SuppressWarnings("unchecked")
    Matcher<Uri> anyOfMatcher =
        anyOf(
            hasHost(equalTo("www.google.com")), hasPath(equalTo("/search")),
            hasParamWithName(equalTo("param2")),
                hasParamWithValue(equalTo("ie"), equalTo("UTF-8")));

    assertTrue("AnyOf matcher didn't match", anyOfMatcher.matches(uri));
  }

  @Test
  public void getQueryParameterNamesTesting() {
    Set<String> paramNames = getQueryParameterNames(uri);
    assertTrue(paramNames.equals(SetsKt.setOf("q", "aq", "oq", "sourceid", "ie")));
  }

  @Test
  public void matches() {
    Matcher<Uri> matcher =
        allOf(
            hasHost(equalTo("www.google.com")),
            hasPath(equalTo("/search")),
            hasParamWithValue(equalTo("q"), equalTo("Matcher")),
            hasParamWithValue(equalTo("aq"), equalTo("f")));
    Uri uri = Uri.parse("https://www.google.com/search?q=Matcher&aq=f");
    assertTrue("Matches failed.", matcher.matches(uri));
  }
}
