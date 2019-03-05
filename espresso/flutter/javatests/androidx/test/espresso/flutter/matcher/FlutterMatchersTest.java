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

import static androidx.test.espresso.flutter.matcher.FlutterMatchers.isFlutterView;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.testapp.flutter.app.FlutterActivity;
import io.flutter.view.FlutterView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test cases for {@link FlutterMatchers}. */
@RunWith(AndroidJUnit4.class)
public class FlutterMatchersTest {

  private ActivityScenario<FlutterActivity> scenario;

  @Before
  public void setUp() throws Exception {
    scenario = ActivityScenario.launch(FlutterActivity.class);
  }

  @Test
  public void isFlutterViewTest() {
    scenario.onActivity(
        activity -> {
          View flutterView = new FlutterView(activity);
          assertTrue(isFlutterView().matches(flutterView));
        });
  }

  @Test
  public void isNotFlutterViewTest() {
    scenario.onActivity(
        activity -> {
          View nonFlutterView = new View(activity);
          assertFalse(isFlutterView().matches(nonFlutterView));
        });
  }

  @Test
  public void isFlutterViewTest_description() {
    Matcher<View> flutterViewMatcher = FlutterMatchers.isFlutterView();
    Description description = new StringDescription();
    flutterViewMatcher.describeTo(description);
    assertThat(description.toString()).isEqualTo("is a FlutterView");
  }
}
