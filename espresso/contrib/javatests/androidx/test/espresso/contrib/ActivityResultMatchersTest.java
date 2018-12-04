/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.espresso.contrib;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ActivityResultMatchersTest {

  private static Matcher<Intent> isSameIntent(final Intent expectedIntent) {

    return new TypeSafeMatcher<Intent>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("is Intent: " + expectedIntent);
      }

      @Override
      public boolean matchesSafely(Intent intent) {
        return expectedIntent.filterEquals(intent);
      }
    };
  }

  @Test
  public void successHasResultData() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    ActivityResult activityResult = new ActivityResult(1, intent);
    assertThat(
        ActivityResultMatchers.hasResultData(isSameIntent(intent)).matches(activityResult),
        is(true));
  }

  @Test
  public void failureHasResultData() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    ActivityResult activityResult = new ActivityResult(1, intent);
    assertThat(
        ActivityResultMatchers.hasResultData(isSameIntent(new Intent(Intent.ACTION_VIEW)))
            .matches(activityResult),
        is(false));
  }

  @Test
  public void successHasResultCode() {
    ActivityResult activityResult = new ActivityResult(1, null);
    assertThat(ActivityResultMatchers.hasResultCode(1).matches(activityResult), is(true));
  }

  @Test
  public void failureHasResultCode() {
    ActivityResult activityResult = new ActivityResult(1, null);
    assertThat(ActivityResultMatchers.hasResultCode(2).matches(activityResult), is(false));
  }
}
