/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.ui.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.setFailureHandler;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import androidx.test.espresso.FailureHandler;
import androidx.test.filters.LargeTest;
import org.hamcrest.Matcher;

/**
 * A sample of how to set a non-default {@link FailureHandler}.
 */
@LargeTest
public class CustomFailureHandlerTest extends ActivityInstrumentationTestCase2<MainActivity> {

  private static final String TAG = "CustomFailureHandlerTes";

  @SuppressWarnings("deprecation")
  public CustomFailureHandlerTest() {
    // This constructor was deprecated - but we want to support lower API levels.
    super("androidx.test.ui.app", MainActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
    setFailureHandler(new CustomFailureHandler());
  }

  public void testWithCustomFailureHandler() {
    try {
      onView(withText("does not exist")).perform(click());
      fail("Expected MySpecialExceptioffn");
    } catch (MySpecialException expected) {
      Log.e(TAG, "Special exception is special and expected: ", expected);
    }
  }

  /**
   * A {@link FailureHandler} that re-throws all exceptions as
   * {@link MySpecialException}.
   */
  private static class CustomFailureHandler implements FailureHandler {
    @Override
    public void handle(Throwable error, Matcher<View> viewMatcher) {
      throw new MySpecialException(error);
    }
  }

  private static class MySpecialException extends RuntimeException {
    MySpecialException(Throwable cause) {
      super(cause);
    }
  }
}
