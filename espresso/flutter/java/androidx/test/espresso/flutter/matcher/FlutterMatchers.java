/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.espresso.flutter.matcher;

import androidx.annotation.VisibleForTesting;
import android.view.View;
import androidx.test.espresso.matcher.BoundedMatcher;
import io.flutter.view.FlutterView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/** A collection of matchers that match {@link FlutterView}s or Flutter widgets. */
public final class FlutterMatchers {
  /** Returns a matcher that matches a {@link FlutterView}. */
  public static Matcher<View> isFlutterView() {
    return new IsFlutterViewMatcher();
  }

  @VisibleForTesting
  static final class IsFlutterViewMatcher extends BoundedMatcher<View, FlutterView> {

    private IsFlutterViewMatcher() {
      super(FlutterView.class);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is a FlutterView");
    }

    @Override
    public boolean matchesSafely(FlutterView flutterView) {
      return true;
    }
  }
}
