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

package androidx.test.espresso.matcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link HasBackgroundMatcher}. */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class HasBackgroundMatcherTest {

  private Context context;

  @Before
  public void setUp() throws Exception {
    context = getApplicationContext();
  }

  // placeholder test so at least one test is found on API 15
  @Test
  public void emptyTest() {}

  @Test
  @SdkSuppress(minSdkVersion = 16)
  public void verifyViewHasBackground() {
    // TODO(b/117557353): investigate failures on API 28
    assumeFalse(VERSION.SDK_INT == 28);
    View viewWithBackground = new View(context);
    int drawable1 = androidx.test.ui.app.R.drawable.drawable_1;
    int drawable2 = androidx.test.ui.app.R.drawable.drawable_2;

    viewWithBackground.setBackground(context.getResources().getDrawable(drawable1));

    assertTrue(new HasBackgroundMatcher(drawable1).matches(viewWithBackground));
    assertFalse(new HasBackgroundMatcher(drawable2).matches(viewWithBackground));
  }

  @Test
  @SdkSuppress(minSdkVersion = 16)
  public void verifyBackgroundWhenBackgroundIsNotSet() {
    View view = new View(context);
    view.setBackground(null);
    int drawable1 = androidx.test.ui.app.R.drawable.drawable_1;

    assertFalse(new HasBackgroundMatcher(drawable1).matches(view));
  }

  private void recycle(Bitmap bitmap) {
    if (bitmap != null) {
      bitmap.recycle();
    }
  }
}
