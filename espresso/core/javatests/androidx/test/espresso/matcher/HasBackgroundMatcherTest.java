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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

  @Test
  // TODO(b/117557353): investigate why this fails on 28
  @SdkSuppress(minSdkVersion = 16, maxSdkVersion = 27)
  public void verifyViewHasBackground() {
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

  @Test
  public void compareSameBitmapImage() {
    Bitmap bitmap = null;
    try {
      bitmap =
          BitmapFactory.decodeResource(context.getResources(), android.R.drawable.alert_dark_frame);
      assertTrue(HasBackgroundMatcher.compareBitmaps(bitmap, bitmap));
    } finally {
      recycle(bitmap);
      bitmap = null;
    }
  }

  @Test
  public void compareDifferentBitmapImages() {
    Bitmap bitmap1 = null;
    Bitmap bitmap2 = null;

    try {
      bitmap1 =
          BitmapFactory.decodeResource(context.getResources(), android.R.drawable.alert_dark_frame);
      bitmap2 =
          BitmapFactory.decodeResource(
              context.getResources(), android.R.drawable.alert_light_frame);

      assertFalse(HasBackgroundMatcher.compareBitmaps(bitmap1, bitmap2));
    } finally {
      recycle(bitmap1);
      recycle(bitmap2);
      bitmap1 = null;
      bitmap2 = null;
    }
  }

  private void recycle(Bitmap bitmap) {
    if (bitmap != null) {
      bitmap.recycle();
    }
  }
}
