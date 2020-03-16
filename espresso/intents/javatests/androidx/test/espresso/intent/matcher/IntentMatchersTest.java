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
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.IntentMatchers.filterEquals;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasDataString;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasHost;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasParamWithValue;
import static androidx.test.espresso.intent.matcher.UriMatchers.hasPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.test.espresso.intent.ResolvedIntent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.HashSet;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Unit tests for {@link androidx.test.espresso.intent.matcher.IntentMatchers}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class IntentMatchersTest {

  @Rule public ExpectedException expectedException = none();

  private final Uri uri = Uri.parse("https://www.google.com/search?q=Matcher");

  @Test
  public void matchesTesting() {
    Matcher<Intent> matcher =
        allOf(
            hasAction(equalTo(Intent.ACTION_VIEW)),
            hasData(
                allOf(
                    hasHost(equalTo("www.google.com")),
                    hasPath(equalTo("/search")),
                    hasParamWithValue(equalTo("q"), equalTo("Matcher")))),
            hasCategories(hasItem(equalTo("category"))),
            hasType(equalTo(Context.ACTIVITY_SERVICE)),
            hasExtras(hasEntry(equalTo("key"), equalTo("value"))));
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE)
            .putExtra("key", "value");
    assertTrue(matcher.matches(intent));
  }

  @Test
  public void matchesIntentWithNoTypeAndMatcherWithType() {
    Matcher<Intent> matcher =
        allOf(
            hasAction(equalTo(Intent.ACTION_VIEW)),
            hasData(
                allOf(
                    hasHost(equalTo("www.google.com")),
                    hasPath(equalTo("/search")),
                    hasParamWithValue(equalTo("q"), equalTo("Matcher")))),
            hasCategories(hasItem(equalTo("category"))),
            hasType(equalTo(Context.ACTIVITY_SERVICE)),
            hasExtras(hasEntry(equalTo("key"), equalTo("value"))));
    assertFalse(
        matcher.matches(new Intent(Intent.ACTION_VIEW).setData(uri).addCategory("category")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentWithTypeAndMatcherWithNoType() {
    Matcher<Intent> matcher = hasAction(equalTo(Intent.ACTION_VIEW));
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE);
    assertTrue(matcher.matches(intent));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentWithNoExtraAndMatcherWithOneExtra() {
    Matcher<Intent> matcher =
        allOf(
            hasAction(equalTo(Intent.ACTION_VIEW)),
            hasData(
                allOf(
                    hasHost(equalTo("www.google.com")),
                    hasPath(equalTo("/search")),
                    hasParamWithValue(equalTo("q"), equalTo("Matcher")))),
            hasCategories(hasItem(equalTo("category"))),
            hasType(equalTo(Context.ACTIVITY_SERVICE)),
            hasExtras(hasEntry(equalTo("key"), equalTo("value"))));
    assertFalse(
        matcher.matches(
            new Intent(Intent.ACTION_VIEW)
                .addCategory("category")
                .setDataAndType(uri, Context.ACTIVITY_SERVICE)));
  }

  @Test
  public void matchesIntentWithOneExtraAndMatcherWithNoExtra() {
    Matcher<Intent> matcher =
        allOf(
            hasAction(equalTo(Intent.ACTION_VIEW)),
            hasData(
                allOf(
                    hasHost(equalTo("www.google.com")),
                    hasPath(equalTo("/search")),
                    hasParamWithValue(equalTo("q"), equalTo("Matcher")))),
            hasCategories(hasItem(equalTo("category"))),
            hasType(equalTo(Context.ACTIVITY_SERVICE)));
    assertTrue(
        matcher.matches(
            new Intent(Intent.ACTION_VIEW)
                .addCategory("category")
                .setDataAndType(uri, Context.ACTIVITY_SERVICE)
                .putExtra("key", "value")));
  }

  @Test
  public void matchesIntentWithMultipleExtraAndMatcherWithMultipleExtra() {
    Matcher<Intent> matcher =
        hasExtras(
            allOf(
                hasEntry(equalTo("key1"), equalTo("value1")),
                hasEntry(equalTo("key1"), equalTo("value1"))));
    assertTrue(
        matcher.matches(
            new Intent(Intent.ACTION_VIEW)
                .addCategory("category")
                .setDataAndType(uri, Context.ACTIVITY_SERVICE)
                .putExtra("key1", "value1")
                .putExtra("key2", "value2")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentWithNoCategoryAndMatcherWithOneCategory() {
    Matcher<Intent> matcher = hasCategories(hasItem(equalTo("category")));
    assertFalse(matcher.matches(new Intent(Intent.ACTION_VIEW, uri)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentWithOneCategoryAndMatcherWithNoCategory() {
    Matcher<Intent> matcher = hasAction(equalTo(Intent.ACTION_VIEW));
    assertTrue(matcher.matches(new Intent(Intent.ACTION_VIEW, uri).addCategory("category")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentAndMatcherWithMultipleCategory() {
    Matcher<Intent> matcher =
        hasCategories(allOf(hasItem(equalTo("category")), hasItem(equalTo("category1"))));
    assertTrue(
        matcher.matches(
            new Intent(Intent.ACTION_VIEW, uri).addCategory("category").addCategory("category1")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void matchesIntentWithOneCategoryAndMatcherWithMultipleCategories() {
    Matcher<Intent> matcher =
        hasCategories(
            allOf(
                hasItem(equalTo("category")),
                hasItem(equalTo("category1")),
                hasItem(equalTo("category2"))));
    assertFalse(matcher.matches(new Intent(Intent.ACTION_VIEW, uri).addCategory("category")));
  }

  @Test
  public void hasActionTesting() {
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE)
            .putExtra("key", "value");
    assertTrue(hasAction(Intent.ACTION_VIEW).matches(intent));
    assertTrue(hasAction(equalTo(Intent.ACTION_VIEW)).matches(intent));
  }

  @Test
  public void hasActionDoesNotMatch() {
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE)
            .putExtra("key", "value");
    assertFalse(hasAction(Intent.ACTION_DIAL).matches(intent));
    assertFalse(hasAction(equalTo(Intent.ACTION_DIAL)).matches(intent));
  }

  @Test
  public void hasCategoriesTesting() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addCategory("category");
    intent.addCategory("category1");
    intent.addCategory("category2");

    assertTrue(hasCategories(intent.getCategories()).matches(intent));
    assertTrue(hasCategories(hasItems("category", "category1", "category2")).matches(intent));
  }

  @Test
  public void hasCategoriesDoesNotMatch() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addCategory("category");
    intent.addCategory("category1");
    intent.addCategory("category2");

    HashSet<String> set = new HashSet<String>();
    set.add("category");
    set.add("category1");
    set.add("category8");

    assertFalse(hasCategories(set).matches(intent));
    assertFalse(hasCategories(hasItems("category", "category1", "category8")).matches(intent));
  }

  @Test
  public void hasComponentTesting() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    String pkg = "com.some.wonderful.package.name";
    String cls = pkg + ".FooBar";
    ComponentName c = new ComponentName(pkg, cls);
    intent.setComponent(c);

    assertTrue(hasComponent(intent.getComponent().getClassName()).matches(intent));
    assertTrue(hasComponent(intent.getComponent()).matches(intent));
    assertTrue(hasComponent(equalTo(c)).matches(intent));
  }

  @Test
  public void hasComponentDoesNotMatch() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    String pkg = "com.some.wonderful.package.name";
    String cls = pkg + ".FooBar";
    ComponentName c = new ComponentName(pkg, cls);
    intent.setComponent(c);

    c = new ComponentName(pkg, pkg + ".Baz");
    assertFalse(hasComponent("not_component").matches(intent));
    assertFalse(hasComponent(c).matches(intent));
    assertFalse(hasComponent(equalTo(c)).matches(intent));
  }

  @Test
  public void hasDataTesting() {
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE)
            .putExtra("key1", "value1");

    assertTrue(hasData(uri.toString()).matches(intent));
    assertTrue(hasData(uri).matches(intent));
    assertTrue(hasData(equalTo(uri)).matches(intent));
    assertTrue(hasDataString(equalTo(uri.toString())).matches(intent));
  }

  @Test
  public void hasDataDoesMatch() {
    Intent intent =
        new Intent(Intent.ACTION_VIEW)
            .addCategory("category")
            .setDataAndType(uri, Context.ACTIVITY_SERVICE)
            .putExtra("key1", "value1");

    assertFalse(hasData("https://www.google.com/search?q=NotMatcher").matches(intent));
    assertFalse(hasData(Uri.parse("https://www.google.com/search?q=NotMatcher")).matches(intent));
    assertFalse(
        hasData(equalTo(Uri.parse("https://www.google.com/search?q=NotMatcher"))).matches(intent));
    assertFalse(
        hasDataString(equalTo("https://www.google.com/search?q=NotMatcher")).matches(intent));
  }

  @Test
  public void hasTypeTesting() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addCategory("category");
    intent.setDataAndType(uri, Context.ACTIVITY_SERVICE);
    intent.putExtra("key1", "value1");

    assertTrue(hasType(Context.ACTIVITY_SERVICE).matches(intent));
    assertTrue(hasType(equalTo(Context.ACTIVITY_SERVICE)).matches(intent));
  }

  @Test
  public void hasTypeDoesNotMatch() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addCategory("category");
    intent.setDataAndType(uri, Context.ACTIVITY_SERVICE);
    intent.putExtra("key1", "value1");

    assertFalse(hasType(Context.ACCOUNT_SERVICE).matches(intent));
    assertFalse(hasType(equalTo(Context.ACCOUNT_SERVICE)).matches(intent));
  }

  @Test
  public void toPackageTesting() {
    final String pkg = "pkg1";
    ResolvedIntent intent = new FakeResolvedIntent(pkg);
    assertTrue(toPackage(pkg).matches(intent));
    assertFalse(toPackage("notpkg1").matches(intent));
    expectedException.expect(RuntimeException.class);
    toPackage("whatever").matches(new Intent(Intent.ACTION_VIEW));
  }

  @Test
  public void hasExtraWithKeyTesting() {
    Intent intent = new Intent().putExtra("key1", "value1").putExtra("key2", 100.0);
    assertTrue(hasExtraWithKey("key1").matches(intent));
    assertTrue(hasExtraWithKey("key2").matches(intent));
  }

  @Test
  public void hasExtraTesting() {
    Intent intent = new Intent().putExtra("key1", "value1").putExtra("key2", 100.0);
    assertTrue(hasExtra("key1", "value1").matches(intent));
    assertTrue(hasExtra("key2", 100.0).matches(intent));
  }

  @Test
  public void hasExtraWithKeyDoesNotMatch() {
    Intent intent = new Intent().putExtra("key1", "value1");
    assertFalse(hasExtraWithKey("key2").matches(intent));
  }

  @Test
  public void hasExtraDoesNotMatch() {
    Intent intent = new Intent().putExtra("key1", "value1");
    assertFalse(hasExtra("key1", "value2").matches(intent));
    assertFalse(hasExtra("key2", "value1").matches(intent));
  }

  @Test
  public void hasPackageMatches() {
    Intent intent = new Intent().setPackage("com.foo.bar");
    assertTrue(hasPackage("com.foo.bar").matches(intent));
    assertTrue(hasPackage(equalTo("com.foo.bar")).matches(intent));
  }

  @Test
  public void hasPackageDoesNotMatch() {
    Intent intent = new Intent().setPackage("com.foo.bar");
    assertFalse(hasPackage("com.baz.qux").matches(intent));
    assertFalse(hasPackage(equalTo("com.baz.qux")).matches(intent));
  }

  @Test
  public void hasPackageNoPackage() {
    Intent intent = new Intent();
    assertFalse(hasPackage("com.foo.bar").matches(intent));
    assertFalse(hasPackage(equalTo("com.foo.bar")).matches(intent));
  }

  @Test
  public void hasFlagsWithSingleFlag() {
    Intent intent = new Intent();
    assertTrue(hasFlags(0).matches(intent));
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    assertTrue(hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).matches(intent));
  }

  @Test
  public void hasFlagsWithMultipleFlags() {
    Intent intent = new Intent();
    intent.setFlags(
        Intent.FLAG_DEBUG_LOG_RESOLUTION
            | Intent.FLAG_ACTIVITY_NO_HISTORY
            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    assertTrue(hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION).matches(intent));
    assertTrue(hasFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).matches(intent));
    assertTrue(hasFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS).matches(intent));
    assertTrue(
        hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .matches(intent));
    assertTrue(
        hasFlags(
                Intent.FLAG_DEBUG_LOG_RESOLUTION
                    | Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .matches(intent));
  }

  @Test
  public void hasFlagsWithCustomFlags() {
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | 8 | 4 | 2);
    assertTrue((hasFlags(8 | 2)).matches(intent));
    assertTrue((hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | 4)).matches(intent));
    assertTrue((hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION, 8, 4)).matches(intent));
  }

  @Test
  public void hasFlagsDoesNotMatch() {
    Intent intent = new Intent();
    assertFalse(hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION).matches(intent));
    intent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | Intent.FLAG_ACTIVITY_NO_HISTORY);
    assertFalse(hasFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS).matches(intent));
    assertFalse(
        hasFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .matches(intent));
  }

  @Test
  public void hasFlagsWithCustomFlagsDoesNotMatch() {
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION | 8);
    assertFalse((hasFlags(16)).matches(intent));
    assertFalse((hasFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | 8)).matches(intent));
  }

  @Test
  public void filterEqualsMatches() {
    Intent intent =
        new Intent("foo.action", uri)
            .setType("text")
            .setClassName("com.foo.bar", "com.foo.bar.Baz")
            .addCategory("category");
    Intent intentWithFlagsAndExtras =
        new Intent(intent).addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION).putExtra("foo", "bar");
    assertTrue(filterEquals(intent).matches(intentWithFlagsAndExtras));
  }

  @Test
  public void filterEqualsDoesNotMatch() {
    Intent intent =
        new Intent("foo.action", uri)
            .setClassName("com.foo.bar", "com.foo.bar.Baz")
            .addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION)
            .setType("text");
    Intent intentWithDifferentAction = new Intent(intent).setAction("bar.action");
    assertFalse(filterEquals(intent).matches(intentWithDifferentAction));
    Intent intentWithDifferentData =
        new Intent(intent).setData(Uri.parse("https://www.google.com/search?q=NotMatcher"));
    assertFalse(filterEquals(intent).matches(intentWithDifferentData));
    Intent intentWithDifferentType = new Intent(intent).setType("img");
    assertFalse(filterEquals(intent).matches(intentWithDifferentType));
    Intent intentWithDifferentComponent =
        new Intent(intent).setClassName("com.foo.bar", "com.foo.bar.Other");
    assertFalse(filterEquals(intent).matches(intentWithDifferentComponent));
    Intent intentWithDifferentCategories = new Intent(intent).addCategory("category");
    assertFalse(filterEquals(intent).matches(intentWithDifferentCategories));
  }

  @Test
  public void isInternalTesting() {
    String targetPackage = getApplicationContext().getPackageName();
    ComponentName targetComponent = new ComponentName(targetPackage, targetPackage + ".SomeClass ");
    assertTrue(isInternal().matches(new Intent().setComponent(targetComponent)));
    assertFalse(not(isInternal()).matches(new Intent().setComponent(targetComponent)));
  }

  @Test
  public void isInternalDoesNotMatch() {
    assertFalse(isInternal().matches(new Intent())); // no target package
    ComponentName externalComponent =
        new ComponentName("com.google.android", "com.google.android.SomeClass");
    assertFalse(isInternal().matches(new Intent().setComponent(externalComponent)));
  }

  private class FakeResolvedIntent extends Intent implements ResolvedIntent {
    private final String pkg;

    FakeResolvedIntent(String pkg) {
      this.pkg = pkg;
    }

    @Override
    public boolean canBeHandledBy(String appPackage) {
      return appPackage.equals(pkg);
    }

    @Override
    public Intent getIntent() {
      return this;
    }
  };
}
