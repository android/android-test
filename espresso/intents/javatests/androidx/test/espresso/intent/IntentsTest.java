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

package androidx.test.espresso.intent;

import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static kotlin.collections.CollectionsKt.listOf;
import static kotlin.collections.CollectionsKt.mutableListOf;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Unit tests for {@Intents}. */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class IntentsTest {

  @Rule public ExpectedException expectedException = none();

  private Intents intents;

  private static final ResettingStubber RESETTING_STUB =
      new ResettingStubber() {
        @Override
        public void setActivityResultForIntent(Matcher<Intent> matcher, ActivityResult result) {}

        @Override
        public void setActivityResultFunctionForIntent(
            Matcher<Intent> matcher, ActivityResultFunction result) {}

        @Override
        public ActivityResult getActivityResultForIntent(Intent intent) {
          return null;
        }

        @Override
        public void initialize() {}

        @Override
        public boolean isInitialized() {
          return true;
        }

        @Override
        public void reset() {}
      };

  @Before
  public void setUp() throws Exception {
    intents = new Intents(RESETTING_STUB);
    intents.internalInit();
  }

  @Test
  public void validateAnyIntent() {
    intents.internalIntended(
        anyIntent(),
        times(1),
        listOf(buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.some.package")));
  }

  @Test
  public void validateToOnePackage() {
    intents.internalIntended(
        toPackage("com.google.some.package"),
        times(1),
        listOf(buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.some.package")));
  }

  @Test
  public void validateToMultiplePackagesOne() {
    intents.internalIntended(
        toPackage("com.google.some.package"),
        times(1),
        listOf(
            buildVerifiableIntent(
                Intent.ACTION_VIEW, "com.google.some.package", "com.google.android.other")));
  }

  @Test
  public void validateToMultiplePackagesTwo() {
    intents.internalIntended(
        allOf(toPackage("com.google.some.package"), toPackage("com.google.android.other")),
        times(1),
        listOf(
            buildVerifiableIntent(
                Intent.ACTION_VIEW, "com.google.some.package", "com.google.android.other")));
  }

  @Test
  public void validateNoPackagesFail() {
    expectedException.expect(AssertionFailedError.class);
    intents.internalIntended(
        toPackage("com.google.some.package"), times(1), new ArrayList<VerifiableIntent>());
  }

  @Test
  public void validateMultiplePackagesFail() {
    expectedException.expect(AssertionFailedError.class);
    intents.internalIntended(
        toPackage("com.google.some.fail"),
        times(1),
        listOf(
            buildVerifiableIntent(
                Intent.ACTION_VIEW, "com.google.some.package", "com.google.android.other")));
  }

  @Test
  public void intended_MultipleCalls() {
    List<VerifiableIntent> intents =
        listOf(
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.A"),
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.B"),
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.C"));
    this.intents.internalIntended(toPackage("com.google.android.B"), times(1), intents);
    this.intents.internalIntended(toPackage("com.google.android.A"), times(1), intents);
    this.intents.internalIntended(toPackage("com.google.android.C"), times(1), intents);
  }

  @Test
  public void intended_MultipleCallsWithSameIntent() {
    List<VerifiableIntent> intents =
        listOf(
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.A"),
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.A"));
    this.intents.internalIntended(toPackage("com.google.android.A"), times(2), intents);
    this.intents.internalIntended(toPackage("com.google.android.A"), times(2), intents);
    try {
      this.intents.internalIntended(anyIntent(), times(1), intents);
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException("Expected to fail on previous line.");
    } catch (AssertionFailedError e) {
      // expected.
      Pattern p =
          Pattern.compile(
              "Wanted to match 1 intents\\. Actually matched 2 intents\\..*"
                  + "Matched intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n\n"
                  + "Recorded intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A.*",
              Pattern.DOTALL);
      assertTrue(
          String.format("Pattern\n\n%s\n\ndoesn't match\n\n%s", p, e.getMessage()),
          p.matcher(e.getMessage()).matches());
    }

    try {
      this.intents.internalIntended(anyIntent(), times(3), intents);
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException("Expected to fail on previous line.");
    } catch (AssertionFailedError e) {
      // expected.
      Pattern p =
          Pattern.compile(
              "Wanted to match 3 intents\\. Actually matched 2 intents\\..*"
                  + "Matched intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n\n"
                  + "Recorded intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A.*",
              Pattern.DOTALL);
      assertTrue(
          String.format("Pattern\n\n%s\n\ndoesn't match\n\n%s", p, e.getMessage()),
          p.matcher(e.getMessage()).matches());
    }

    this.intents.internalIntended(anyIntent(), times(2), intents);
  }

  @Test
  public void intended_NoIntents() {
    intents.internalIntended(anyIntent(), times(0), new ArrayList<VerifiableIntent>());
  }

  @Test
  public void assertNoUnverifiedIntents() {
    List<VerifiableIntent> intents =
        listOf(
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.A"),
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.B"),
            buildVerifiableIntent(Intent.ACTION_VIEW, "com.google.android.A"));

    // Match Intent A.
    this.intents.internalIntended(toPackage("com.google.android.A"), Intents.times(2), intents);

    // assertNoUnverifiedIntents() should fail because Intent B hasn't been verified.
    try {
      this.intents.internalIntended(anyIntent(), VerificationModes.noUnverifiedIntents(), intents);
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException("Expected to fail on previous line.");
    } catch (AssertionFailedError e) {
      // expected.
      Pattern p =
          Pattern.compile(
              "Found unverified intents.\n\n"
                  + "Unverified intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n\n"
                  + "Recorded intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A.*",
              Pattern.DOTALL);
      assertTrue(
          String.format("Pattern\n\n%s\n\ndoesn't match\n\n%s", p, e.getMessage()),
          p.matcher(e.getMessage()).matches());
    }

    // Try, but fail to match B.
    try {
      this.intents.internalIntended(toPackage("com.google.android.B"), Intents.times(2), intents);
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException("Expected to fail on previous line.");
    } catch (AssertionFailedError e) {
      // expected.
      Pattern p =
          Pattern.compile(
              "Wanted to match 2 intents\\. Actually matched 1 intents\\..*"
                  + "Matched intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n\n"
                  + "Recorded intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A.*",
              Pattern.DOTALL);
      assertTrue(
          String.format("Pattern\n\n%s\n\ndoesn't match\n\n%s", p, e.getMessage()),
          p.matcher(e.getMessage()).matches());
    }

    // assertNoUnverifiedIntents() should fail because Intent B still hasn't been verified. The
    // previous call to intended() should have left Intent B still unverified since it failed.
    try {
      this.intents.internalIntended(anyIntent(), VerificationModes.noUnverifiedIntents(), intents);
      // Can't call fail() because it throws an AssertionFailedError, just like intended() does.
      throw new IllegalStateException("Expected to fail on previous line.");
    } catch (AssertionFailedError e) {
      // expected.
      Pattern p =
          Pattern.compile(
              "Found unverified intents.\n\n"
                  + "Unverified intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n\n"
                  + "Recorded intents:\n"
                  + "-[^\n]*com\\.google\\.android\\.A[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.B[^\n]*\n"
                  + "-[^\n]*com\\.google\\.android\\.A.*",
              Pattern.DOTALL);
      assertTrue(
          String.format("Pattern\n\n%s\n\ndoesn't match\n\n%s", p, e.getMessage()),
          p.matcher(e.getMessage()).matches());
    }

    // Match Intent B.
    this.intents.internalIntended(toPackage("com.google.android.B"), Intents.times(1), intents);

    // Multiple calls should not fail.
    this.intents.internalIntended(anyIntent(), VerificationModes.noUnverifiedIntents(), intents);
    this.intents.internalIntended(anyIntent(), VerificationModes.noUnverifiedIntents(), intents);
  }

  private static VerifiableIntent buildVerifiableIntent(String action, String... packages) {
    return new VerifiableIntentImpl(
        new ResolvedIntentImpl(new Intent(action), buildResolveInfoList(packages)));
  }

  private static List<ResolveInfo> buildResolveInfoList(String... resolvePackage) {
    List<ResolveInfo> resolveInfoList = mutableListOf();
    for (String pkg : resolvePackage) {
      ResolveInfo resolveInfo = new ResolveInfo();
      resolveInfo.activityInfo = new ActivityInfo();
      resolveInfo.activityInfo.packageName = pkg;
      resolveInfoList.add(resolveInfo);
    }
    return resolveInfoList;
  }

  @After
  public void tearDown() throws Exception {
    intents.internalRelease();
    intents = null;
  }
}
